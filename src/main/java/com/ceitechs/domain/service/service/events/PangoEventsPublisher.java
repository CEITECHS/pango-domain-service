package com.ceitechs.domain.service.service.events;


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
     * An easier way to publish various events of type {@link OnPangoEvent}
     *
     * @param onPangoEvent
     * @param <T>
     */
    public <T> void publishAttachmentEvent(OnPangoEvent<T> onPangoEvent){
        publisher.publishEvent(onPangoEvent);
    }
}