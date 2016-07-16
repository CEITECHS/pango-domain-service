package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PropertyFeature {
    private int nbrOfRooms;
    private int nbrOfBedRooms;
    private int nbrOfSelfContainedBedRooms;
    private int nbrOfBaths;
    private int nbrOfKitchens;    
    private String propertySize;
    private String sizeUnit = "sqft"; // default squire foot
    List<String> additionalFeatures;
}
