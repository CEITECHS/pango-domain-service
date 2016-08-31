package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ceitechs.domain.service.domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import org.springframework.util.Assert;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PropertyHoldingHistoryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private PropertyHoldingHistoryRepository propertyHoldingHistoryRepository;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private UserRepository userRepository;



    @Autowired
    private GridFsService gridFsService;

    private String propertyId;

    private String propertyUnitId;

    private String userReferenceId;

    private String unitHoldingHistoryId;

    private String ownerReferenceId;

    private PropertyHoldingHistory savedUnitHoldingHistory;

    @Before
    public void setUp() {
        // Delete all unit holding rentingHistory
        propertyHoldingHistoryRepository.deleteAll();

        PropertyHoldingHistory unitHoldingHistory = new PropertyHoldingHistory();

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

        // Create a new property unit
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnitId = PangoUtility.generateIdAsString();
        propertyUnit.setPropertyId(propertyUnitId);

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
        // Save property unit
        propertyUnitRepository.save(propertyUnit);

        // Create a new holding rentingHistory
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

        // save unit holding rentingHistory
        savedUnitHoldingHistory = propertyHoldingHistoryRepository.save(unitHoldingHistory);
    }

    @Test
    public void testSaveUnitHoldingHistory() {
        assertNotNull("The saved unit holding rentingHistory should not be null", savedUnitHoldingHistory);
        assertEquals("The unit holding Id should match", unitHoldingHistoryId,
                savedUnitHoldingHistory.getHoldingReferenceId());
    }

    @Test
    public void testGetUnitHoldingHistoryByUser() {
        // Get all the unit holding rentingHistory for the user
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);
        Page<PropertyHoldingHistory> results = propertyHoldingHistoryRepository.findByUserOrderByStartDateDesc(newUser,
                new PageRequest(0, 10));
        assertNotNull("The returned unit holding rentingHistory should not be null", results);
        assertThat("The returned unit holding rentingHistory should match the expected list", results.getContent(),
                hasSize(1));
    }

    @Test
    public void testGetUnitHoldingHistoryByOwner() {
        // Get all the unit holding rentingHistory for the owners property
        Optional<List<PropertyHoldingHistory>> results = propertyHoldingHistoryRepository
                .getUnitHoldingHistory(ownerReferenceId);
        assertNotNull("The returned unit holding rentingHistory should not be null", results);
        assertThat("The returned unit holding rentingHistory should match the expected list", results.get(), hasSize(1));
        assertEquals("The owner id should from the results should match the expected owner id", ownerReferenceId,
                results.get().get(0).getPropertyUnit().getOwner().getUserReferenceId());
    }

    @Test
    public void testGetUnitHoldingHistoryByOwnerWithNoProperty() {
        // Get all the unit holding rentingHistory for the owners property
        String ownerId = PangoUtility.generateIdAsString();
        Optional<List<PropertyHoldingHistory>> results = propertyHoldingHistoryRepository.getUnitHoldingHistory(ownerId);
        assertNotNull("The returned unit holding rentingHistory should be null", results);
        assertFalse("The returned unit holding rentingHistory should match the expected", results.isPresent());
    }

    @Test
    public void findByPropertyAndPhaseNotInTest(){
        PropertyUnit unit = propertyUnitRepository.findByPropertyIdAndActiveTrue(propertyUnitId);
        Assert.notNull(unit, "unit can not be null");
        PropertyHoldingHistory propertyHoldingHistory = propertyHoldingHistoryRepository.findByPropertyUnitAndPhaseNotInOrderByCreatedDateDesc(unit, Arrays.asList(PropertyHoldingHistory.HoldingPhase.CANCELLED, PropertyHoldingHistory.HoldingPhase.EXPIRED));
        assertNotNull("The saved unit holding rentingHistory should not be null", propertyHoldingHistory);
        assertEquals("The unit holding Id should match", unitHoldingHistoryId, propertyHoldingHistory.getHoldingReferenceId());

    }

    @Test
    public void findByPropertyAndPhaseNotInTest2(){
        PropertyUnit unit = propertyUnitRepository.findByPropertyIdAndActiveTrue(propertyUnitId);
        Assert.notNull(unit, "unit can not be null");
        PropertyHoldingHistory propertyHoldingHistory = propertyHoldingHistoryRepository.findByPropertyUnitAndPhaseNotInOrderByCreatedDateDesc(unit, Arrays.asList(PropertyHoldingHistory.HoldingPhase.CANCELLED, PropertyHoldingHistory.HoldingPhase.INITIATED, PropertyHoldingHistory.HoldingPhase.EXPIRED));
        assertNull("The saved unit holding rentingHistory should not be null", propertyHoldingHistory);
       // assertNotEquals("The unit holding Id should match", unitHoldingHistoryId, propertyHoldingHistory.getHoldingReferenceId());

    }
    @After
    public void tearDown() {
    }
}
