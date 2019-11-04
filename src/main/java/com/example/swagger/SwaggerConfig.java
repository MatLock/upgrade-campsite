package com.example.swagger;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

  public SwaggerConfig() {
  }

  @Bean
  public Docket api() {
    TypeResolver typeResolver = new TypeResolver();
    return new Docket(DocumentationType.SWAGGER_2)
            .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
            .alternateTypeRules(AlternateTypeRules.newRule(
                    typeResolver.resolve(List.class, LocalDateTime.class),
                    typeResolver.resolve(List.class, Date.class), Ordered.HIGHEST_PRECEDENCE))

            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example.controller"))
            .paths(PathSelectors.any())
            .build();
  }
}
