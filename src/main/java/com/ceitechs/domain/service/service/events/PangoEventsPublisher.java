package com.ceitechs.domain.service.service.events;

/**
 * Created by iddymagohe on 7/17/16.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author iddymagohe
 * @since 1.0
 */

@Service
public class PangoEventsPublisher {
    private final ApplicationEventPublisher publisher;

    @Autowired
    PangoEventsPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * An easier way to publish various events of type {@link OnAttachmentEvent}
     *
     * @param onAttachmentEvent
     * @param <T>
     */
    public <T> void publishAttachmentEvent(OnAttachmentEvent<T> onAttachmentEvent){
        publisher.publishEvent(onAttachmentEvent);
    }
}