package com.pastelpunk.queue.core.features.queue.client;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.google.gson.Gson;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.pastelpunk.queue.core.configuration.CommonConfiguration;
import com.pastelpunk.queue.core.model.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueueService {

    private static final String QUEUE_NAME = "pastelpunkqueue";

    private final QueueClient queueClient;
    private final int retries;

    private final Gson gson = new Gson();

    public QueueService(CommonConfiguration commonConfiguration) {
        retries = commonConfiguration.getQueueConfiguration().getRetries();

        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
                .connectionString(commonConfiguration.getQueueConfiguration().getConnectionString())
                .buildClient();
        queueClient = queueServiceClient.getQueueClient(QUEUE_NAME);
    }

    public void addItemToQueue(UnitOfWork unitOfWork) {
        queueClient.sendMessage(gson.toJson(unitOfWork));
    }

    public void returnItemToQueue(UnitOfWork unitOfWork) {
        queueClient.updateMessage(unitOfWork.getMessageId(), unitOfWork.getPopReceipt(),
                gson.toJson(unitOfWork), Duration.ofSeconds(5));
    }


    public void deleteItemFromQueue(UnitOfWork unitOfWork) {
        queueClient.deleteMessage(unitOfWork.getMessageId(), unitOfWork.getPopReceipt());
    }

    private @Nullable
    UnitOfWork getQueueItem2() {
        QueueMessageItem queueMessageItem = queueClient.receiveMessage();

        if (queueMessageItem == null) {
            return null;
        }

        if (retries < queueMessageItem.getDequeueCount()) {
            log.error("Expiring message {}", queueMessageItem.getMessageText());
            queueClient.deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
        }

        return map(queueMessageItem);
    }


    @HystrixCommand(fallbackMethod = "reliable")
    public UnitOfWork getUnitOfWork() {

        PagedIterable<QueueMessageItem> queueMessageItems =
                queueClient.receiveMessages(1,
                        Duration.ofHours(2),
                        Duration.ofSeconds(30),
                        null);

        if (!queueMessageItems.iterator().hasNext()) {
            throw new RuntimeException();
        }

        List<UnitOfWork> unitOfWorks = queueMessageItems.stream()
                .filter(this::filterMessages)
                .map(this::map)
                .collect(Collectors.toList());

        assert unitOfWorks.size() <= 1;
        return unitOfWorks.isEmpty() ? null : unitOfWorks.get(0);

    }

    public UnitOfWork reliable() {
        return null;
    }

    private boolean filterMessages(QueueMessageItem queueMessageItem) {
        if (retries < queueMessageItem.getDequeueCount()) {
            log.error("Expiring message {}", queueMessageItem.getMessageText());
            queueClient.deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
            return false;
        } else {
            return true;
        }
    }

    private UnitOfWork map(QueueMessageItem queueMessageItem) {
        String messageText = queueMessageItem.getMessageText();
        UnitOfWork unitOfWork = gson.fromJson(messageText, UnitOfWork.class);
        unitOfWork.setMessageId(queueMessageItem.getMessageId());
        unitOfWork.setPopReceipt(queueMessageItem.getPopReceipt());
        return unitOfWork;
    }
}
