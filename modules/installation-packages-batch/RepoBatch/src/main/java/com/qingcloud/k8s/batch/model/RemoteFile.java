package com.qingcloud.k8s.batch.model;

public class RemoteFile {
	private String url;
	public RemoteFile() {
		
	}

	public RemoteFile(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
