package com.qingcloud.k8s.batch.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.qingcloud.k8s.batch.model.RemoteFile;

public class KubeFileProcessor implements ItemProcessor <RemoteFile, String> {

	// creates an unmodifiable list
	String[] itemArray = new String[] {
			"http://mirrors.sohu.com/centos/7.4.1708/extras/x86_64/Packages/ansible-2.3.1.0-3.el7.noarch.rpm",
			//"http://mirrors.sohu.com/centos/7.4.1708/isos/x86_64/CentOS-7-x86_64-NetInstall-1708.iso",
			"http://mirrors.sohu.com/centos/7.4.1708/isos/x86_64/sha1sum.txt" };

	List<String> items = new ArrayList<String>(Arrays.asList(itemArray));

	public String process(RemoteFile remoteFile) throws Exception {
//		if (!items.isEmpty()) {
//			return getNextItem();
//		}
//		return null;
		return remoteFile.getUrl();
	}

	// using items directly without lock is not thread safe
	private synchronized String getNextItem() {
		if (!items.isEmpty()) {
			return items.remove(0);
		}
		return null;
	}

}