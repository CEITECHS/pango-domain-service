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
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.User;
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
    @Ignore
    public void findAllUser() {
        List<User> userList = userRepository.findAll();
        assertNotNull(userList);
    }

    @Test
    @Ignore
    public void saveImage() {
        DBObject metadata = new BasicDBObject();
        metadata.put("userReferenceId", "1");
        metadata.put("caption", "profile_pic");
        try {
            gridFsTemplate.store(resource.getInputStream(), resource.getFilename(), "image/png", metadata);
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
