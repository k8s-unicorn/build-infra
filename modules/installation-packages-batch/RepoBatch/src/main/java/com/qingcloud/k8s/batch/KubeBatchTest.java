package com.qingcloud.k8s.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qingcloud.k8s.constants.QingStorConstants;
//@SpringBootApplication
public class KubeBatchTest {

	public static void main(String[] args) throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(QingStorConstants.getInstance().getBatchContextConfig());
		try {

			// appContext = new
			// ClassPathXmlApplicationContext("classpath:applicationBatchContext.xml");
			// get the launcher
			JobLauncher jobLauncher = (JobLauncher) appContext.getBean("jobLauncher");
			// get the job to run
			Job job = (Job) appContext.getBean("simpleJob");
			
			// run
			jobLauncher.run(job, new JobParameters());
		}

		finally {
			appContext.close();
		}

	}
//	public static void main(String[] args) throws Exception {
//		SpringApplication.run(KubeBatchTest.class, args);
//	}
}