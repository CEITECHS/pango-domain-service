package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.domain.ListingFor;
import com.ceitechs.domain.service.domain.PendingPayment;
import com.ceitechs.domain.service.domain.PerPeriod;
import com.ceitechs.domain.service.domain.Property;
import com.ceitechs.domain.service.domain.PropertyFeature;
import com.ceitechs.domain.service.domain.PropertyRent;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.domain.TransactionType;
import com.ceitechs.domain.service.domain.UnitHoldingHistory;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserTransactionHistory;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class UnitHoldingHistoryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private UnitHoldingHistoryRepository unitHoldingHistoryRepository;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private GridFsService gridFsService;

    private String propertyId;

    private String propertyUnitId;

    private String userReferenceId;

    private String unitHoldingHistoryId;

    private String ownerReferenceId;

    private UnitHoldingHistory savedUnitHoldingHistory;

    @Before
    public void setUp() {
        // Delete all unit holding history
        unitHoldingHistoryRepository.deleteAll();

        UnitHoldingHistory unitHoldingHistory = new UnitHoldingHistory();

        // Create a user
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

        // Create a owner
        ownerReferenceId = PangoUtility.generateIdAsString();
        User owner = new User();
        owner.setUserReferenceId(ownerReferenceId);
        owner.setAddress(address);

        owner.setFirstName("Owner");
        owner.setLastName("Owner");
        owner.setEmailAddress("owner.owner@pango.com");

        // Save the owner
        userRepository.save(owner);

        // Create a property
        propertyId = PangoUtility.generateIdAsString();
        Property property = new Property();
        property.setPropertyId(propertyId);
        property.setPropertyDesc("nice property");

        // Save the property
        propertyRepository.save(property);

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        property.setPropertyDesc("nice property unit");
        propertyUnitId = PangoUtility.generateIdAsString();
        propertyUnit.setPropertyUnitId(propertyUnitId);

        // Adding listing
        propertyUnit.setListingFor(ListingFor.RENT);

        // Adding property purpose
        propertyUnit.setPurpose(PropertyPurpose.HOME);

        // Adding location
        double[] location = {77.45678, 77.45678};
        propertyUnit.setLocation(location);

        // Adding the owner details
        propertyUnit.setOwner(owner);

        // Adding property feature
        PropertyFeature features = new PropertyFeature();
        features.setPropertySize(1200.0);
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
        // Save property unit
        propertyUnitRepository.save(propertyUnit);

        // Create a new holding history
        unitHoldingHistoryId = PangoUtility.generateIdAsString();
        unitHoldingHistory.setHoldingReferenceId(unitHoldingHistoryId);
        unitHoldingHistory.setUser(user);
        unitHoldingHistory.setPropertyUnit(propertyUnit);

        // Holding on a property would be for 48 hours
        unitHoldingHistory.setStartDate(LocalDateTime.now());
        unitHoldingHistory.setEndDate(LocalDateTime.now().plusDays(2));

        // Holding Payment
        PendingPayment holdingPayment = new PendingPayment();
        unitHoldingHistory.setHoldingPayment(holdingPayment);

        // User Transaction History
        UserTransactionHistory transactionHistory = new UserTransactionHistory();
        transactionHistory.setTransactionType(TransactionType.HOLDING);
        unitHoldingHistory.setTransactionHistory(transactionHistory);

        // save unit holding history
        savedUnitHoldingHistory = unitHoldingHistoryRepository.save(unitHoldingHistory);
    }

    @Test
    public void testSaveUnitHoldingHistory() {
        assertNotNull("The saved unit holding history should not be null", savedUnitHoldingHistory);
        assertEquals("The unit holding Id should match", unitHoldingHistoryId,
                savedUnitHoldingHistory.getHoldingReferenceId());
    }

    @Test
    public void testGetUnitHoldingHistoryByUser() {
        // Get all the unit holding history for the user
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);
        Page<UnitHoldingHistory> results = unitHoldingHistoryRepository.findByUserOrderByStartDateDesc(newUser,
                new PageRequest(0, 10));
        assertNotNull("The returned unit holding history should not be null", results);
        assertThat("The returned unit holding history should match the expected list", results.getContent(),
                hasSize(1));
    }

    @Test
    public void testGetUnitHoldingHistoryByOwner() {
        // Get all the unit holding history for the owners property
        Optional<List<UnitHoldingHistory>> results = unitHoldingHistoryRepository
                .getUnitHoldingHistory(ownerReferenceId);
        assertNotNull("The returned unit holding history should not be null", results);
        assertThat("The returned unit holding history should match the expected list", results.get(), hasSize(1));
        assertEquals("The owner id should from the results should match the expected owner id", ownerReferenceId,
                results.get().get(0).getPropertyUnit().getOwner().getUserReferenceId());
    }

    @Test
    public void testGetUnitHoldingHistoryByOwnerWithNoProperty() {
        // Get all the unit holding history for the owners property
        String ownerId = PangoUtility.generateIdAsString();
        Optional<List<UnitHoldingHistory>> results = unitHoldingHistoryRepository.getUnitHoldingHistory(ownerId);
        assertNotNull("The returned unit holding history should be null", results);
        assertFalse("The returned unit holding history should match the expected", results.isPresent());
    }

    @After
    public void tearDown() {
    }
}
