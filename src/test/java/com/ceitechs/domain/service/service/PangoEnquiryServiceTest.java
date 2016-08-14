package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * @author  by iddymagohe on 8/10/16.
 */
public class PangoEnquiryServiceTest  extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired PangoEnquiryService enquiryService;
    @Autowired UserRepository userRepository;
    @Autowired PropertyUnitRepository propertyUnitRepository;
    @Autowired PropertyUnitEnquiryRepository enquiryRepository;
    @Autowired GridFsService gridFsService;
    @Autowired
    private GridFsOperations operations;




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

       Optional<EnquiryProjection> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);
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

        Optional<EnquiryProjection> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);
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

        Optional<EnquiryProjection> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,propertyUnit.getPropertyUnitId(),enquiry);


    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnquiryCorrespondenceTestWithAttachments() throws IOException, EntityExists, EntityNotFound {

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
        enquiry.setEnquiryType(CorrespondenceType.REQUEST_INFO);

        Optional<EnquiryProjection> propertyUnitEnquiry =  enquiryService.createUserEnquiryToProperty(usr,savedPrt.getPropertyUnitId(),enquiry);
        assertTrue(propertyUnitEnquiry.isPresent());
        assertEquals(enquiry.getMessage(),propertyUnitEnquiry.get().getMessage());
        assertThat("Collection size should be one", propertyUnitEnquiry.get().getCorrespondences(), hasSize(0));

        EnquiryCorrespondence correspondence = new EnquiryCorrespondence();
        correspondence.setMessage("Sure property is availbale waiting for you");
        correspondence.setCorrespondenceType(CorrespondenceType.SITE_VISIT);
        enquiryService.addEnquiryCorrespondence(usr,propertyUnitEnquiry.get().getEnquiryReferenceId(),correspondence);
        PropertyUnitEnquiry savedWithCorrespondence = enquiryRepository.findOne(propertyUnitEnquiry.get().getEnquiryReferenceId());
        assertThat("Collection size should be one", savedWithCorrespondence.getCorrespondences(), hasSize(1));

        operations.delete(null);

        EnquiryCorrespondence correspondence2 = new EnquiryCorrespondence();
        correspondence2.setMessage("I thought I should pass along a few more pics");
        correspondence2.setCorrespondenceType(CorrespondenceType.REQUEST_INFO);
        correspondence2.setAttachment(buildAttachment());

        assertTrue(enquiryService.addEnquiryCorrespondence(usr,propertyUnitEnquiry.get().getEnquiryReferenceId(),correspondence2).isPresent());
        PropertyUnitEnquiry savedWithCorrespondenceAtt = enquiryRepository.findOne(propertyUnitEnquiry.get().getEnquiryReferenceId());
        assertThat("Collection size should be one", savedWithCorrespondenceAtt.getCorrespondences(), hasSize(2));


        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileType(FileMetadata.FILETYPE.DOCUMENT.name());
        fileMetadata.setReferenceId(savedWithCorrespondence.getEnquiryReferenceId() +"-"+ savedWithCorrespondenceAtt.getCorrespondences().stream().filter(correspondence1 -> correspondence1.getMessage().equals("I thought I should pass along a few more pics")).findFirst().get().getCorrespondenceReferenceId());
        List<GridFSDBFile> attachments = gridFsService.getAllAttachments(fileMetadata, ReferenceIdFor.ENQUIRY);
        assertFalse(attachments.isEmpty());

        FileMetadata fetchedData = FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(attachments.get(0)),ReferenceIdFor.ENQUIRY);
        assertEquals(fileMetadata.getReferenceId(),fetchedData.getReferenceId());
        assertEquals(savedWithCorrespondenceAtt.getPropertyUnit().getPropertyUnitId(),fetchedData.getParentReferenceId());

        //unrelated user to update add correspondence
        //throws IllegalArgumentException
        enquiryService.addEnquiryCorrespondence(createLoggedInUser(),propertyUnitEnquiry.get().getEnquiryReferenceId(),correspondence2);


    }

    @Test
    public void  retrieveEnquiriesByPropsectiveTenantTest() throws EntityNotFound {
        Map<User, List<EnquiryProjection>> savedProperties = createSampleSearchData();
        List<User> usrs = savedProperties.keySet().stream().collect(Collectors.toList());
        List<EnquiryProjection> enquiryProjection = enquiryService.retrieveEnquiriesBy(usrs.get(0),0);
        assertThat(enquiryProjection,hasSize(savedProperties.get(usrs.get(0)).size()));
        enquiryProjection.forEach(enquiryProjection1 -> assertEquals(usrs.get(0),enquiryProjection1.getProspectiveTenant()));
    }

    @Test
    public void retrieveEnquiriesByOwnerOfApropertyTest(){
        Map<User, List<EnquiryProjection>> savedProperties = createSampleSearchData();

        User owner = savedProperties.values().stream().findFirst().get().get(0).getPropertyUnit().getOwner();

        List<EnquiryProjection> enquiryProjectionListbyOwner = savedProperties.values().stream()
                .flatMap(l->l.stream()).filter(enquiryProjection -> enquiryProjection.getPropertyUnit().getOwner().equals(owner))
                .collect(Collectors.toList());
        List<EnquiryProjection> enquiryProjectionList = enquiryService.retrieveEnquiriesBy(owner, Optional.empty(),100);
        assertThat(enquiryProjectionList,hasSize(enquiryProjectionListbyOwner.size()));
        enquiryProjectionList.forEach(enquiryProjection -> assertEquals(owner,enquiryProjection.getPropertyUnit().getOwner()));
    }

    @Test
    public void retrieveEnquiriesByOwnerOfPropertiesTest(){
        Map<User, List<EnquiryProjection>> savedProperties = createSampleSearchData();
        PropertyProjection unit = savedProperties.values().stream().findFirst().get().get(0).getPropertyUnit();

        List<EnquiryProjection> enquiryProjectionListbyOwner = savedProperties.values().stream()
                .flatMap(l->l.stream()).filter(enquiryProjection -> enquiryProjection.getPropertyUnit().getPropertyUnitId().equals(unit.getPropertyUnitId()))
                .collect(Collectors.toList());
        List<EnquiryProjection> enquiryProjectionList = enquiryService.retrieveEnquiriesBy(unit.getOwner(), Optional.of(unit.getPropertyUnitId()),100);
        assertThat(enquiryProjectionList,hasSize(enquiryProjectionListbyOwner.size()));
        enquiryProjectionList.forEach(enquiryProjection -> assertEquals(unit.getPropertyUnitId(),enquiryProjection.getPropertyUnit().getPropertyUnitId()));
    }

    @Test
    public void retrieveCorrespondenceAttachmentTest(){
        Map<User, List<EnquiryProjection>> savedProperties = createSampleSearchData();

        List<User> usrs = savedProperties.keySet().stream().collect(Collectors.toList());

        List<EnquiryProjection> enquiryProjectionListbyUsr = savedProperties.values().stream()
                .flatMap(l->l.stream()).filter(enquiryProjection -> enquiryProjection.getProspectiveTenant().equals(usrs.get(0)))
                .collect(Collectors.toList());

        Optional<EnquiryProjection> optional = enquiryService.retrieveEnquiryBy(usrs.get(0),enquiryProjectionListbyUsr.get(0).getEnquiryReferenceId());
        assertTrue(optional.isPresent());
        Optional<Attachment> attachment = enquiryService.retrieveCorrespondenceAttachmentBy(usrs.get(0),optional.get().getCorrespondences().get(0).getAttachmentId());
        assertTrue(attachment.isPresent());
        System.out.println(attachment);

    }

    private Map<User, List<EnquiryProjection>> createSampleSearchData(){
        Map<User, List<EnquiryProjection>> userListMap = new HashMap<>();
        userRepository.deleteAll();
        propertyUnitRepository.deleteAll();
        enquiryRepository.deleteAll();
        operations.delete(null);

        List<User> prospectiveTenants = new ArrayList<>();
        List<PropertyUnit> propertyUnits = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> {
            User user = createLoggedInUser();
            user.setFirstName(RandomStringUtils.random(10));
            user.setLastName(RandomStringUtils.random(10));
            user.setEmailAddress(user.getFirstName() + '@' + user.getLastName());
            prospectiveTenants.add(user);

            try {
                PropertyUnit propertyUnit = createPropertyUnit();
                propertyUnit.setPropertyUnitDesc(RandomStringUtils.randomAlphabetic(30));
                Address address= new Address();
                address.setAddressLine1(RandomStringUtils.random(10));
                address.setCountry("TZ");
                address.setCity("Dar es Salaam");
                propertyUnit.setAddress(address);
                propertyUnits.add(propertyUnit);
            } catch (IOException e) {
                //Ignore attachment
            }
        });
        List<User>  savedUsers = userRepository.save(prospectiveTenants); // saved users
        List<PropertyUnit> propertyUnitList = propertyUnitRepository.save(propertyUnits);

        // create a bunch of Enquiries.

        IntStream.range(0,10).forEach(i -> {

            propertyUnitList.forEach(propertyUnit -> {
                PropertyUnitEnquiry enquiry= new PropertyUnitEnquiry();
                //enquiry.setProspectiveTenant(savedUsers.get(i));
                //enquiry.setPropertyUnit(propertyUnitList.get(i));
                enquiry.setMessage(RandomStringUtils.randomAlphanumeric(100));
                enquiry.setEnquiryType(CorrespondenceType.INTERESTED);
                User usr = savedUsers.get(i);

                try {
                    Optional<EnquiryProjection> enquiryProjection = enquiryService.createUserEnquiryToProperty(usr,propertyUnit.getPropertyUnitId(),enquiry);
                    EnquiryCorrespondence correspondence2 = new EnquiryCorrespondence();
                    correspondence2.setMessage("I thought I should pass along a few more pics");
                    correspondence2.setCorrespondenceType(CorrespondenceType.REQUEST_INFO);
                    correspondence2.setAttachment(buildAttachment());
                    enquiryService.addEnquiryCorrespondence(usr,enquiryProjection.get().getEnquiryReferenceId(),correspondence2);
                    if (userListMap.containsKey(usr)){
                        userListMap.get(usr).add(enquiryProjection.get());

                    }else{
                        List<EnquiryProjection> enquiryProjectionList = new ArrayList<>();
                        enquiryProjectionList.add(enquiryProjection.get());
                        userListMap.put(usr, enquiryProjectionList);
                    }
                } catch (EntityExists | IOException entityExists) {
                    entityExists.printStackTrace();
                } catch (EntityNotFound entityNotFound) {
                    entityNotFound.printStackTrace();
                }
            });

        });





        return userListMap;
    }

    private static Attachment buildAttachment() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setFileType(FileMetadata.FILETYPE.DOCUMENT.name());
        attachment.setFileName(resource.getFilename());
        attachment.setFileSize(resource.getFile().length());
        attachment.setFileDescription("attachement requested");
        attachment.setProfilePicture(false);
        attachment.setContentBase64(PangoUtility.InputStreamToBase64(Optional.of(resource.getInputStream()),attachment.extractExtension()).get());
        return attachment;
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

        UserProfile userProfile = new UserProfile();
        userProfile.setPassword("123456");
        userProfile.setVerified(true);
        user.setProfile(userProfile);


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
