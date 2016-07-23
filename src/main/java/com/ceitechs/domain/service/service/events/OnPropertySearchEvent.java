package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.User;

/**
 * Created by iddymagohe on 7/22/16.
 */
public class OnPropertySearchEvent implements OnPangoEvent<PropertySearchCriteria> {

    private final PropertySearchCriteria propertySearchCriteria;

    private final int resultCount;

    private final User user;

    public OnPropertySearchEvent(PropertySearchCriteria propertySearchCriteria, int resultCount, User user) {
        this.propertySearchCriteria = propertySearchCriteria;
        this.resultCount = resultCount;
        this.user = user;
    }

    @Override
    public PropertySearchCriteria get() {
        return null;
    }
}
