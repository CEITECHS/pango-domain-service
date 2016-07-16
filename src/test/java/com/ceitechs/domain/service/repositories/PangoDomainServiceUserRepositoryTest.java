package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.User;
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

    @Test
    public void saveUser() {
        User user = new User();
        user.setUserReferenceId("1");
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
        assertNotNull("The expected userId should not be null", savedUser.getUserReferenceId());
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
