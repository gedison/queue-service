package com.pastelpunk.queue.core.features.work;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.pastelpunk.queue.core.features.queue.client.QueueService;
import com.pastelpunk.queue.core.model.QueueServiceException;
import com.pastelpunk.queue.core.model.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkConsumer {

    private final QueueService queueService;
    private final WorkProcessor workProcessor;

    public WorkConsumer(@NonNull QueueService queueService,
                        @NonNull WorkProcessor workProcessor){
        this.queueService = queueService;
        this.workProcessor = workProcessor;
    }

    @Scheduled(fixedDelay = 1000)
    public void run(){
        UnitOfWork unitOfWork = queueService.getUnitOfWork();
        if(unitOfWork != null) {
            try {
                workProcessor.process(unitOfWork);
                queueService.deleteItemFromQueue(unitOfWork);
            }catch (QueueServiceException e){
                queueService.returnItemToQueue(unitOfWork);
            }catch (Exception e){
                log.error("",e);
                queueService.deleteItemFromQueue(unitOfWork);
            }
        }
    }


}
