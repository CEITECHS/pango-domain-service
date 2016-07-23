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
import com.ceitechs.domain.service.domain.PropertyRemoved;
import com.ceitechs.domain.service.domain.PropertyFeature;
import com.ceitechs.domain.service.domain.PropertyRent;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyUnit.PropertyPurpose;
import com.ceitechs.domain.service.domain.TransactionType;
import com.ceitechs.domain.service.domain.UnitRentalHistory;
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
public class UnitRentalHistoryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    private Resource propertyUnitResource = new ClassPathResource("propertyUnit.jpg");

    @Autowired
    private UnitRentalHistoryRepository unitRentalHistoryRepository;

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

    private String unitRentalHistoryId;

    private String ownerReferenceId;

    private UnitRentalHistory savedUnitRentalHistory;

    @Before
    public void setUp() {
        // Delete all unit rental history
        unitRentalHistoryRepository.deleteAll();

        UnitRentalHistory unitRentalHistory = new UnitRentalHistory();

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

        // Create a property
        propertyId = PangoUtility.generateIdAsString();
        PropertyRemoved property = new PropertyRemoved();
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

        // Create a new rental history
        unitRentalHistoryId = PangoUtility.generateIdAsString();
        unitRentalHistory.setRentalReferenceId(unitRentalHistoryId);
        unitRentalHistory.setUser(user);
        unitRentalHistory.setPropertyUnit(propertyUnit);

        // Rental on a property would be for 12 months
        unitRentalHistory.setStartDate(LocalDate.now());
        unitRentalHistory.setEndDate(LocalDate.now().plusMonths(12));

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
        unitRentalHistory.setPendingPayments(rentalPaymentsList);

        // User Transaction History
        List<UserTransactionHistory> transactionHistoryList = new ArrayList<>();
        UserTransactionHistory transactionHistory = new UserTransactionHistory();
        transactionHistory.setTransactionType(TransactionType.PAYMENT);
        transactionHistory.setTransactionAmount(1200.0);
        transactionHistory.setTransactionDate(LocalDate.now());
        transactionHistory.setTransactionDesc("First Payment");
        transactionHistory.setUser(user);
        transactionHistoryList.add(transactionHistory);
        unitRentalHistory.setTransactionHistory(transactionHistoryList);

        // Rental History active flag
        unitRentalHistory.setActive(true);

        // save unit holding history
        savedUnitRentalHistory = unitRentalHistoryRepository.save(unitRentalHistory);
    }

    @Test
    public void testSaveUnitRentalHistory() {
        assertNotNull("The saved unit rental history should not be null", savedUnitRentalHistory);
        assertEquals("The unit rental Id should match", unitRentalHistoryId,
                savedUnitRentalHistory.getRentalReferenceId());
    }

    @Test
    public void testGetUnitRentalHistoryByUser() {
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);
        Page<UnitRentalHistory> results = unitRentalHistoryRepository.findByUserOrderByIsActiveDescStartDateDesc(newUser,
                new PageRequest(0, 10));
        assertNotNull("The returned unit rental history should not be null", results);
        assertThat("The returned unit rental history should match the expected list", results.getContent(), hasSize(1));
    }

    @Test
    public void testGetUnitRentalHistoryByPropertyUnit() {
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnit.setPropertyUnitId(propertyUnitId);
        Page<UnitRentalHistory> results = unitRentalHistoryRepository
                .findByPropertyUnitOrderByIsActiveDescStartDateDesc(propertyUnit, new PageRequest(0, 10));
        assertNotNull("The returned unit rental history should not be null", results);
        assertThat("The returned unit rental history should match the expected list", results.getContent(), hasSize(1));
        assertEquals("The property unit id from the results should match the expected property unit id", propertyUnitId,
                results.getContent().get(0).getPropertyUnit().getPropertyUnitId());
    }

    @Test
    public void testGetUnitRentalHistoryByPropertyUnitOrderByActive() {
        createNewRentalHistory();
        PropertyUnit propertyUnit = new PropertyUnit();
        propertyUnit.setPropertyUnitId(propertyUnitId);
        Page<UnitRentalHistory> results = unitRentalHistoryRepository
                .findByPropertyUnitOrderByIsActiveDescStartDateDesc(propertyUnit, new PageRequest(0, 10));
        assertNotNull("The returned unit rental history should not be null", results);
        assertThat("The returned unit rental history should match the expected list", results.getContent(), hasSize(2));
        assertTrue("The active property should be first", results.getContent().get(0).isActive());
    }

    private void createNewRentalHistory() {
        UnitRentalHistory unitRentalHistory = new UnitRentalHistory();
        User newUser = new User();
        newUser.setUserReferenceId(userReferenceId);

        PropertyUnit newPropertyUnit = new PropertyUnit();
        newPropertyUnit.setPropertyUnitId(propertyUnitId);

        // Create a new rental history
        String newunitRentalHistoryId = PangoUtility.generateIdAsString();
        unitRentalHistory.setRentalReferenceId(newunitRentalHistoryId);
        unitRentalHistory.setUser(newUser);
        unitRentalHistory.setPropertyUnit(newPropertyUnit);

        // Rental on a property would be for 12 months
        unitRentalHistory.setStartDate(LocalDate.now());
        unitRentalHistory.setEndDate(LocalDate.now().plusMonths(12));

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
        unitRentalHistory.setPendingPayments(rentalPaymentsList);

        // User Transaction History
        List<UserTransactionHistory> transactionHistoryList = new ArrayList<>();
        UserTransactionHistory transactionHistory = new UserTransactionHistory();
        transactionHistory.setTransactionType(TransactionType.PAYMENT);
        transactionHistory.setTransactionAmount(1200.0);
        transactionHistory.setTransactionDate(LocalDate.now());
        transactionHistory.setTransactionDesc("First Payment");
        transactionHistory.setUser(newUser);
        transactionHistoryList.add(transactionHistory);
        unitRentalHistory.setTransactionHistory(transactionHistoryList);

        // Rental History active flag
        unitRentalHistory.setActive(false);

        // save unit holding history
        unitRentalHistoryRepository.save(unitRentalHistory);
    }

    @After
    public void tearDown() {
    }

}
