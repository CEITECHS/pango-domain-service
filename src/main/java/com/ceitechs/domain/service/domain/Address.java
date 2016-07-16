package com.ceitechs.domain.service.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
@Getter
@Setter
@EqualsAndHashCode
public class Address {
    private String addressLine1;

    private String addressLine2;

    private String city;

    private String State;

    private String zip;

    private String country;

}
