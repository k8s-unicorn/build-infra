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

public class KubeDbProcessor implements ItemProcessor <RemoteFile, String> {

    public String process(RemoteFile remoteFile) throws Exception
    {

    	RemoteFile newUrl = new RemoteFile();
    	newUrl.setUrl(remoteFile.getUrl());
    	return remoteFile.getUrl();
    	
    }

}