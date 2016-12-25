package com.ceitechs.domain.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3Client;

@Configuration
public class PangoDomainServiceS3Config {
	
	@Bean
	public AmazonS3Client s3Clinet(){
		return new AmazonS3Client();		
	}
}
