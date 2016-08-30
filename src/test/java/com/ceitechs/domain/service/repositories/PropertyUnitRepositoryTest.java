package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
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

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();

        propertyUnitId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyId(propertyUnitId);

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
        features.setPropertySize(1200.0);
        propertyUnit.setFeatures(features);

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
            attachment.setFileDescription("An Amazing PropertyRemoved Unit");
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
        assertEquals("The propertyId should match", propertyUnitId, savedPropertyUnit.getPropertyId());
    }

    @Test
    public void findActivePropertyTest(){
        PropertyUnit unitById = propertyUnitRepository.findByPropertyIdAndActiveTrue(savedPropertyUnit.getPropertyId());
        assertNotNull("The saved property unit should not be null", unitById);
        assertEquals("The propertyId should match", propertyUnitId, unitById.getPropertyId());
    }

    @Test
    public void bedRoomsWithinPriceAndDistanceTest(){
        randomProperties();

        PropertySearchCriteria searchCriteria = searchCriteriaResource();
        searchCriteria.setBedRoomsCount(4);
        searchCriteria.setMinPrice(3200);

        GeoResults<PropertyUnit> resultsList = propertyUnitRepository.findAllPropertyUnits(searchCriteria);
        assertNotNull(resultsList);
        assertTrue(!resultsList.getContent().isEmpty());
        System.out.println(resultsList.getAverageDistance());
        resultsList.getContent().stream().forEach(g-> {
            assertTrue(g.getDistance().getValue() <= searchCriteria.getRadius());
            assertTrue(g.getContent().getRent().getAmount() >= searchCriteria.getMinPrice());
            System.out.println(g.getContent().getPropertyUnitDesc() + " --- " + g.getDistance() + "--- " + g.getContent().getRent().getAmount() + " "+ g.getContent().getRent().getPeriodforAmount() );
        });
    }

    @Test
    public void studioWithinPriceAndDistanceTest(){
        randomProperties();
        PropertySearchCriteria searchCriteria = searchCriteriaResource();
        searchCriteria.setBedRoomsCount(0.5);
        searchCriteria.setAmenities("gym,SWIMMING POOL");
        GeoResults<PropertyUnit> resultsList = propertyUnitRepository.findAllPropertyUnits(searchCriteria);
        assertNotNull(resultsList);
        assertTrue(!resultsList.getContent().isEmpty());
        System.out.println(resultsList.getAverageDistance());
        resultsList.getContent().stream().forEach(g-> {
            assertTrue(g.getDistance().getValue() <= searchCriteria.getRadius());
            assertTrue(g.getContent().getRent().getAmount() >= searchCriteria.getMinPrice());
            assertTrue(g.getContent().getFeatures().isStudio());
            System.out.println(g.getContent().getPropertyUnitDesc() + " --- " + g.getDistance() + "--- " + g.getContent().getRent().getAmount() + " "+ g.getContent().getRent().getPeriodforAmount() );

        });
    }

    @Test
    public void officeWithinPriceAndDistanceTest(){
        randomProperties();
        PropertySearchCriteria searchCriteria = searchCriteriaResource();
        searchCriteria.setPropertyPupose(PropertyPurpose.BUSINESS.name());
        searchCriteria.setRoomsCount(1);
        searchCriteria.setMinPrice(1000);
        searchCriteria.setMaxPrice(0);
        searchCriteria.setBathCount(1);
        GeoResults<PropertyUnit> resultsList = propertyUnitRepository.findAllPropertyUnits(searchCriteria);
        assertNotNull(resultsList);
        assertTrue(!resultsList.getContent().isEmpty());
        System.out.println(resultsList.getAverageDistance());
        resultsList.getContent().stream().forEach(g-> {
            assertTrue(g.getDistance().getValue() <= searchCriteria.getRadius());
            assertTrue(g.getContent().getRent().getAmount() >= searchCriteria.getMinPrice());
            System.out.println(g.getContent().getPropertyUnitDesc() + " --- " + g.getDistance() + "--- " + g.getContent().getRent().getAmount() + " "+ g.getContent().getRent().getPeriodforAmount() );

        });
    }


    public PropertySearchCriteria searchCriteriaResource(){
        PropertySearchCriteria searchCriteria = new PropertySearchCriteria();
        searchCriteria.setPropertyPupose(PropertyPurpose.HOME.name());
        searchCriteria.setLatitude(-6.662951);
        searchCriteria.setLongitude(39.166650);
        searchCriteria.setMoveInDateAsString("2018-07-25");
        searchCriteria.setRadius(50);
        searchCriteria.setRoomsCount(4);
        searchCriteria.setBedRoomsCount(2);
        searchCriteria.setBathCount(1);
        searchCriteria.setMinPrice(2200);
        searchCriteria.setMaxPrice(2200);
        return searchCriteria;
    }


    public void randomProperties(){
        propertyUnitRepository.deleteAll();

        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnitRepository.save(propertyUnit);
        IntStream.range(0,6).forEach(i -> {
            PropertyFeature features = new PropertyFeature();
            features.setPropertySize(1200.0); 

            PropertyRent rent = new PropertyRent();
            rent.setCurrency("TZS");
            rent.setPeriodforAmount(PerPeriod.MONTHLY);
            if(i%2 == 0) {
                rent.setAmount(1200 +(i*1000) );
                propertyUnit.setLocation(new double[]{39.229809, -6.769280});
                propertyUnit.setPurpose(PropertyPurpose.BUSINESS);
                features.setNbrOfBaths(2);
                features.setNbrOfRooms(i+1);
                propertyUnit.setPropertyUnitDesc(String.format(" A nice %d rooms office", features.getNbrOfRooms()));
                propertyUnit.setFeatures(features);
                propertyUnit.setRent(rent);
            }else{
                rent.setAmount(1200 +(i*1000) );
                propertyUnit.setLocation(new double[]{39.288764,-6.808039});
                propertyUnit.setPurpose(PropertyPurpose.HOME);
                features.setNbrOfBedRooms(i+1);
                if(i == 1) {
                    features.setStudio(true);
                    features.setNbrOfBedRooms(0);
                    propertyUnit.setPropertyUnitDesc("Studio apartment ");
                    Amenity amenity = new Amenity();
                    amenity.setAvailable(false);
                    amenity.setName("GYM");

                    Amenity amenity1 = new Amenity();
                    amenity1.setAvailable(true);
                    amenity1.setName("SWIMMING POOL");
                    features.getAdditionalFeatures().addAll(Arrays.asList(amenity,amenity1));
                }
                else {
                    features.setNbrOfRooms(i+1);
                    propertyUnit.setPropertyUnitDesc(String.format(" Amazing %d bed rooms apartment", features.getNbrOfBedRooms()));
                }
                features.setNbrOfBaths(2);
                propertyUnit.setFeatures(features);
                propertyUnit.setRent(rent);

            }

            propertyUnit.setPropertyId(PangoUtility.generateIdAsString());
            propertyUnit.setPropertyUnitDesc(propertyUnit.getPropertyUnitDesc() + i);
            propertyUnitRepository.save(propertyUnit);
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

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnitId=PangoUtility.generateIdAsString();
        propertyUnit.setPropertyId(propertyUnitId);
        propertyUnit.setPropertyUnitDesc("Amazing 2 bedrooms appartment");

        // Adding listing
        propertyUnit.setListingFor(ListingFor.RENT);

        // Adding property purpose
        propertyUnit.setPurpose(PropertyPurpose.HOME);

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

        // Adding property unit image

        return propertyUnit;
    }

    @After
    public void tearDown() {

    }
}
