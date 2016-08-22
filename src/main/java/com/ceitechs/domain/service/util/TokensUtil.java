package com.ceitechs.domain.service.util;

import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserProfile;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * @author  iddymagohe on 8/21/16.
 * @since 1.0
 */
public class TokensUtil {

    private static final String SEPARATOR ="_";

    public static String createAccountVerificationToken(User user) throws Exception {

        long expires = System.currentTimeMillis() + 1000L * 60 * 60 *72; //expires in three days
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(user.getEmailAddress());
        signatureBuilder.append(":");
        signatureBuilder.append(expires);
        signatureBuilder.append(":");
        signatureBuilder.append(user.getProfile().getPassword());
        signatureBuilder.append(":");
        signatureBuilder.append(computeSignature(user,expires));
        String randomKey = PangoUtility.secretKeyText();
        return  PangoUtility.compressBase64Token(randomKey+SEPARATOR+PangoUtility.encrypt(signatureBuilder.toString(),randomKey));
    }

    private static String computeSignature(User user, long expires) throws Exception {
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(user.getEmailAddress());
        signatureBuilder.append(":");
        signatureBuilder.append(expires);//expires in three days
        signatureBuilder.append(":");
        signatureBuilder.append(user.getProfile().getPassword());
        signatureBuilder.append(":");

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
    }

    public static Optional<User> validateToken(String verificationToken) throws Exception {
        String[] outerParts = new String(PangoUtility.decompressedToken(verificationToken)).split(SEPARATOR);
        String decodedToken = PangoUtility.decrypt(outerParts[1], outerParts[0]);
        String[] parts = decodedToken.split(":");
        long expires = Long.parseLong(parts[1]);

        if (expires < System.currentTimeMillis()) new IllegalStateException("Token has expired");
        User user = new User();
        user.setEmailAddress(parts[0]);
        UserProfile profile = new UserProfile();
        profile.setPassword(parts[2]);
        profile.setVerificationCode(parts[3]);
        user.setProfile(profile);
        if (user.getProfile().getVerificationCode().equals(computeSignature(user, expires))) {
            return Optional.ofNullable(user);
        }
        return Optional.empty();
    }
}
