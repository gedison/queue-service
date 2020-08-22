package com.pastelpunk.queue.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.RandomStringUtils;

@Getter
@Setter
@ToString
public class UnitOfWork {
    private String messageId;
    private String popReceipt;
    private String clientId = RandomStringUtils.randomAlphabetic(12);
}
