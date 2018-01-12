package com.qingcloud.k8s.batch.reader;



import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.qingcloud.k8s.batch.model.RemoteFile;

public class RemoteFileFieldSetMapper implements FieldSetMapper<RemoteFile>
{
    public RemoteFile mapFieldSet(FieldSet fieldSet) throws BindException {
    	RemoteFile remoteFile = new RemoteFile();
    	remoteFile.setUrl( fieldSet.readString(0));//( "url" ) );
        return remoteFile;
    }

}
