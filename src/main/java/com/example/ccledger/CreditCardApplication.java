package com.example.ccledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@Configurationと@EnableAutoConfigurationと@ComponentScanをまとめたもの
public class CreditCardApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditCardApplication.class, args);
		//デフォルトの引数で実行される
	}

}
