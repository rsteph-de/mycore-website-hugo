package org.mycore.website.hugo.builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HugoBuilderApp {
	
	@Autowired
	HugoBuilderService builderService;
	
	@Autowired
	HugoBuilderDataStore builderData;
	
	public static Log LOGGER = LogFactory.getLog(HugoBuilderApp.class);
	private static long DELAY_MS = 165000; 
	private static long EXECUTION_DELAY_S = 180; //some seconds more the the delay; 
	private static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

	
	@PostConstruct
	public void init() {

		SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
				
				@Override
				public void run() {
					long now = System.currentTimeMillis();
					if(now - DELAY_MS > builderData.getLastCommitTime().longValue()) {
				      LOGGER.info("Last commit was more than 3 min ago ... starting build task execution");
				      	builderService.run();
					}		
				}
			},
		    1, EXECUTION_DELAY_S,
		    TimeUnit.MILLISECONDS);

	}
	
	@PreDestroy
    public void destroy() {
		LOGGER.info("Going to shutdown Hugo Builder");
		SCHEDULED_EXECUTOR_SERVICE.shutdown();
    }

	public static void main(String... args) {
		SpringApplication.run(HugoBuilderApp.class, args);
	}
}
