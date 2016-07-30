package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.User;

/**
 * @author  by iddymagohe on 7/30/16.
 * @since 1.0
 */
public class UserVerificationEvent implements OnPangoEvent<User>{

    private User user;

    public UserVerificationEvent(User user) {
        this.user = user;
    }

    @Override
    public User get() {
        return user;
    }

}
