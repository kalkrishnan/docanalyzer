package com.kkrishnan.docanalyzer;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, PgVectorStoreAutoConfiguration.class })
public class DocanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocanalyzerApplication.class, args);
	}

}
