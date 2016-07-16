/**
 * 
 */
package com.ceitechs.domain.service.util;

import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author iddymagohe
 * @since 0.1
 *
 */
public class PangoUtility {

    @Getter
    private String SECRET_KEY; // To be passed in as environment variable

    /**
     * @param sECRET_KEY the sECRET_KEY to set
     */
    public void setSECRET_KEY(String sECRET_KEY) {
        String[] secrets = sECRET_KEY.split("-");
        SECRET_KEY = secrets[secrets.length - 1];
    }

    /**
     * Used by JCE for D/Encryption
     * 
     * @return Cipher
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES");
    }

    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = getCipher();
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(encryptedByte);
        return encryptedText;
    }

    public static String encrypt(String plainText, String encodedKey) throws Exception {
        SecretKey secretKey = secretKey(encodedKey);
        return encrypt(plainText, secretKey);
    }

    public static String decrypt(String encryptedText, String encodedKey) throws Exception {
        SecretKey secretKey = secretKey(encodedKey);
        return decrypt(encryptedText, secretKey);
    }

    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = getCipher();
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }

    static SecretKey secretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    static SecretKey secretKey(String encodedKey) throws NoSuchAlgorithmException {
        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return secretKey;
    }

    static String secretKeyText() throws NoSuchAlgorithmException {
        // create new key
        SecretKey secretKey = secretKey();
        // get base64 encoded version of the key
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static String generateIdAsString() {
        return replaceHyphens(UUID.randomUUID().toString());
    }

    public static String replaceSpaces(String codefrom) {
        return codefrom.replaceAll("\\s+", "");
    }

    public static String replaceHyphens(String codefrom) {
        return codefrom.replaceAll("-", "");
    }

    public static <T> ArrayList<T> toArrayList(final Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public static <T> List<T> toList(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    public static Optional<String> InputStreamToBase64(Optional<InputStream> inputStream, String ext)
            throws IOException {
        if (inputStream.isPresent()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            FileCopyUtils.copy(inputStream.get(), output);
            return Optional.ofNullable(
                    "data:image/" + ext + ";base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
        }

        return Optional.empty();
    }

    public static Optional<InputStream> Base64ToInputStream(Optional<String> base64String) throws IOException {
        if (base64String.isPresent()) {
            return Optional
                    .ofNullable(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(base64String.get())));
        }

        return Optional.empty();
    }

    /**
     * To persist provider related files objects, not user profile photo
     * 
     * @param referenceId
     * @param attachment
     * @return
     */
    public static Map<String, String> attachmentMetadataToMap(String referenceId, Attachment attachment) {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put(MetadataFields.CONTENT_TYPE, attachment.getFileType());
        metadataMap.put(MetadataFields.FILE_NAME, attachment.getFileName());
        metadataMap.put(MetadataFields.TYPE, FileMetadata.FILETYPE.PHOTO.name());
        metadataMap.put(MetadataFields.FILE_DESCR, "Thumbnail image");
        metadataMap.put(MetadataFields.PROVIDER_ID, referenceId);
        metadataMap.put(MetadataFields.THUMBNAIL, "true");
        return metadataMap;

    }

}
