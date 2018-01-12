package com.qingcloud.k8s.batch.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.*;

import com.qingcloud.k8s.constants.QingStorConstants;

@Component
public class KubeCommandLineRunner implements CommandLineRunner {

    public void run(String... args) {
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
		} catch (JobExecutionAlreadyRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobRestartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobInstanceAlreadyCompleteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobParametersInvalidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			appContext.close();
		}
    }

}