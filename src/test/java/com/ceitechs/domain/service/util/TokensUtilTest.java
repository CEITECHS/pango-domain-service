package com.ceitechs.domain.service.util;

import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserProfile;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;


/**
 * @author iddymagohe on 8/21/16.
 */
public class TokensUtilTest {

    @Test
    public void verificationTokenTest() throws Exception {
        User user = new User();
        user.setEmailAddress("pango@pango.com");
        UserProfile profile = new UserProfile();
        profile.setPassword("ThisSTrongPass!!");
        user.setProfile(profile);
        String signature = TokensUtil.createAccountVerificationToken(user);
        System.out.println(" sig " +signature);
        Optional<User> userOptional = TokensUtil.validateVerificationToken(signature, user);
        assertTrue(userOptional.isPresent());
        assertEquals(user.getEmailAddress(),userOptional.get().getEmailAddress());
        assertEquals(user.getProfile().getPassword(),userOptional.get().getProfile().getPassword());
        System.out.println(" code " +userOptional.get().getProfile().getVerificationCode());
    }
}
