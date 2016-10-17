package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.PropertyHoldingHistory;
import com.ceitechs.domain.service.domain.PropertyRentalHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * @author  iddymagohe on 9/06/16.
 */
public class PropertyRentingEvent implements OnPangoEvent<PropertyRentalHistory> {
    private final PropertyRentalHistory rental;

    private final User owner;

    public PropertyRentingEvent(PropertyRentalHistory rental, User owner) {
        this.rental = rental;
        this.owner = owner;
    }

    @Override
    public PropertyRentalHistory get() {
        return rental;
    }

    @Override
    public User getUser() {
        return owner;
    }
}
