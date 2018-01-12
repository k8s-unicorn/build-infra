package com.qingcloud.k8s.batch.writer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.batch.item.ItemWriter;

import com.qingcloud.k8s.constants.QingStorConstants;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.service.Bucket;
import com.qingstor.sdk.service.Bucket.CompleteMultipartUploadOutput;
import com.qingstor.sdk.service.Bucket.InitiateMultipartUploadOutput;
import com.qingstor.sdk.service.Bucket.UploadMultipartOutput;

public class KubeWriter implements ItemWriter<String> {
	final static int blocksize = 16 * 1024 * 1024;

	public void write(List<? extends String> items) throws Exception {
		for (String fileURL : items) {
			try {
				uploadFileToQingStor(fileURL);
			} catch (QSException qse) {
				qse.printStackTrace();
			}

		}
	}

	public void uploadFileToQingStor(String remoteFileURL) throws QSException, IOException {

		String objectName = FilenameUtils.getName(remoteFileURL);

		System.out.println("start to handle file " + objectName + " --- start time " + System.currentTimeMillis());

		Bucket.InitiateMultipartUploadInput inputInit = new Bucket.InitiateMultipartUploadInput();
		inputInit.setContentType(getContentType(remoteFileURL));
		InitiateMultipartUploadOutput initOutput = QingStorConstants.getInstance().getQingStorBudget()
				.initiateMultipartUpload(objectName, inputInit);

		String multipart_upload_name = objectName;
		// init multipart_upload_id
		String multipart_upload_id = initOutput.getUploadID();
		System.out.println("-multipart_upload_id----" + initOutput.getUploadID());

		StringBuffer content = new StringBuffer();
		content.append("{\n" + "    \"object_parts\": [\n");
		BufferedInputStream in = null;
		// InputStream in = null;
		try {
			in = new BufferedInputStream(new URL(remoteFileURL).openStream());
			// in = new URL(remoteFileURL).openStream();

			int fileSize = getFileSize(remoteFileURL);
			byte data[] = new byte[blocksize];
			int offset = 0;
			int bytesRead;
			int totalBytesRead = 0;
			int part_number = 0;
			String strFileContents;
			if (fileSize < blocksize) {
				data = new byte[fileSize];
				while ((bytesRead = in.read(data, offset, fileSize - offset)) > 0) {
					//System.out.println("bytesRead #: " + bytesRead);
					if (offset + bytesRead >= fileSize) {
						strFileContents = new String(data);

						UploadMultipartOutput bm = QingStorConstants.getInstance().getQingStorBudget()
								.uploadMultipart(multipart_upload_name, composeUploadInput(multipart_upload_id,
										multipart_upload_name, part_number, strFileContents, fileSize));
						// System.out.println("-UploadMultipartOutput----" +
						// bm.getMessage());
						System.out.println("uploading part #: " + part_number);
						content.append(" {\"part_number\": " + part_number + "},\n");
					}
					offset += bytesRead;
				}
			} else {
				while ((bytesRead = in.read(data, offset, blocksize - offset)) > -1) {
					if (offset + bytesRead >= blocksize) {
						totalBytesRead = totalBytesRead + offset + bytesRead;
						strFileContents = new String(data);
						// System.out.println("bytesRead #: " + bytesRead);

						UploadMultipartOutput bm = QingStorConstants.getInstance().getQingStorBudget()
								.uploadMultipart(multipart_upload_name, composeUploadInput(multipart_upload_id,
										multipart_upload_name, part_number, strFileContents, blocksize));
						System.out.println(
								"-UploadMultipartOutput----id: " + bm.getRequestId() + "--msg: " + bm.getMessage());
						System.out.println("uploading part #: " + part_number);
						content.append(" {\"part_number\": " + part_number + "},\n");
						part_number++;
						data = new byte[blocksize];
						offset = 0;
					}
					// System.out.println("-----" + offset + "------" +
					// bytesRead + "------" + offset+bytesRead);
					offset += bytesRead;
				}
				offset = 0;
				System.out.println("fileSize: " + fileSize + "--- totalBytesRead: " + totalBytesRead);
				int bytesLeft = fileSize - totalBytesRead;
				data = new byte[bytesLeft];
				System.out.println("bytesLeft: " + bytesLeft);
				System.out.println(in.read(data, offset, bytesLeft - offset));
				while (bytesLeft > 0 && (bytesRead = in.read(data, offset, bytesLeft - offset)) > 0) {
					System.out.println("download the last part " + part_number);
					if (offset + bytesRead >= bytesLeft) {
						System.out.println("upload the last part " + part_number);
						strFileContents = new String(data);
						// System.out.println("bytesRead #: " + bytesRead);

						UploadMultipartOutput bm = QingStorConstants.getInstance().getQingStorBudget()
								.uploadMultipart(multipart_upload_name, composeUploadInput(multipart_upload_id,
										multipart_upload_name, part_number, strFileContents, bytesLeft));
						System.out.println(
								"-UploadMultipartOutput----id: " + bm.getRequestId() + "--msg: " + bm.getMessage());
						System.out.println("uploading part #: " + part_number);
						content.append(" {\"part_number\": " + part_number + "},\n");
					}
					offset += bytesRead;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		content.replace(content.length() - 2, content.length() - 1, "");
		content.append(" ]\n}");
		// Write code here that turns the phrase above into concrete actions
		Bucket.CompleteMultipartUploadInput input = new Bucket.CompleteMultipartUploadInput();
		input.setUploadID(initOutput.getUploadID());

		System.out.println("total content: " + content.toString());
		input.setBodyInput(content.toString());
		CompleteMultipartUploadOutput completeMultipartUploadOutput = QingStorConstants.getInstance().getQingStorBudget()
				.completeMultipartUpload(multipart_upload_name, input);
		System.out.println("-completeMultipartUploadOutput----" + completeMultipartUploadOutput.getMessage());

	}

	public Bucket.UploadMultipartInput composeUploadInput(String id, String name, int number, String content, int size)
			throws IOException {
		// System.out.println("bytesRead #: " + bytesRead);
		Bucket.UploadMultipartInput input = new Bucket.UploadMultipartInput();
		input.setXQSEncryptionCustomerKey(name + number);
		input.setContentLength((long) size);
		input.setBodyInputStream(IOUtils.toInputStream(content, "UTF-8"));
		input.setPartNumber(number);
		input.setUploadID(id);
		return input;
	}

	public int getFileSize(String remoteFileURL) {
		try {
			URL url = new URL(remoteFileURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			if (code == 200) {
				return conn.getContentLength();
			}

		} catch (IOException ioe) {
			System.err.println("file doesn't exist on remote site " + ioe.getMessage());
		}
		return 0;
	}

	public String getContentType(String remoteFileURL) {
		HttpURLConnection conn;
		try {
			URL url = new URL(remoteFileURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			if (code == 200) {
				System.out.println(conn.getContentType());
				return conn.getContentType();
			}

		} catch (IOException ioe) {
			System.err.println("file doesn't exist on remote site " + ioe.getMessage());
		}
		return "";
	}
}