package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyHoldingHistoryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author  iddymagohe on 8/30/16.
 */
public class PangoPropertyLeasingServiceTest extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired
    UserRepository userRepository;
    @Autowired
    private PangoPropertyLeasingService leasingService;

    @Autowired
    private PropertyHoldingHistoryRepository propertyHoldingHistoryRepository;

    @Autowired
    PropertyUnitRepository unitRepository;

    @Test(expected = EntityExists.class)
    public void createHoldingRequestTest() throws EntityExists, EntityNotFound {
        unitRepository.deleteAll();
        userRepository.deleteAll();
        propertyHoldingHistoryRepository.deleteAll();

        PropertyUnit unit = createPropertyUnit();
        User user = createUser();

        Optional<PropertyHoldingHistory> propertyHoldingHistory = leasingService.createPropertyHoldingRequestBy(user,unit.getPropertyId());
        assertTrue(propertyHoldingHistory.isPresent());
        assertEquals("users details must be equal",user.getUserReferenceId(),propertyHoldingHistory.get().getUser().getUserReferenceId());
        assertEquals("Owner Ids must be equal",unit.getOwner().getUserReferenceId(),propertyHoldingHistory.get().getOwnerReferenceId());

        leasingService.createPropertyHoldingRequestBy(user,unit.getPropertyId()); // throws EntityExists

    }

    @Test(expected = EntityNotFound.class)
    public void createHoldingRequestUnknownUserOrPropertyTest() throws EntityExists, EntityNotFound {
        User user = new User();
        user.setUserReferenceId("24234324");
        Optional<PropertyHoldingHistory> propertyHoldingHistory = leasingService.createPropertyHoldingRequestBy(user,"123456");

    }

    public User createUser(){
        User user = new User();
        String userReferenceId = PangoUtility.generateIdAsString();
        user.setUserReferenceId(userReferenceId);
        Address address = new Address();
        address.setAddressLine1("Masaki");
        address.setAddressLine2("Address Line 2");
        address.setCity("Dar es Salaam,");
        address.setState("Dar es Salaam");
        address.setZip("12345");
        user.setAddress(address);

        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        UserProfile userProfile = new UserProfile();
        userProfile.setVerified(true);
        userProfile.setPassword("gfuyegrfuyergfu");

        user.setProfile(userProfile);

        // Save the user
        return userRepository.save(user);
    }

    public PropertyUnit createPropertyUnit() {

        User user = new User();
        String userReferenceId = PangoUtility.generateIdAsString();
        user.setUserReferenceId(userReferenceId);
        Address address = new Address();
        address.setAddressLine1("Masaki");
        address.setAddressLine2("Address Line 2");
        address.setCity("Dar es Salaam,");
        address.setState("Dar es Salaam");
        address.setZip("12345");
        user.setAddress(address);

        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        // Save the user
        userRepository.save(user);

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        String propertyId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyId(propertyId);
        propertyUnit.setPropertyUnitDesc("Amazing 2 bedrooms appartment");

        // Adding listing
        propertyUnit.setListingFor(ListingFor.RENT);

        // Adding property purpose
        propertyUnit.setPurpose(PropertyUnit.PropertyPurpose.HOME);

        // Adding location(long,lat)
        double[] location = { 39.272271,-6.816064};
        propertyUnit.setLocation(location);

        // Adding the owner details
        propertyUnit.setOwner(user);

        // Adding property feature
        PropertyFeature features = new PropertyFeature();
        features.setPropertySize(1200.0);
        features.setNbrOfBaths(2);
        features.setNbrOfBedRooms(3);
        features.setNbrOfRooms(5);
        propertyUnit.setFeatures(features);

        propertyUnit.setNextAvailableDate(LocalDateTime.now().plusDays(3));

        // Adding property rent
        PropertyRent rent = new PropertyRent();
        rent.setAmount(1200);
        rent.setCurrency("TZS");
        rent.setPeriodforAmount(PerPeriod.MONTHLY);
        propertyUnit.setRent(rent);

        return unitRepository.save(propertyUnit);
    }
}
