package com.vcoding.content.config;

import com.vcoding.content.application.ai.ContentAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ContentAiProperties.class)
public class ContentAiConfig {
}
