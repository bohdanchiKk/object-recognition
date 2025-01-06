package org.example.imagebackend.config;

import org.example.imagebackend.response.UploadResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public UploadResponse uploadResponse(){
        return new UploadResponse();
    }
}
