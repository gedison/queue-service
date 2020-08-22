package com.pastelpunk.queue.core.configuration;

import com.pastelpunk.queue.core.configuration.model.QueueConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("pastelpunk")
@Getter
@Setter
public class CommonConfiguration {
    private QueueConfiguration queueConfiguration;
}
