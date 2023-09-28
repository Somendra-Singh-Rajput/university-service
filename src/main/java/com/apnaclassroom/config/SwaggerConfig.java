package com.apnaclassroom.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Admin Ease India",
                        email = "support@adminease.in",
                        url = "https://adminease.in"
                ),
                description = "OpenApi documentation for Apnaclassroom CRM",
                title = "OpenApi specification - Apnaclassroom",
                version = "1.0",
                license = @License(
                        name = "Licence name",
                        url = "https://adminease.in"
                ),
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "${swagger.local.url}"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "${swagger.prod.url}"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public Docket publicApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(Collections.singleton("https"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.apnaclassroom.controller"))
                .paths(PathSelectors.regex("/public.*"))
                .build()
                .groupName("springshop-public")
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("University Management System")
                .description("This is a system for universities to manage everything at their end")
                .version("1.0")
                .build();
    }
}
