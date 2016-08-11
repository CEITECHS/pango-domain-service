/**
 * 
 */
package com.ceitechs.domain.service.util;

import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.domain.AttachmentToUpload;
import com.ceitechs.domain.service.domain.User;
import lombok.Getter;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author iddymagohe
 * @since 0.1
 *
 */
public class PangoUtility {

    /**
     * DATE_FORMAT = "YYYY-MM-dd"
     */
    public static final String  DATE_FORMAT_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    public static final String DATE_FORMAT_PATTERN_SEPEATOR ="-";

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

    public static long generateIdAsLong(){
        Random randomno = new Random();

        // get next long value
        return randomno.nextLong();
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
            return Optional.ofNullable("data:" + ext + ";base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
        }

        return Optional.empty();
    }

    public static Optional<InputStream> Base64ToInputStream(Optional<String> base64String) throws IOException {
        if (base64String.isPresent()) {
            return Optional.ofNullable(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(base64String.get())));
        }

        return Optional.empty();
    }

    /**
     * Prepare Metadata required to persist an attachment
     * 
     * @param referenceId
     * @param attachment
     * @param referenceIdFor
     * @return
     */
    public static Map<String, String> attachmentMetadataToMap(String referenceId,ReferenceIdFor referenceIdFor,Attachment attachment,  String ...parentReferenceIds) {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put(MetadataFields.CONTENT_TYPE, FileMetadata.FILETYPE.valueOf(attachment.getFileType().toUpperCase()).getSuffix()+ attachment.extractExtension());
        metadataMap.put(MetadataFields.FILE_NAME, attachment.getFileName());
        metadataMap.put(MetadataFields.TYPE, FileMetadata.FILETYPE.valueOf(attachment.getFileType()).name());
        metadataMap.put(MetadataFields.FILE_DESCR, attachment.getFileDescription());
        metadataMap.put(referenceIdFor.getMetadataField(), referenceId);
        metadataMap.put(MetadataFields.PROFILEPICTURE, String.valueOf(attachment.isProfilePicture()));

        referenceIdFor.getParentField().ifPresent(p -> {
            if (org.springframework.util.StringUtils.hasText(parentReferenceIds[0])){
                 metadataMap.put(p.getMetadataField(),parentReferenceIds[0]);
            }

            p.getParentField().ifPresent(gp -> {
                if (org.springframework.util.StringUtils.hasText(parentReferenceIds[1])){
                    metadataMap.put(gp.getMetadataField(),parentReferenceIds[1]);
                }
            });
        });
        return metadataMap;

    }

    /**
     * Prepare Metadata required to persist an attachment
     * @param attachmentToUpload
     * @return
     */
    public static Map<String, String> attachmentMetadataToMap(AttachmentToUpload attachmentToUpload){
        return  attachmentMetadataToMap(attachmentToUpload.getReferenceId(),attachmentToUpload.getReferenceIdFor(),attachmentToUpload.getAttachment(),attachmentToUpload.getParentReferenceId());
    }

    public static int random(int minimum, int maximum){
        Random rand = new Random();
        return  minimum + rand.nextInt((maximum - minimum) + 1);
    }

    public static double random(double minimum, double maximum){
        Random r = new Random();
        return  minimum + (maximum - minimum) * r.nextDouble();
    }

    public static Optional<LocalDate> getLocalDateDateFrom(String dateString){
        if (StringUtils.hasText(dateString) && dateString.matches(DATE_FORMAT_PATTERN)){
            String[] dateParts  = dateString.split(DATE_FORMAT_PATTERN_SEPEATOR);
            return Optional.of(LocalDate.of(Integer.valueOf(dateParts[0]),Integer.valueOf(dateParts[1]),Integer.valueOf(dateParts[2])));
        }
        return Optional.empty();
    }

    public static String remainingDurationBtnDateTimes(final LocalDateTime startDateATime,  final LocalDateTime toDateTime){
            LocalDateTime lastDate = LocalDateTime.of(startDateATime.toLocalDate(), startDateATime.toLocalTime()) ;
            long days =  ChronoUnit.DAYS.between(startDateATime,toDateTime);
            long hours =  ChronoUnit.HOURS.between(lastDate.plusDays(days),toDateTime);
            long minutes = ChronoUnit.MINUTES.between(lastDate.plusDays(days).plusHours(hours),toDateTime);
            return String.format("%dd %dh %dm",days, hours, minutes);
    }

    public static <T> boolean updatedSomeObjectProperties(T originalObject, T updatedObject, List<String> updatableProperties, Class<T> clazz) throws IntrospectionException {
        if (originalObject == null || updatedObject == null || (updatableProperties == null || updatableProperties.isEmpty()))
            return false; // null parameters were passed.
        if (originalObject != updatedObject) { //only go through the process if objects are not same reference
            Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                    .filter(propertyDescriptor -> updatableProperties.contains(propertyDescriptor.getName()))
                    .forEach(propertyDescriptor -> {
                        updateProperty(originalObject, updatedObject, propertyDescriptor);

                    });
            return true;
        }
        return false;
    }

    /**
     *
     * @param originalObject
     * @param updatedObject
     * @param descriptor
     */
    private static void updateProperty(Object originalObject, Object updatedObject, PropertyDescriptor descriptor) {
        try {
            Method readMethod = descriptor.getReadMethod();
            Object newValue = readMethod.invoke(updatedObject);
            if (newValue != null && !newValue.equals(readMethod.invoke(originalObject))) {
                Method writeMethod = descriptor.getWriteMethod();
                writeMethod.invoke(originalObject, newValue);
            }
        }catch (Exception ex){
            ex.printStackTrace(); //log this exception
        }
    }


    public static List<String> fieldNamesByAnnotation(Class clazz, Class<? extends Annotation> annotationClass){
        Field[] fieldList = clazz.getDeclaredFields();
       return Arrays.stream(fieldList).filter(field -> field.isAnnotationPresent(Updatable.class)).map(Field::getName).collect(Collectors.toList());
    }

}
