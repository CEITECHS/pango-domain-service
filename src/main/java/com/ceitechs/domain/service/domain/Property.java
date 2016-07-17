package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "property")
@TypeAlias("property")
public class Property {
    @Id
    private String propertyId;

    private String propertyDesc;

    private ListingFor listingFor;

    private Address address;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] location;

    private double rating;

    @DBRef
    private List<PropertyUnit> propertyUnits;

    @DBRef
    private User owner;

    private PropertyFeature features;

    private PropertyRent rent;

    @Transient
    private List<FileMetadata> propertyImages;

    private LocalDate createdDate = LocalDate.now();
}
