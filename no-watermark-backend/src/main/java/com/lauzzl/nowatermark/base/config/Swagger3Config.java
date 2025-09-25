package com.lauzzl.nowatermark.base.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Swagger3Config {

    @Value("${spring.application.version}")
    private String VERSION;

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("无印")
                        .description("无印接口文档")
                        .contact(new Contact().url("https://github.com/LauZzL/no-watermark").name("LauZzL").email("lauzzl@163.com"))
                        .version(VERSION)
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("无印接口文档")
                        .url("https://github.com/LauZzL/no-watermark"));
    }


}