package com.ceitechs.domain.service.service.events;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface OnAttachmentEvent<T> {
    T get();
}

//TODO: ADD the Attachment Delete Impl