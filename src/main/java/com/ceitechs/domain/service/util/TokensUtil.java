package com.ceitechs.domain.service.util;

import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserProfile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.crypto.MacProvider;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * @author  iddymagohe on 8/21/16.
 * @since 1.0
 */
public class TokensUtil {

    public static String createAccountVerificationToken(User user) throws Exception {
        String key  = Base64.getEncoder().encodeToString(MacProvider.generateKey().getEncoded());
        user.getProfile().setVerificationCode(key);
        String compactJws = JwtTokenUtil.generateToken(user.getEmailAddress(),JwtTokenUtil.AUDIENCE_UNEXPIRED,key);
        return  compactJws;
    }


    public static Optional<User> validateVerificationToken(String verificationToken, User user) throws Exception {
        if (JwtTokenUtil.validateToken(verificationToken, token -> {
            Claims claims = JwtTokenUtil.getClaimsFromToken(token, user.getProfile().getVerificationCode());
            String subject = (String) JwtTokenUtil.getSubjectFromClaims(claims);
            return subject.equals(user.getEmailAddress());
        })) {
            return Optional.ofNullable(user);
        }
        return Optional.empty();
    }
}
