package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author  by iddymagohe on 8/10/16.
 */
public class PangoEnquiryServiceTest  extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired PangoEnquiryService enquiryService;
    @Autowired UserRepository userRepository;
    @Autowired PropertyUnitRepository propertyUnitRepository;
    @Autowired PropertyUnitEnquiryRepository enquiryRepository;


    @Test
    public void createUserEnquiryToPropertyTest() throws IOException, EntityExists, EntityNotFound {
        userRepository.deleteAll();
        propertyUnitRepository.deleteAll();
        enquiryRepository.deleteAll();
        User usr = createLoggedInUser();
        User savedUsr = userRepository.save(usr);
        assertNotNull(savedUsr);

        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnit.setOwner(savedUsr);

        PropertyUnit savedPrt = propertyUnitRepository.save(propertyUnit);
        assertNotNull(propertyUnitRepository.save(savedPrt));

        PropertyUnitEnquiry enquiry= new PropertyUnitEnquiry();
        enquiry.setPropertyUnit(savedPrt);
        enquiry.setProspectiveTenant(savedUsr);
        enquiry.setMessage("Interested in one of your properties");
        enquiry.setEnquiryType(CorrespondenceType.INTERESTED);

       Optional<PropertyUnitEnquiry> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);
        assertTrue(propertyUnitEnquiry.isPresent());
        assertEquals(enquiry.getMessage(),propertyUnitEnquiry.get().getMessage());
    }

    @Test(expected = EntityExists.class)
    public void createUserEnquiryToPropertyExistsTest() throws IOException, EntityExists, EntityNotFound {
        userRepository.deleteAll();
        propertyUnitRepository.deleteAll();
        enquiryRepository.deleteAll();
        User usr = createLoggedInUser();
        User savedUsr = userRepository.save(usr);
        assertNotNull(savedUsr);
        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnit.setOwner(savedUsr);
        PropertyUnit savedPrt = propertyUnitRepository.save(propertyUnit);
        assertNotNull(propertyUnitRepository.save(savedPrt));

        PropertyUnitEnquiry enquiry= new PropertyUnitEnquiry();
        enquiry.setProspectiveTenant(savedUsr);
        enquiry.setPropertyUnit(savedPrt);

        enquiry.setMessage("Interested in one of your properties");
        enquiry.setEnquiryType(CorrespondenceType.INTERESTED);

        Optional<PropertyUnitEnquiry> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);
        assertTrue(propertyUnitEnquiry.isPresent());
        assertEquals(enquiry.getMessage(),propertyUnitEnquiry.get().getMessage());
        enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);

    }

    @Test(expected = EntityNotFound.class)
    public void createUnkownUserEnquiryToPropertyTest() throws IOException, EntityExists, EntityNotFound {
        userRepository.deleteAll();
        propertyUnitRepository.deleteAll();
        enquiryRepository.deleteAll();
        User usr = createLoggedInUser();
        User savedUsr = userRepository.save(usr);
        assertNotNull(savedUsr);
        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnit.setPropertyUnitId("un existing Id");


        PropertyUnitEnquiry enquiry= new PropertyUnitEnquiry();
        enquiry.setProspectiveTenant(savedUsr);
        enquiry.setPropertyUnit(propertyUnit);

        enquiry.setMessage("Interested in one of your properties");
        enquiry.setEnquiryType(CorrespondenceType.INTERESTED);

        Optional<PropertyUnitEnquiry> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,propertyUnit.getPropertyUnitId(),enquiry);


    }

    public void addEnquiryCorrespondenceTest(){
        //TODO test all exception scenarios
        // added correspondence
        // uploaded attachment
        fail("not yet implemented");
    }

    static User createLoggedInUser(){
        User user = new User();
        user.setFirstName("iam");
        user.setLastName("magohe");
        user.setUserReferenceId(PangoUtility.generateIdAsString());
        user.setEmailAddress("iam.magohe@pango.com");
        UserProfile userProfile = new UserProfile();
        userProfile.setPassword("123456");
        userProfile.setVerified(true);
        user.setProfile(userProfile);
        return user;
    }

    public PropertyUnit createPropertyUnit() throws IOException {
        String userReferenceId = PangoUtility.generateIdAsString();
        User user = new User();
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
        // String propertyUnitId=PangoUtility.generateIdAsString();
        // propertyUnit.setPropertyUnitId(propertyUnitId);
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

        return propertyUnit;
    }
}
