package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
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

    @Test
    public void searchPropertyTest() throws IOException {
        operations.delete(null);
        unitRepository.deleteAll();
        userRepository.deleteAll();

        PropertyUnit unit = createPropertyUnit();
        User usr = unit.getOwner();

        Optional<PropertyUnit> propertyUnitOptional = domainService.createProperty(unit, usr);
        assertTrue(propertyUnitOptional.isPresent());

        PropertySearchCriteria searchCriteria= new PropertySearchCriteria();
        searchCriteria.setMinPrice(1000);
        searchCriteria.setPropertyPupose(unit.getPurpose().name());
        searchCriteria.setBedRoomsCount(3);
        searchCriteria.setLatitude(-6.662951);
        searchCriteria.setLongitude(39.166650);
        searchCriteria.setRadius(50);
        searchCriteria.setMoveInDateAsString("2018-05-05");
        List<GeoResult<PropertyUnit>> geoResults = domainService.searchForProperties(searchCriteria,usr);
        assertThat("A size of one is expected", geoResults,hasSize(1));
        geoResults.forEach(g ->{
            System.out.println(g.getDistance() + " - " +g.getContent());
            System.out.println(g.getContent().getCoverPhoto());
        });
    }

    @Test
    public void searchPropertyByReferenceIdTest() throws IOException {
        operations.delete(null);
        unitRepository.deleteAll();
        userRepository.deleteAll();

        PropertyUnit unit = createPropertyUnit();
        User usr = unit.getOwner();

        Optional<PropertyUnit> propertyUnitOptional = domainService.createProperty(unit, usr);
        assertTrue(propertyUnitOptional.isPresent());
       Optional<PropertyUnit> propertyUnit = domainService.retrievePropertyBy(propertyUnitOptional.get().getPropertyUnitId(),usr);
        assertTrue(propertyUnit.isPresent());
        assertThat("Attachments", propertyUnit.get().getAttachments(),hasSize(unit.getAttachments().size()));
        System.out.println(propertyUnit.get());
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

    @Test
    public void userPreferencesInteractionsTest(){
        userRepository.deleteAll();
        User user = createUser();
        User savedOne =  userRepository.save(user);
        List<Integer> actives = new ArrayList<>();
        IntStream.range(0,15).forEach(i -> {
            UserPreference userPreference = createUserPreference();
            userPreference.setFromDate(LocalDate.now().plusDays(i));
            userPreference.setToDate(userPreference.getFromDate().plusDays(15));
            int random = PangoUtility.random(0,9);
            if(i < 3) {
                userPreference.setActive(random % 2 == 0);
            }else{
                userPreference.setActive(false);
            }

            if(userPreference.isActive()) actives.add(1);

            userPreference.setSendNotification(true);
            userPreference.setUserSearchHistory(new UserSearchHistory());
            userRepository.addUserPreferences(userPreference,savedOne);
        });

        //retrieve
        List<UserPreference> preferences = domainService.retrievePreferencesByUserId(savedOne.getUserReferenceId());
        assertTrue(!preferences.isEmpty());
        assertThat("Active Preferences", preferences.stream().filter(UserPreference::isActive).collect(toList()), IsCollectionWithSize.hasSize(actives.size()));
        preferences.forEach(System.out::println);
        UserPreference oneActive =  preferences.stream().filter(UserPreference::isActive).findAny().get();
        System.out.println(oneActive);

        //remove test
        assertTrue(domainService.removeUserPreferenceBy(oneActive.getPreferenceId(),user).isPresent());
        List<UserPreference> preferencesz = domainService.retrievePreferencesByUserId(savedOne.getUserReferenceId());
        assertTrue(!preferencesz.isEmpty());
        assertThat("Active Preferences", preferencesz.stream().filter(UserPreference::isActive).collect(toList()), IsCollectionWithSize.hasSize(actives.size() - 1));

        //Update
        UserPreference oneInActive =  preferences.stream().filter(userPreference -> !userPreference.isActive()).findFirst().get();
        System.out.println(oneInActive);
        oneInActive.setActive(true);
        oneInActive.setToDate(oneInActive.getToDate().plusDays(10));
        oneInActive.setUserSearchHistory(new UserSearchHistory(new PropertySearchCriteria(),0));

        assertTrue(domainService.updateUserPreference(oneInActive,user).isPresent());
        List<UserPreference> preferencesy = domainService.retrievePreferencesByUserId(savedOne.getUserReferenceId());
        assertTrue(!preferencesy.isEmpty());
        assertThat("Active Preferences", preferencesy.stream().filter(UserPreference::isActive).collect(toList()), IsCollectionWithSize.hasSize(actives.size()));


    }

    static User createUser(){
        User user = new User();
        user.setUserReferenceId(PangoUtility.generateIdAsString());
        user.setFirstName("iam");
        user.setLastName("magohe");
        user.setEmailAddress("iam.magohe@pango.com");
        UserProfile userProfile = new UserProfile();
        userProfile.setPassword("123456");
        userProfile.setVerified(true);
        user.setProfile(userProfile);
        return user;
    }

    static UserPreference createUserPreference(){
        UserPreference userPreference = new UserPreference();
        userPreference.setPreferenceType(UserPreference.PreferenceType.Notification);
        userPreference.setCategory(UserPreference.PreferenceCategory.USERSET);
        return  userPreference;
    }


    @Test
    public void userRegistrationTest() throws EntityExists {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);
        assertTrue(savedUsr.isPresent());
        assertEquals("firstName must match",usr.getFirstName(),savedUsr.get().getFirstName());
        assertEquals("lastName must match",usr.getLastName(),savedUsr.get().getLastName());

    }

    @Test
    public void userRegistrationTestExists() throws EntityExists {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);

        usr.setUserReferenceId(savedUsr.get().getUserReferenceId());
        assertThatThrownBy(() -> domainService.registerUser(usr))
                .isInstanceOf(EntityExists.class)
                .hasMessage(String.format("User with Email : %s  address exists",usr.getEmailAddress()))
                .hasCauseInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void updateBasicInfoTest() throws EntityExists {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        assertNull(usr.getAddress());
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);
        usr.setUserReferenceId(savedUsr.get().getUserReferenceId());
        usr.setEmailAddress("updated.emailAddress@pango.com");
        Address address = new Address();
        address.setAddressLine1("10000 Palace VCT");
        address.setCity("Richmond");
        address.setCountry("US");
        usr.setAddress(address);

        Optional<UserProjection> updatedUser = domainService.updateUserInformation(usr,UserUpdating.BASIC_INFO);
        assertTrue(updatedUser.isPresent());
        assertNotNull(updatedUser.get().getAddress());
    }

    @Test
    public void updateUserPasswordTest() throws EntityExists {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        assertNull(usr.getAddress());
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);
        usr.setUserReferenceId(savedUsr.get().getUserReferenceId());
        usr.getProfile().setPassword("updated password");
        assertTrue(domainService.updateUserInformation(usr,UserUpdating.PASSWORD_CHANGE).isPresent());
        assertEquals("password must match",usr.getProfile().getPassword(),userRepository.findOne(usr.getUserReferenceId()).getProfile().getPassword());


    }

    @Test
    public void updateUserProfilePictureTest() throws EntityExists, IOException {
        operations.delete(null);
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        assertNull(usr.getAddress());
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);
        usr.setUserReferenceId(savedUsr.get().getUserReferenceId());
        Attachment profilePicture = buildAttachment();
        profilePicture.setFileDescription("profile picture");
        usr.getProfile().setProfilePicture(profilePicture);
        assertTrue(domainService.updateUserInformation(usr,UserUpdating.PROFILE_PICTURE).isPresent());
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setReferenceId(usr.getUserReferenceId());
        fileMetadata.setFileType(FileMetadata.FILETYPE.PHOTO.name());
        GridFSDBFile profilePic = gridFsService.getProfilePicture(fileMetadata,ReferenceIdFor.USER);
        assertNotNull(profilePic);
        FileMetadata fileMetadata1 = FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(profilePic),ReferenceIdFor.USER);
        assertEquals(profilePicture.getFileDescription(),fileMetadata1.getCaption());
        usr.getProfile().getProfilePicture().setFileDescription("Updated profile picture");
        assertTrue(domainService.updateUserInformation(usr,UserUpdating.PROFILE_PICTURE).isPresent());
        FileMetadata fileMetadata2 = FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(gridFsService.getProfilePicture(fileMetadata,ReferenceIdFor.USER)),ReferenceIdFor.USER);
        assertEquals(profilePicture.getFileDescription(),fileMetadata2.getCaption());

        //test retrieval with associated user profile picture
        Optional<UserProjection> userProjection = domainService.retrieveUserByIdOrUserName("",usr.getEmailAddress());
        assertTrue(userProjection.isPresent());
        assertNotNull(userProjection.get().getProfilePicture());
        assertEquals(fileMetadata2.getCaption(), userProjection.get().getProfilePicture().getFileDescription());

        Optional<UserProjection> userProjection2 = domainService.retrieveUserByIdOrUserName(usr.getUserReferenceId(),"");
        assertTrue(userProjection2.isPresent());
        assertNotNull(userProjection2.get().getProfilePicture());
        assertEquals(userProjection.get().getProfilePicture().getFileDescription(),userProjection2.get().getProfilePicture().getFileDescription());
        assertEquals(userProjection.get().getEmailAddress(),userProjection2.get().getEmailAddress());
    }

}
