package com.gieun.commerce.global.config;


import com.gieun.commerce.global.security.AuthCookieProvider;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Domain API Docs",
        description = "api docs",
        version = "v1"
    )
)
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    // 인증은 HttpOnly access_token 쿠키로 이뤄지므로, 실제 방식(쿠키 apiKey)을 스킴에 반영한다.
    // (스킴 이름 "JWT"는 각 컨트롤러의 @SecurityRequirement(name="JWT")와 매칭)
    String securityJwtName = "JWT";
    Components components = new Components()
        .addSecuritySchemes(securityJwtName, new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.COOKIE)
            .name(AuthCookieProvider.ACCESS_TOKEN_COOKIE));

    return new OpenAPI()
        .components(components);
  }

}