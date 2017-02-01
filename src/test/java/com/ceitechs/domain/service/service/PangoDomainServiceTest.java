package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private GridFsOperations operations;

    @Autowired
    PropertyUnitRepository unitRepository;

    @Autowired
    @Lazy(true)
    PangoDomainService domainService;

    @Value("${user.verification.uri}")
    private String verificationUri;

    @Test
    public void createPropertyTest() throws IOException {
        operations.delete(null);
        unitRepository.deleteAll();
        userRepository.deleteAll();

        PropertyUnit unit = createPropertyUnit();
        User usr = unit.getOwner();

        Optional<PropertyUnit> propertyUnitOptional = domainService.createProperty(unit, usr);
        assertTrue(propertyUnitOptional.isPresent());
       // FileMetadata meta = new FileMetadata();
       // meta.setReferenceId(propertyUnitOptional.get().getPropertyId());
       // GridFSDBFile file = gridFsService.getProfilePicture(meta, ReferenceIdFor.PROPERTY);
        //assertNotNull(file);
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
       Optional<PropertyUnit> propertyUnit = domainService.retrievePropertyBy(propertyUnitOptional.get().getPropertyId(),usr);
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
       // String propertyId=PangoUtility.generateIdAsString();
       // propertyUnit.setPropertyId(propertyId);
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
        propertyUnit.setAddress(address);

        // Adding property unit image
        //propertyUnit.getAttachments().add(buildAttachment());

        return propertyUnit;
    }

    private static AttachmentOld buildAttachment() throws IOException {
        AttachmentOld attachment = new AttachmentOld();
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
        user.setFirstName("Iddy");
        user.setLastName("Magohe");
        user.setEmailAddress("iddy85@gmail.com");
        UserProfile userProfile = new UserProfile();
        userProfile.setPassword("someStrongPassword");
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
    public void userRegistrationTest() throws EntityExists, InterruptedException {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);

        assertTrue(savedUsr.isPresent());
        assertEquals("firstName must match",usr.getFirstName(),savedUsr.get().getFirstName());
        assertEquals("lastName must match",usr.getLastName(),savedUsr.get().getLastName());
        Thread.sleep(1500);

    }

    @Test
    public void verifyAccountByTokenTest() throws EntityExists, Exception {
        userRepository.deleteAll();
        User usr = createUser();
        usr.getProfile().getRoles().add(PangoUserRole.USER);
        Optional<UserProjection> savedUsr = domainService.registerUser(usr);
        assertTrue(savedUsr.isPresent());
        User savedWithCode = userRepository.findOne(savedUsr.get().getUserReferenceId());
        assertNotNull(savedWithCode);
        assertNotNull(savedWithCode.getProfile().getVerificationCode());
        assertFalse(savedWithCode.getProfile().isVerified());
        Optional<UserProjection> verifiedUser = domainService.verifyUserAccountBy(savedUsr.get().getVerificationPathParam().replace(verificationUri,""));
        System.out.println(savedUsr.get().getVerificationPathParam());
        assertTrue(verifiedUser.isPresent());
        User verifiedWithCode = userRepository.findOne(verifiedUser.get().getUserReferenceId());
        assertTrue(verifiedWithCode.getProfile().isVerified());
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
    public void updateUserFavoritePropertiesTest() throws IOException, EntityNotFound {
        userRepository.deleteAll();
        unitRepository.deleteAll();
        User user = createUser();
        user.setFirstName(RandomStringUtils.randomAlphabetic(10));
        user.setLastName(RandomStringUtils.randomAlphabetic(10));
        user.setEmailAddress(user.getFirstName()+'@'+ user.getLastName());
        userRepository.save(user);
        List<String> prtIds = new ArrayList<>();

        PropertyUnit propertyUnit = createPropertyUnit();
        propertyUnit.setPropertyId(PangoUtility.generateIdAsString());
        propertyUnit.setPropertyUnitDesc(RandomStringUtils.randomAlphabetic(20));
        unitRepository.save(propertyUnit);

        assertThat(user.getFavouredProperties(), hasSize(0));
        domainService.updateUserFavoriteProperties(user,propertyUnit.getPropertyId(),true);
        User savedUsr = userRepository.findOne(user.getUserReferenceId());
        assertThat(savedUsr.getFavouredProperties(), hasSize(1));
        domainService.updateUserFavoriteProperties(user,propertyUnit.getPropertyId(),false);
        savedUsr = userRepository.findOne(user.getUserReferenceId());
        assertThat(savedUsr.getFavouredProperties(), hasSize(0));


        IntStream.range(0,3).forEach(i -> {
            PropertyUnit unit = new PropertyUnit();
            unit.setPropertyId(PangoUtility.generateIdAsString() + i);
            unitRepository.save(unit);
            prtIds.add(unit.getPropertyId());

            try {
                domainService.updateUserFavoriteProperties(user,unit.getPropertyId(),true);
            } catch (EntityNotFound entityNotFound) {
                entityNotFound.printStackTrace();
            }
        });

        savedUsr = userRepository.findOne(user.getUserReferenceId());
        assertThat(savedUsr.getFavouredProperties(), IsCollectionWithSize.hasSize(3));

        propertyUnit.setPropertyId(prtIds.get(0));
        domainService.updateUserFavoriteProperties(user,propertyUnit.getPropertyId(),false);

        savedUsr = userRepository.findOne(user.getUserReferenceId());
        assertThat(savedUsr.getFavouredProperties(), IsCollectionWithSize.hasSize(2));

    }

    @Test
    public void retrieveFavoritePropertiesByUserTest() throws EntityNotFound {
        userRepository.deleteAll();
        unitRepository.deleteAll();

        User user = createUser();
        user.setFirstName(RandomStringUtils.randomAlphabetic(10));
        user.setLastName(RandomStringUtils.randomAlphabetic(10));
        user.setEmailAddress(user.getFirstName()+'@'+ user.getLastName());
        userRepository.save(user);

        IntStream.range(0,5).forEach(i -> {
            PropertyUnit unit = null;
            try {
                unit = createPropertyUnit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            unit.setPropertyId("");
           domainService.createProperty(unit,unit.getOwner());

            try {
                domainService.updateUserFavoriteProperties(user,unit.getPropertyId(),true);
            } catch (EntityNotFound entityNotFound) {
                entityNotFound.printStackTrace();
            }
        });
        user.setLatitude(-6.662951);
        user.setLongitude(39.166650);
        List<PropertyProjection> favouredProps = domainService.retrieveFavoritePropertiesBy(user);
        assertThat("A size of five is expected", favouredProps,hasSize(5));
        favouredProps.forEach(g ->{
            System.out.println(g.getDistance() + " - " +g.getPropertyUnitDesc());
            System.out.println(g.getCoverPhoto());
        });
    }

}
