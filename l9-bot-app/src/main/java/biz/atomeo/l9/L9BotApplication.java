package biz.atomeo.l9;

import biz.atomeo.l9.config.L9AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(L9AppProperties.class)
public class L9BotApplication {

	public static void main(String[] args) {
		SpringApplication.run(L9BotApplication.class, args);
	}

}
