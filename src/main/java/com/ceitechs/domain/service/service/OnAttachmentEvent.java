package com.ceitechs.domain.service.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author iddymagohe
 * @since 1.0
 */
public interface OnAttachmentEvent<T> {
    T get();
}

@Service
class PangoEventsPublisher {


    private final  ApplicationEventPublisher publisher;

    @Autowired
    PangoEventsPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishAttachmentUploadedEvent(OnAttachmentEvent<List<AttachmentToUpload>> onAttachmentEvent){
        publisher.publishEvent(publisher);
    }


}
