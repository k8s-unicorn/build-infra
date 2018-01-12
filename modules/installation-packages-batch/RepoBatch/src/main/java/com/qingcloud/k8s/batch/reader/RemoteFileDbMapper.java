package com.qingcloud.k8s.batch.reader;



import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.qingcloud.k8s.batch.model.RemoteFile;

public class RemoteFileDbMapper implements RowMapper<RemoteFile>
{
	public RemoteFile mapRow(ResultSet rs, int i) throws SQLException {
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setUrl(rs.getString("url"));

		return remoteFile;
	}

}
