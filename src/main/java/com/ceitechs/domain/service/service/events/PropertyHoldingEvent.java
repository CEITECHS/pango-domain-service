package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.PropertyHoldingHistory;

/**
 * @author  iddymagohe on 8/29/16.
 */
public class PropertyHoldingEvent implements OnPangoEvent<PropertyHoldingHistory> {
    private final PropertyHoldingHistory unitHolding;

    public PropertyHoldingEvent(PropertyHoldingHistory unitHolding) {
        this.unitHolding = unitHolding;
    }

    @Override
    public PropertyHoldingHistory get() {
        return null;
    }
}
