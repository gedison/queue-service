package com.pastelpunk.queue.core.features.work;

import com.pastelpunk.queue.core.features.queue.client.QueueService;
import com.pastelpunk.queue.core.model.UnitOfWork;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkProducer {
    private final QueueService queueService;

    public WorkProducer(@NonNull QueueService queueService){
        this.queueService = queueService;
    }

    @GetMapping("api/v1/work")
    public void addMessage(){
        for(int i=0; i<100; i++){
            queueService.addItemToQueue(new UnitOfWork());
        }
    }
}
