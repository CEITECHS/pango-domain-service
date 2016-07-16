package com.ceitechs.domain.service.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.util.PangoUtility;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PangoDomainServiceUserRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    private PangoDomainServiceUserRepository userRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    GridFsService gridFsService;

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
    @Ignore
    public void findAllUser() {
        List<User> userList = userRepository.findAll();
        assertNotNull(userList);
    }

    @Test
    @Ignore
    public void saveImage() {

        String fileName = resource.getFilename();

        try {
            Attachment attachment = new Attachment();
            attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            attachment.setFileName(resource.getFilename());
            attachment.setFileSize(resource.getFile().length());
            attachment.setFileDescription("profile_picture");
            Map<String,String> metadata = PangoUtility.attachmentMetadataToMap("1", ReferenceIdFor.USER,"",attachment);
            gridFsService.storeFiles(resource.getInputStream(), metadata, BasicDBObject::new); //TODO: test case
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void getImage() {
        List<GridFSDBFile> files = gridFsTemplate
                .find(new Query().addCriteria(Criteria.where("metadata.userReferenceId").is("1")));
        for (GridFSDBFile file : files) {
            try {
                file.writeTo("/Users/abhisheksingh/Downloads/new-mongodb.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
