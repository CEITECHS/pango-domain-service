package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PropertyRent {
    private double amount;
    private String currency;
    private PerPeriod periodforAmount;
    private int rentalPeriod;
}

