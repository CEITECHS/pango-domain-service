package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyHoldingHistoryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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

    @Test
    public void retrieveHoldingHistoryTest(){
        unitRepository.deleteAll();
        userRepository.deleteAll();
        propertyHoldingHistoryRepository.deleteAll();
        List<PropertyUnit> propertyUnits = new ArrayList<>();
        List<User> users = new ArrayList<>();
        User userL = createUser();
        for (int i = 0; i < 60; i++) {

            if (i % 10 == 0) {
                userL = createUser();
                users.add(userL);
            } else {
                users.add(createUser());
            }

            propertyUnits.add(createPropertyUnit(userL));
        }

        List<PropertyHoldingHistory> holdingHistories = propertyUnits.parallelStream().map(p -> {
            // Create a new holding rentingHistory
            String holdId = PangoUtility.generateIdAsString();
            PropertyHoldingHistory unitHoldingHistory = new PropertyHoldingHistory();
            unitHoldingHistory.setHoldingReferenceId(holdId);
            unitHoldingHistory.setUser(users.get(PangoUtility.random(0, users.size() - 1)));
            unitHoldingHistory.setPropertyUnit(p);
            // Holding on a property would be for 48 hours
            unitHoldingHistory.setStartDate(LocalDateTime.now());
            unitHoldingHistory.setEndDate(LocalDateTime.now().plusDays(1));
            unitHoldingHistory.setOwnerReferenceId(p.getOwner().getUserReferenceId());

            // Holding Payment
            PendingPayment holdingPayment = new PendingPayment();
            unitHoldingHistory.setHoldingPayment(holdingPayment);

            // User Transaction History
            UserTransactionHistory transactionHistory = new UserTransactionHistory();
            transactionHistory.setTransactionType(TransactionType.HOLDING);
            unitHoldingHistory.setTransactionHistory(transactionHistory);
            return unitHoldingHistory;
        }).collect(Collectors.toList());

        propertyHoldingHistoryRepository.save(holdingHistories);

        Map<User, Long> userPropertyHoldingHistoryMap = holdingHistories.stream().collect(groupingBy(PropertyHoldingHistory::getUser,counting()));
        Map<User, Long> ownerPropertyHoldingHistoryMap = propertyUnits.stream().collect(groupingBy(PropertyUnit::getOwner,counting()));

        ownerPropertyHoldingHistoryMap.forEach((k,v) -> {
            List<PropertyHoldingHistory> ownerHoldings = null;
            try {
                ownerHoldings = leasingService.retrievesHoldingHistoryBy(k, true);
                Assert.notNull(ownerHoldings);
                assertThat("size should be " + v, ownerHoldings, hasSize(v.intValue()));
            } catch (EntityNotFound entityNotFound) {
                entityNotFound.printStackTrace();
            }

        });

        userPropertyHoldingHistoryMap.forEach((k, v) -> {
            List<PropertyHoldingHistory> userHoldings = null;
            try {
                userHoldings = leasingService.retrievesHoldingHistoryBy(k, false);
                Assert.notNull(userHoldings);
                assertThat("size should be : " + v, userHoldings, hasSize(v.intValue()));
            } catch (EntityNotFound entityNotFound) {
                entityNotFound.printStackTrace();
            }

        });

        propertyHoldingHistoryRepository.save(holdingHistories);

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

    private PropertyUnit createPropertyUnit(User user) {

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        String propertyId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyUnitDesc("Amazing 2 bedrooms appartment");
        propertyUnit.setPropertyId(propertyId);

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

 //TODO updatePropertyHoldingRequest test-case
}
