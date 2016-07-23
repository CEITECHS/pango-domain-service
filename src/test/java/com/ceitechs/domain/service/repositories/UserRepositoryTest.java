package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserPreference;
import com.ceitechs.domain.service.domain.UserPreference.PreferenceCategory;
import com.ceitechs.domain.service.domain.UserPreference.PreferenceType;
import com.ceitechs.domain.service.domain.UserProfile;
import com.ceitechs.domain.service.domain.UserSearchHistory;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.DateConvertUtility;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class UserRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    GridFsService gridFsService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String userReferenceId;

    @BeforeClass
    public static void initialize() {
    }

    @Before
    public void setUp() {
        userReferenceId = PangoUtility.generateIdAsString();
        // Delete all the users to make sure our test bed is clean
        userRepository.deleteAll();
    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        User savedUser = userRepository.save(user);
        assertEquals("The expected userId should be same as the userId we set", userReferenceId,
                savedUser.getUserReferenceId());
    }

    @Test
    public void testSaveUserForSize() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        userRepository.save(user);
        List<User> userList = new ArrayList<>();
        Iterable<User> userIterable = userRepository.findAll(Arrays.asList(userReferenceId));

        userIterable.forEach(userList::add);
        assertThat("There should only be one user with the given userReferenceId", userList, hasSize(1));
    }

    @Test
    public void testSaveUserWithoutAddress() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        User savedUser = userRepository.save(user);
        assertThat("The address object should be null", savedUser.getAddress(), nullValue());
    }

    @Test
    public void testSaveUserWithAddress() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        address.setAddressLine2("Address Line 2");
        address.setCity("City");
        address.setState("State");
        address.setZip("12345");

        user.setAddress(address);

        User savedUser = userRepository.save(user);
        assertThat("The address object should not be null", savedUser.getAddress(), notNullValue());
    }

    @Test
    public void testUpdateAddress() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        address.setAddressLine2("Address Line 2");
        address.setCity("City");
        address.setState("State");
        address.setZip("12345");

        user.setAddress(address);

        User savedUser = userRepository.save(user);
        Address newAddress = new Address();
        newAddress.setAddressLine1("New Address Line 1");
        newAddress.setAddressLine2("New Address Line 2");
        newAddress.setCity("New City");
        newAddress.setState("New State");
        newAddress.setZip("54321");

        savedUser.setAddress(newAddress);
        User newSavedUser = userRepository.save(savedUser);

        assertThat("The address object should not be null", newSavedUser.getAddress(), notNullValue());
        assertThat("The returned address should be same as the updated address",
                (newSavedUser.getAddress().equals(newAddress)));
    }

    @Test
    public void testSaveUserProfile() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        address.setAddressLine2("Address Line 2");
        address.setCity("City");
        address.setState("State");
        address.setZip("12345");
        user.setAddress(address);

        UserProfile userProfile = new UserProfile();
        userProfile.setPassword("password");
        user.setProfile(userProfile);

        User savedUser = userRepository.save(user);
        assertThat("The returned user profile should be same as the updated user profile",
                savedUser.getProfile().equals(userProfile));
    }

    @Test
    public void testSaveUserPreferences() {

        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        String preferenceId1 = PangoUtility.generateIdAsString();
        String preferenceId2 = PangoUtility.generateIdAsString();

        UserPreference userPreference1 = new UserPreference();
        userPreference1.setPreferenceId(preferenceId1);
        userPreference1.setPreferenceType(PreferenceType.Notification);
        userPreference1.setActive(true);
        userPreference1.setCategory(PreferenceCategory.SEARCH);

        UserPreference userPreference2 = new UserPreference();
        userPreference2.setPreferenceId(preferenceId2);
        userPreference2.setPreferenceType(PreferenceType.Notification);
        userPreference2.setCategory(PreferenceCategory.SEARCH);

        List<UserPreference> preferencesList = new ArrayList<>();
        preferencesList.add(userPreference1);
        preferencesList.add(userPreference2);

        user.setPreferences(preferencesList);

        User savedUser = userRepository.save(user);

        assertThat("The returned preferences list shoud match the expected list", savedUser.getPreferences(),
                hasSize(2));
    }

    @Test
    public void testDeleteUserPreferences() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        String preferenceId1 = PangoUtility.generateIdAsString();
        String preferenceId2 = PangoUtility.generateIdAsString();

        UserPreference userPreference1 = new UserPreference();
        userPreference1.setPreferenceId(preferenceId1);
        userPreference1.setPreferenceType(PreferenceType.Notification);
        userPreference1.setActive(true);
        userPreference1.setCategory(PreferenceCategory.SEARCH);

        UserPreference userPreference2 = new UserPreference();
        userPreference2.setPreferenceId(preferenceId2);
        userPreference2.setPreferenceType(PreferenceType.Notification);
        userPreference2.setCategory(PreferenceCategory.SEARCH);

        List<UserPreference> preferencesList = new ArrayList<>();
        preferencesList.add(userPreference1);
        preferencesList.add(userPreference2);

        user.setPreferences(preferencesList);

        User savedUser = userRepository.save(user);
        Update update = new Update().pull("preferences", Query.query(Criteria.where("preferenceId").is(preferenceId1)));

        // delete user preference
        mongoTemplate.updateMulti(Query.query(Criteria.where("_id").is(savedUser.getUserReferenceId())), update,
                User.class);

        User newUser = userRepository.findOne(userReferenceId);
        assertThat("The returned preferences list shoud match the expected list", newUser.getPreferences(), hasSize(1));
    }

    @Test
    public void testUpdateUserPreferences() {
        userRepository.deleteAll();
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        String preferenceId1 = PangoUtility.generateIdAsString();
        String preferenceId2 = PangoUtility.generateIdAsString();

        UserPreference userPreference1 = new UserPreference();
        userPreference1.setPreferenceId(preferenceId1);
        userPreference1.setPreferenceType(PreferenceType.Notification);
        userPreference1.setActive(true);
        userPreference1.setCategory(PreferenceCategory.SEARCH);

        UserPreference userPreference2 = new UserPreference();
        userPreference2.setPreferenceId(preferenceId2);
        userPreference2.setPreferenceType(PreferenceType.Notification);
        userPreference2.setCategory(PreferenceCategory.SEARCH);

        List<UserPreference> preferencesList = new ArrayList<>();
        preferencesList.add(userPreference1);
        preferencesList.add(userPreference2);

        user.setPreferences(preferencesList);

        User savedUser = userRepository.save(user);

        List<UserPreference> newpreferencesList = savedUser.getPreferences();
        newpreferencesList.forEach(userPref -> {
            if (userPref.getPreferenceId().equals(preferenceId1)) {
                userPref.setActive(false);
            }
        });

        User updatedUser = userRepository.save(savedUser);

        UserPreference userPreference3 = new UserPreference();
        userPreference3.setPreferenceId(preferenceId2);
        userPreference3.setPreferenceType(PreferenceType.Notification);
        userPreference3.setCategory(PreferenceCategory.USERSET);

        userRepository.addUserPreferences(userPreference3, updatedUser);

        User usr = userRepository.findOne(updatedUser.getUserReferenceId());

        assertThat("The returned preferences list shoud match the expected list", usr.getPreferences(),
                hasSize(3));
    }



   // @Test
    public void testSaveSearchHistory() {
        User user = new User();
        user.setUserReferenceId(userReferenceId);
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");

        String preferenceId1 = PangoUtility.generateIdAsString();

        UserPreference userPreference1 = new UserPreference();
        userPreference1.setPreferenceId(preferenceId1);
        userPreference1.setPreferenceType(PreferenceType.Notification);
        userPreference1.setActive(true);
        userPreference1.setCategory(PreferenceCategory.SEARCH);

       // UserSearchHistory searchHistory1 = new UserSearchHistory("Query1", 1);
       // searchHistory1.setDate(DateConvertUtility.asLocalDateTime(new Date()));
      //  searchHistory1.setQuery("Query1");

      //  userPreference1.setUserSearchHistory(searchHistory1);

        List<UserPreference> preferencesList = new ArrayList<>();
        preferencesList.add(userPreference1);

        user.setPreferences(preferencesList);
        User savedUser = userRepository.save(user);

        assertThat("The returned search history shoud not be null",
                savedUser.getPreferences().get(0).getUserSearchHistory(), notNullValue());
    }

    @Test
    public void findAllUser() {
        List<User> userList = userRepository.findAll();
        assertNotNull(userList);
    }

    @Test
    public void saveImage() {
        try {
            Attachment attachment = new Attachment();
            attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            attachment.setFileName(resource.getFilename());
            attachment.setFileSize(resource.getFile().length());
            attachment.setFileDescription("profile_picture");
            Map<String, String> metadata = PangoUtility.attachmentMetadataToMap("1", ReferenceIdFor.USER, attachment,
                    "");
            gridFsService.storeFiles(resource.getInputStream(), metadata, BasicDBObject::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
