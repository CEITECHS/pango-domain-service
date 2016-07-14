package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public class PangoDomainServiceUserRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    private PangoDomainServiceUserRepository userRepository;

    @Test
    public void saveUser() {
        User user = new User();
        user.setUserReferenceId(new BigInteger("1"));
        user.setFirstName("fName");
        user.setLastName("lName");
        user.setEmailAddress("fName.lName@pango.com");
        User savedUser = userRepository.save(user);
        assertNotNull("The expected userId should not be null", savedUser.getUserReferenceId());
    }

    @Test
    public void findAllUser() {
        List<User> userList = userRepository.findAll();
        assertNotNull(userList);
    }
}
