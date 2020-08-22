package com.pastelpunk.queue.core.features.work;


import com.pastelpunk.queue.core.model.QueueServiceException;
import com.pastelpunk.queue.core.model.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
public class WorkProcessor {

    public void process(UnitOfWork unitOfWork) throws QueueServiceException {
        log.info("Processing {}", unitOfWork);
        if(new SecureRandom().nextInt(100) > 50 ){
            throw new QueueServiceException();
        }
    }
}
