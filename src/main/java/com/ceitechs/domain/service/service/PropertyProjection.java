package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.PropertyFeature;
import com.ceitechs.domain.service.domain.PropertyUnit;

import java.time.LocalDateTime;

/**
 * @author iddymagohe  on 8/13/16.
 */
public interface PropertyProjection {

    String getPropertyUnitId();

    String getUnitNumber();

    String getPropertyUnitDesc();

    PropertyUnit.PropertyPurpose getPurpose();

    Attachment getCoverPhoto();

    PropertyFeature getFeatures();

    LocalDateTime getNextAvailableDate();

    Address getAddress();
}
