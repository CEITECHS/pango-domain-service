package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.ceitechs.domain.service.domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PropertyUnitRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsService gridFsService;

    private String propertyUnitId;

    private String propertyId;

    private String userReferenceId;

    private PropertyUnit savedPropertyUnit;

    @Before
    public void setUp() {
        // Delete all the existing properties units
        propertyUnitRepository.deleteAll();

        userReferenceId = PangoUtility.generateIdAsString();
        User user = new User();
        user.setUserReferenceId(userReferenceId);

        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        address.setAddressLine2("Address Line 2");
        address.setCity("City");
        address.setState("State");
        address.setZip("12345");
        user.setAddress(address);

        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        // Save the user
        userRepository.save(user);

        propertyId = PangoUtility.generateIdAsString();
        Property property = new Property();
        property.setPropertyId(propertyId);
        property.setPropertyDesc("nice property");

        // Save the property
        propertyRepository.save(property);

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        property.setPropertyDesc("nice property unit");
        propertyUnitId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyUnitId(propertyUnitId);

        // Adding listing
        propertyUnit.setListingFor(ListingFor.RENT);

        // Adding property purpose
        propertyUnit.setPurpose(PropertyPurpose.HOME);

        // Adding location
        double[] location = {77.45678, 77.45678};
        propertyUnit.setLocation(location);

        // Adding the owner details
        propertyUnit.setOwner(user);

        // Adding property feature
        PropertyFeature features = new PropertyFeature();
        features.setPropertySize("1200 SFT");
        propertyUnit.setFeatures(features);

        // Adding property reference
        propertyUnit.setProperty(property);

        // Adding property rent
        PropertyRent rent = new PropertyRent();
        rent.setAmount(1200);
        rent.setCurrency("USD");
        rent.setPeriodforAmount(PerPeriod.MONTHLY);
        propertyUnit.setRent(rent);

        // Adding property unit image
        try {
            Attachment attachment = new Attachment();
            attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            attachment.setFileName(propertyUnitResource.getFilename());
            attachment.setFileSize(propertyUnitResource.getFile().length());
            attachment.setFileDescription("An Amazing Property Unit");
            Map<String, String> metadata = PangoUtility.attachmentMetadataToMap(propertyUnitId,
                    ReferenceIdFor.UNIT_PROPERTY, attachment, propertyId);
            gridFsService.storeFiles(propertyUnitResource.getInputStream(), metadata, BasicDBObject::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        savedPropertyUnit = propertyUnitRepository.save(propertyUnit);
    }

    @Test
    public void testSavePropertyUnit() {
        assertNotNull("The saved property unit should not be null", savedPropertyUnit);
        assertEquals("The propertyUnitId should match", propertyUnitId, savedPropertyUnit.getPropertyUnitId());
    }

    @Test
    public void testFindBySearchCriteriaTest(){
        propertyUnitRepository.deleteAll();

        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnitRepository.save(propertyUnit);
        IntStream.range(0,6).forEach(i ->{
            if(i%2 ==0)
                propertyUnit.setLocation(new double[]{-6.769280,39.229809});
            else{
                propertyUnit.setLocation(new double[]{-6.808039,39.288764});
            }

            propertyUnit.setPropertyUnitId(PangoUtility.generateIdAsString());
            propertyUnit.setPropertyUnitDesc(propertyUnit.getPropertyUnitDesc() + i);
            propertyUnitRepository.save(propertyUnit);
        });

        PropertySearchCriteria searchCriteria = new PropertySearchCriteria();
        searchCriteria.setPropertyPupose(PropertyPurpose.HOME.name());
        searchCriteria.setLatitude(-6.662951);
        searchCriteria.setLongitude(39.166650);
        searchCriteria.setMoveInDateAsString("2016-07-25");
        searchCriteria.setRadius(15);
        searchCriteria.setRoomsCount(4);
        searchCriteria.setBedRoomsCount(2);
        searchCriteria.setBathCount(2);
        GeoResults<PropertyUnit> resultsList = propertyUnitRepository.findAllPropertyUnits(searchCriteria);
        assertNotNull(resultsList);
        assertTrue(!resultsList.getContent().isEmpty());
        System.out.println(resultsList.getAverageDistance());
        resultsList.getContent().stream().forEach(g-> {
           assertTrue(g.getDistance().getValue() <= searchCriteria.getRadius());
            System.out.println(g.getContent().getPropertyUnitDesc() + " --- " + g.getDistance());
        });
    }



    public  PropertyUnit createPropertyUnit(){
        userReferenceId = PangoUtility.generateIdAsString();
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

        propertyId = PangoUtility.generateIdAsString();
        Property property = new Property();
        property.setPropertyId(propertyId);
        property.setPropertyDesc("nice property");

        // Save the property
        propertyRepository.save(property);

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        property.setPropertyDesc("nice property unit");
        propertyUnitId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyUnitId(propertyUnitId);
        propertyUnit.setPropertyUnitDesc("Amazing 2 bedrooms appartment");

        // Adding listing
        propertyUnit.setListingFor(ListingFor.RENT);

        // Adding property purpose
        propertyUnit.setPurpose(PropertyPurpose.HOME);

        // Adding location(long,lat)
        double[] location = {-6.816064, 39.272271};
        propertyUnit.setLocation(location);

        // Adding the owner details
        propertyUnit.setOwner(user);

        // Adding property feature
        PropertyFeature features = new PropertyFeature();
        features.setPropertySize("1200 SFT"); //TODO Should not be a String
        features.setNbrOfBaths(2);
        features.setNbrOfBedRooms(3);
        features.setNbrOfRooms(5);
        propertyUnit.setFeatures(features);

        propertyUnit.setNextAvailableDate(LocalDateTime.now().plusDays(3));


        // Adding property reference
        propertyUnit.setProperty(property);

        // Adding property rent
        PropertyRent rent = new PropertyRent();
        rent.setAmount(1200);
        rent.setCurrency("USD");
        rent.setPeriodforAmount(PerPeriod.MONTHLY);
        propertyUnit.setRent(rent);

        // Adding property unit image

        return propertyUnit;
    }

    @After
    public void tearDown() {

    }
}
