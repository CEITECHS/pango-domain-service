package com.ceitechs.domain.service.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Address {
    private String addressLine1;

    private String addressLine2;

    private String city;

    private String State;

    private String zip;

    private String country;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (addressLine1 != null ? !addressLine1.equals(address.addressLine1) : address.addressLine1 != null)
            return false;
        if (addressLine2 != null ? !addressLine2.equals(address.addressLine2) : address.addressLine2 != null)
            return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (State != null ? !State.equals(address.State) : address.State != null) return false;
        return country != null ? country.equals(address.country) : address.country == null;

    }

    @Override
    public int hashCode() {
        int result = addressLine1 != null ? addressLine1.hashCode() : 0;
        result = 31 * result + (addressLine2 != null ? addressLine2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (State != null ? State.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}
