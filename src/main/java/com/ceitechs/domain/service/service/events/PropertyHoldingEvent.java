package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.PropertyHoldingHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * @author  iddymagohe on 8/29/16.
 */
public class PropertyHoldingEvent implements OnPangoEvent<PropertyHoldingHistory> {
    private final PropertyHoldingHistory unitHolding;

    private final User owner;

    public PropertyHoldingEvent(PropertyHoldingHistory unitHolding, User owner) {
        this.unitHolding = unitHolding;
        this.owner = owner;
    }

    @Override
    public PropertyHoldingHistory get() {
        return unitHolding;
    }

    @Override
    public User getUser() {
        return owner;
    }
}
