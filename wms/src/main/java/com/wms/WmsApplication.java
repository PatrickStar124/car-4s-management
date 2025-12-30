package com.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wms.mapper")  // 确保有这个注解
public class WmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(WmsApplication.class, args);
	}
}