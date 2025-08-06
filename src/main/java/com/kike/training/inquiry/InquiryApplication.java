package com.kike.training.inquiry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication( exclude = { DataSourceAutoConfiguration.class })
@EnableAspectJAutoProxy
public class InquiryApplication {

	public static void main(String[] args) {
		SpringApplication.run(InquiryApplication.class, args);
	}

}
