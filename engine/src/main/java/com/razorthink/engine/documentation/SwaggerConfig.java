package com.razorthink.engine.documentation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.common.base.Predicate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import static springfox.documentation.builders.PathSelectors.regex;
import springfox.documentation.builders.RequestHandlerSelectors;
import static com.google.common.base.Predicates.or;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket postsApi() {

        return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
                .apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.basePackage("com.razorthink.engine"))
                .paths(postPaths()).build();
    }

    private Predicate<String> postPaths() {
        return or(regex("/api.*"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Engine API")
                .description("RestAPI with spring boot")
                .version("1.0").build();
    }

}