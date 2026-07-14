package com.FundRaise.F25.Config;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration
public class CorsConfig implements WebMvcConfigurer {
 
    // Comma-separated list. Set in application.properties (or as an env var
    // APP_CORS_ALLOWED_ORIGINS on your host), e.g.:
    //   app.cors.allowed-origins=http://localhost:5173,https://your-frontend-domain.com
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
 