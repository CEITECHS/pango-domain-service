package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.User;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface OnPangoEvent<T> {
    T get();

    default User getUser(){
        throw new UnsupportedOperationException("Please implement this in service layer");
    }
}

//TODO: ADD the Attachment Delete Impl