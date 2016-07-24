package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PangoDomainServiceTest extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired
    UserRepository userRepository;

    @Autowired
    private GridFsService gridFsService;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    PropertyUnitRepository unitRepository;

    @Autowired
    @Lazy(true)
    PangoDomainService domainService;

    @Test
    public void createPropertyTest() throws IOException {
        operations.delete(null);
        unitRepository.deleteAll();
        userRepository.deleteAll();

        PropertyUnit unit = createPropertyUnit();
        User usr = unit.getOwner();

        Optional<PropertyUnit> propertyUnitOptional = domainService.createProperty(unit, usr);
        assertTrue(propertyUnitOptional.isPresent());
        FileMetadata meta = new FileMetadata();
        meta.setReferenceId(propertyUnitOptional.get().getPropertyUnitId());
        GridFSDBFile file = gridFsService.getProfilePicture(meta, ReferenceIdFor.PROPERTY);
        assertNotNull(file);

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

        // Adding property unit image
        propertyUnit.getAttachments().add(buildAttachment());

        return propertyUnit;
    }

    private static Attachment buildAttachment() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
        attachment.setFileName(resource.getFilename());
        attachment.setFileSize(resource.getFile().length());
        attachment.setFileDescription("Cover photo");
        attachment.setProfilePicture(true);
        attachment.setContentBase64(PangoUtility.InputStreamToBase64(Optional.of(resource.getInputStream()),attachment.extractExtension()).get());
        return attachment;
    }

}
