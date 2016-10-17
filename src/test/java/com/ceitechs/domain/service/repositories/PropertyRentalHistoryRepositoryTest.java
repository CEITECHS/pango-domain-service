package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.domain.PropertyRentalHistory;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PropertyRentalHistoryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private UnitRentalHistoryRepository unitRentalHistoryRepository;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GridFsService gridFsService;

    private String propertyId;

    private String propertyUnitId;

    private String userReferenceId;

    private String unitRentalHistoryId;

    private String ownerReferenceId;

    private PropertyRentalHistory savedPropertyRentalHistory;

    @Before
    public void setUp() {
        // Delete all unit rental rentingHistory
        unitRentalHistoryRepository.deleteAll();

        PropertyRentalHistory propertyRentalHistory = new PropertyRentalHistory();

        // Create an Address
        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        address.setAddressLine2("Address Line 2");
        address.setCity("City");
        address.setState("State");
        address.setZip("12345");

        // Create a user
        userReferenceId = PangoUtility.generateIdAsString();
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");
        // set the address
        user.setAddress(address);
        // Save the user
        userRepository.save(user);

        // Create a owner
        ownerReferenceId = PangoUtility.generateIdAsString();
        User owner = new User();
        owner.setUserReferenceId(ownerReferenceId);
        // set the address
        owner.setAddress(address);
        owner.setFirstName("Owner");
        owner.setLastName("Owner");
        owner.setEmailAddress("owner.owner@pango.com");
        // Save the owner
        userRepository.save(owner);


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

        // Create a new rental rentingHistory
        unitRentalHistoryId = PangoUtility.generateIdAsString();
        propertyRentalHistory.setRentalReferenceId(unitRentalHistoryId);
        propertyRentalHistory.setUser(user);
        propertyRentalHistory.setPropertyUnit(propertyUnit);

        // Rental on a property would be for 12 months
        propertyRentalHistory.setStartDate(LocalDate.now());
        propertyRentalHistory.setEndDate(LocalDate.now().plusMonths(12));

        // Rental Pending Payments
        List<PendingPayment> rentalPaymentsList = new ArrayList<>();
        IntStream.range(0, 12).forEach(i -> {
            PendingPayment rentalPayment = new PendingPayment();
            rentalPayment.setCurrencyType("TSH");
            rentalPayment.setPaymentAmount(1200.0);
            rentalPayment.setStartDate(LocalDate.now().plusMonths(i));
            rentalPayment.setEndDate(LocalDate.now().plusMonths(i + 1));
            rentalPaymentsList.add(rentalPayment);
        });
        propertyRentalHistory.setPendingPayments(rentalPaymentsList);

        // User Transaction History
        List<UserTransactionHistory> transactionHistoryList = new ArrayList<>();
        UserTransactionHistory transactionHistory = new UserTransactionHistory();
        transactionHistory.setTransactionType(TransactionType.PAYMENT);
        transactionHistory.setTransactionAmount(1200.0);
        transactionHistory.setTransactionDate(LocalDate.now());
        transactionHistory.setTransactionDesc("First Payment");
        transactionHistory.setUser(user);
        transactionHistoryList.add(transactionHistory);
        propertyRentalHistory.setTransactionHistory(transactionHistoryList);

        // Rental History active flag
        propertyRentalHistory.setActive(true);

        // save unit holding rentingHistory
        savedPropertyRentalHistory = unitRentalHistoryRepository.save(propertyRentalHistory);
    }

    @Test
    public void testSaveUnitRentalHistory() {
        assertNotNull("The saved unit rental rentingHistory should not be null", savedPropertyRentalHistory);
        assertEquals("The unit rental Id should match", unitRentalHistoryId,
                savedPropertyRentalHistory.getRentalReferenceId());
    }

    @Test
    public void testGetUnitRentalHistoryByUser() {
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);
        Page<PropertyRentalHistory> results = unitRentalHistoryRepository.findByUserOrderByIsActiveDescStartDateDesc(newUser,
                new PageRequest(0, 10));
        assertNotNull("The returned unit rental rentingHistory should not be null", results);
        assertThat("The returned unit rental rentingHistory should match the expected list", results.getContent(), hasSize(1));
    }

    @Test
    public void testGetUnitRentalHistoryByPropertyUnit() {
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnit.setPropertyId(propertyUnitId);
        Page<PropertyRentalHistory> results = unitRentalHistoryRepository
                .findByPropertyUnitOrderByIsActiveDescStartDateDesc(propertyUnit, new PageRequest(0, 10));
        assertNotNull("The returned unit rental rentingHistory should not be null", results);
        assertThat("The returned unit rental rentingHistory should match the expected list", results.getContent(), hasSize(1));
        assertEquals("The property unit id from the results should match the expected property unit id", propertyUnitId,
                results.getContent().get(0).getPropertyUnit().getPropertyId());
    }

    @Test
    public void testGetUnitRentalHistoryByPropertyUnitOrderByActive() {
        createNewRentalHistory();
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnit.setPropertyId(propertyUnitId);
        Page<PropertyRentalHistory> results = unitRentalHistoryRepository
                .findByPropertyUnitOrderByIsActiveDescStartDateDesc(propertyUnit, new PageRequest(0, 10));
        assertNotNull("The returned unit rental rentingHistory should not be null", results);
        assertThat("The returned unit rental rentingHistory should match the expected list", results.getContent(), hasSize(2));
        assertTrue("The active property should be first", results.getContent().get(0).isActive());
    }

    private void createNewRentalHistory() {
        PropertyRentalHistory propertyRentalHistory = new PropertyRentalHistory();
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);

        PropertyUnit newPropertyUnit = new PropertyUnit();
        newPropertyUnit.setPropertyId(propertyUnitId);

        // Create a new rental rentingHistory
        String newunitRentalHistoryId = PangoUtility.generateIdAsString();
        propertyRentalHistory.setRentalReferenceId(newunitRentalHistoryId);
        propertyRentalHistory.setUser(newUser);
        propertyRentalHistory.setPropertyUnit(newPropertyUnit);

        // Rental on a property would be for 12 months
        propertyRentalHistory.setStartDate(LocalDate.now());
        propertyRentalHistory.setEndDate(LocalDate.now().plusMonths(12));

        // Rental Pending Payments
        List<PendingPayment> rentalPaymentsList = new ArrayList<>();
        IntStream.range(0, 12).forEach(i -> {
            PendingPayment rentalPayment = new PendingPayment();
            rentalPayment.setCurrencyType("TSH");
            rentalPayment.setPaymentAmount(1200.0);
            rentalPayment.setStartDate(LocalDate.now().plusMonths(i));
            rentalPayment.setEndDate(LocalDate.now().plusMonths(i + 1));
            rentalPaymentsList.add(rentalPayment);
        });
        propertyRentalHistory.setPendingPayments(rentalPaymentsList);

        // User Transaction History
        List<UserTransactionHistory> transactionHistoryList = new ArrayList<>();
        UserTransactionHistory transactionHistory = new UserTransactionHistory();
        transactionHistory.setTransactionType(TransactionType.PAYMENT);
        transactionHistory.setTransactionAmount(1200.0);
        transactionHistory.setTransactionDate(LocalDate.now());
        transactionHistory.setTransactionDesc("First Payment");
        transactionHistory.setUser(newUser);
        transactionHistoryList.add(transactionHistory);
        propertyRentalHistory.setTransactionHistory(transactionHistoryList);

        // Rental History active flag
        propertyRentalHistory.setActive(false);

        // save unit holding rentingHistory
        unitRentalHistoryRepository.save(propertyRentalHistory);
    }

    @After
    public void tearDown() {
    }

}
