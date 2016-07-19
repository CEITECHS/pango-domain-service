package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.domain.ListingFor;
import com.ceitechs.domain.service.domain.PerPeriod;
import com.ceitechs.domain.service.domain.Property;
import com.ceitechs.domain.service.domain.PropertyFeature;
import com.ceitechs.domain.service.domain.PropertyRent;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PangoDomainServicePropertyUnitRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private PangoDomainServicePropertyUnitRepository propertyUnitRepository;

    @Autowired
    private PangoDomainServicePropertyRepository propertyRepository;

    @Autowired
    private PangoDomainServiceUserRepository userRepository;

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

    @After
    public void tearDown() {

    }
}
