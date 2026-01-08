package com.k12xue.ycbiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 测试页配置
 * 测试地址：<a href="http://localhost:8081/doc.html#/home">...</a>
 * 如果默认8081端口进行了修改，记得修改
 * @author Orkin_Prime
 * @date 2026/1/7 15:04
 */

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("12xue-backend")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.k12xue.ycbiz.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("永城外国语课堂小功能使用统计 API")
                .description("Spring Boot + MyBatis Plus 接口文档")
                .version("1.0")
                .build();
    }
}