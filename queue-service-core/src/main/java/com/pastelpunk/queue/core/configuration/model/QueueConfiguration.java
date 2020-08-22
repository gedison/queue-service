package com.pastelpunk.queue.core.configuration.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueConfiguration {
    private String connectionString;
    private int retries;
}
