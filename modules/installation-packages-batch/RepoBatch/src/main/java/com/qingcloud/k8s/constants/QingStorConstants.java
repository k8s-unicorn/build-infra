package com.qingcloud.k8s.constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.qingstor.sdk.config.EvnContext;
import com.qingstor.sdk.service.Bucket;

public class QingStorConstants {

	public String qingstor_access_key = "";
	public String qingstor_secret_key = "";
	public String qingstor_budget = "";
	public String qingstor_zone = "";
	public String qingstor_url = "";
	public String qingstor_port = "";
	public String qingstor_schema = "";
	public EvnContext qingstor_context = null;
	public Bucket qingstor_bucket = null;
	public String db_name = "";
	public String db_user = "";
	public String db_pwd = "";
	public String spring_batch_ctx_file_name = "";
	public String source_files_location = "";
	

	private QingStorConstants() {
		
		if (spring_batch_ctx_file_name == "") {
			try {
				initBaseConfig(this.getClass().getResourceAsStream("/config.properties"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (qingstor_context == null) {
			initQingStorEnvContext();
		}
	}

	private static final QingStorConstants qsConstants = new QingStorConstants();

	public static QingStorConstants getInstance() {
		return qsConstants;
	}

	public void initQingStorEnvContext() {
		System.out.println("======initQingStorEnvContext======");
		qingstor_context = new EvnContext(qingstor_access_key, qingstor_secret_key);
	}

	public Bucket getQingStorBudget() {
		if (qingstor_context == null) {
			initQingStorEnvContext();
		}
		if (qingstor_bucket == null) {
			qingstor_bucket = new Bucket(qingstor_context, qingstor_zone, qingstor_budget);
		}
		return qingstor_bucket;
	}

	public void initBaseConfig(InputStream is) throws IOException {
		System.out.println("======initBaseConfig======");
		Properties prop = null;
		try {
			prop = new Properties();

			// create Properties class object
			if (is != null) {
				// load properties file into it
				prop.load(is);
				qingstor_access_key = prop.getProperty("qingstor_access_key");
				qingstor_secret_key = prop.getProperty("qingstor_secret_key");
				qingstor_budget = prop.getProperty("qingstor_budget");
				qingstor_zone = prop.getProperty("qingstor_zone");
				qingstor_url = prop.getProperty("qingstor_url");
				qingstor_port = prop.getProperty("qingstor_port");
				qingstor_schema = prop.getProperty("qingstor_schema");
				db_name = prop.getProperty("db_name");
				db_user = prop.getProperty("db_user");
				db_pwd = prop.getProperty("db_pwd");
				spring_batch_ctx_file_name = prop.getProperty("spring_batch_ctx_file_name");
				source_files_location = prop.getProperty("source_files_location");
			} else {
				throw new FileNotFoundException("property file not found in the classpath");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}

	public String getBatchContextConfig() {
		return spring_batch_ctx_file_name;
	}
}
