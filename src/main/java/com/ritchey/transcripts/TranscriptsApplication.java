package com.ritchey.transcripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TranscriptsApplication {
	private static Logger LOG = LoggerFactory.getLogger(TranscriptsApplication.class);

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(TranscriptsApplication.class, args)));
	}

}
