package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.NearQuery;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.domain.FileMetadata.FILETYPE;
import com.ceitechs.domain.service.domain.ListingFor;
import com.ceitechs.domain.service.domain.PerPeriod;
import com.ceitechs.domain.service.domain.Property;
import com.ceitechs.domain.service.domain.PropertyFeature;
import com.ceitechs.domain.service.domain.PropertyRent;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PangoDomainServicePropertyRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyResource = new ClassPathResource("property.jpg");

    @Autowired
    private PangoDomainServicePropertyRepository propertyRepository;

    @Autowired
    private PangoDomainServiceUserRepository userRepository;

    @Autowired
    private PangoDomainServicePropertyUnitRepository propertyUnitRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsService gridFsService;

    private String propertyId;

    private String userReferenceId;

    private Property savedProperty;

    @Before
    public void setUp() {
        // Delete all the existing properties
        propertyRepository.deleteAll();

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

        // Adding listing
        property.setListingFor(ListingFor.RENT);

        // Adding location
        double[] location = {77.45678, 77.45678};
        property.setLocation(location);

        // Adding address
        property.setAddress(address);

        // Adding the owner details
        property.setOwner(user);

        // Adding property feature
        PropertyFeature features = new PropertyFeature();
        features.setPropertySize("1200 SFT");
        property.setFeatures(features);

        // Adding property rent
        PropertyRent rent = new PropertyRent();
        rent.setAmount(1200);
        rent.setCurrency("USD");
        rent.setPeriodforAmount(PerPeriod.MONTHLY);
        property.setRent(rent);

        // Adding property unit

        // Adding property image
        try {
            Attachment attachment = new Attachment();
            attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            attachment.setFileName(propertyResource.getFilename());
            attachment.setFileSize(propertyResource.getFile().length());
            attachment.setFileDescription("An Amazing Property");
            Map<String, String> metadata = PangoUtility.attachmentMetadataToMap(propertyId, ReferenceIdFor.PROPERTY,
                    attachment);
            gridFsService.storeFiles(propertyResource.getInputStream(), metadata, BasicDBObject::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        savedProperty = propertyRepository.save(property);
    }

    @Test
    public void testSaveProperty() {
        assertNotNull("The saved property should not be null", savedProperty);
        assertEquals("The propertyId should match", propertyId, savedProperty.getPropertyId());
    }

    @Test
    public void testGetPropertyByLocation() {
        Point location = new Point(77.45678, 77.45678);
        NearQuery query = NearQuery.near(location).maxDistance(new Distance(1, Metrics.MILES));
        GeoResults<Property> properties = mongoTemplate.geoNear(query, Property.class);
        assertThat("The returned properties list should match the expected list", properties.getContent(), hasSize(1));
    }

    @Test
    public void testGetPropertyByOwner() {
        Page<Property> propertiesList = propertyRepository.findByOwnerOrderByCreatedDateDesc(userReferenceId,
                new PageRequest(0, 10));
        assertThat("The returned properties list should not be null", propertiesList, notNullValue());
        assertThat("The returned properties list should match the expected list", propertiesList.getContent(),
                hasSize(1));
    }

    @Test
    public void testGetPropertyByListing() {
        Page<Property> propertiesList = propertyRepository.findByListingForOrderByCreatedDateDesc(ListingFor.RENT,
                new PageRequest(0, 10));
        assertThat("The returned properties list should not be null", propertiesList, notNullValue());
        assertThat("The returned properties list should match the expected list", propertiesList.getContent(),
                hasSize(1));
    }

    @Test
    public void testGetPropertyImages() {
        FileMetadata searchCriteria = new FileMetadata();
        searchCriteria.setReferenceId(propertyId);
        searchCriteria.setFileType(FILETYPE.PHOTO.name());
        List<GridFSDBFile> results = gridFsService.getAllAttachments(searchCriteria, ReferenceIdFor.PROPERTY);
        assertThat("The expected image count should match the returned image count", results, hasSize(1));
    }

    @Test
    public void testDeletePropertyImages() {
        gridFsService.deleteAttachment("property.jpg", propertyId, ReferenceIdFor.PROPERTY);
        FileMetadata searchCriteria = new FileMetadata();
        searchCriteria.setReferenceId(propertyId);
        searchCriteria.setFileType(FILETYPE.PHOTO.name());
        List<GridFSDBFile> results = gridFsService.getAllAttachments(searchCriteria, ReferenceIdFor.PROPERTY);
        assertThat("The expected image count should match the returned image count", results, hasSize(0));
    }

    @Test
    public void testUpdatePropertyImages() {
        // TODO - Do we need this? 
    }

    @After
    public void tearDown() {
        userRepository.delete(userReferenceId);
        propertyRepository.delete(propertyId);
    }
}
