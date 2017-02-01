package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.*;

import java.time.LocalDateTime;

/**
 * @author iddymagohe  on 8/13/16.
 */
public interface PropertyProjection {

    String getPropertyId();

    String getUnitNumber();

    String getPropertyUnitDesc();

    PropertyUnit.PropertyPurpose getPurpose();

    AttachmentProjection getCoverPhoto();

    PropertyFeature getFeatures();

    LocalDateTime getNextAvailableDate();

    Address getAddress();
    User getOwner();
    double getDistance();
    void setDistance(double distance);
}
