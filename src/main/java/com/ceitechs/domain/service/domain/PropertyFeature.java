package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class PropertyFeature {
    private boolean studio = false;
    private int nbrOfRooms;
    private int nbrOfBedRooms;
    private int nbrOfSelfContainedBedRooms;
    private int nbrOfBaths;
    private int nbrOfKitchens;    
    private double propertySize;
    private String sizeUnit = "sqft"; // default square foot
    private List<Amenity> additionalFeatures = new ArrayList<>();
}
