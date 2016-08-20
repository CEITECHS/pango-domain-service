/**
 * 
 */
package com.ceitechs.domain.service.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.domain.User;
import org.junit.Ignore;
import org.junit.Test;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;

import static org.junit.Assert.*;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PangoUtililityBeanTest extends AbstractPangoDomainServiceIntegrationTest {

    @Test
    public void InputStreamToBase64Test() throws IOException {
        String base64 = PangoUtility.InputStreamToBase64(Optional.ofNullable(resource.getInputStream()), "image/png").get();
        assertNotNull(base64);
        assertTrue(base64.contains("data:image/png;base64"));
    }

    @Test
    public void Base64ToInputStreamTest() throws IOException {
        String base64 = PangoUtility.InputStreamToBase64(Optional.ofNullable(resource.getInputStream()), "image/png").get();
        assertNotNull(base64);
        String splitedBase64 = base64.split(",")[1];
        assertNotNull(splitedBase64);
        Optional<InputStream> content = PangoUtility.Base64ToInputStream(Optional.of(splitedBase64));
        assertTrue(content.isPresent());
        assertTrue(isInputStreamsEqual(content.get(), resource.getInputStream()));
    }

    private static boolean isInputStreamsEqual(InputStream i1, InputStream i2) throws IOException {
        byte[] buf1 = new byte[64 * 1024];
        byte[] buf2 = new byte[64 * 1024];
        try {
            DataInputStream d2 = new DataInputStream(i2);
            int len;
            while ((len = i1.read(buf1)) > 0) {
                d2.readFully(buf2, 0, len);
                for (int i = 0; i < len; i++)
                    if (buf1[i] != buf2[i])
                        return false;
            }
            return d2.read() < 0; // is the end of the second file also.
        } catch (EOFException ioe) {
            return false;
        } finally {
            i1.close();
            i2.close();
        }
    }

    @Test
    public void phoneNumberMatcher() {
        String pattern = "^(\\+|0)[0-9]{7,14}$";
        String[] phones = {"0756354180", "+255756354180", "+14106354180"};
        for (String p : phones) {
            assertTrue(p.matches(pattern));
        }

         String DATE_FORMAT = "YYYY-MM-dd";
        String DATE_FORMAT_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date now = new Date(System.currentTimeMillis());
       // System.out.println(dateFormat.format(now));
        assertTrue(dateFormat.format(now).matches(DATE_FORMAT_PATTERN));
       // System.out.println(PangoUtility.getLocalDateDateFrom(dateFormat.format(now)).get());


    }

    @Test
    public void testSecretKeyEncryptDecrypt() throws Exception {
        SecretKey secretKey = PangoUtility.secretKey();
        String encryptedText = PangoUtility.encrypt("OmG", secretKey);
        assertTrue("OmG".equals(PangoUtility.decrypt(encryptedText, secretKey)));
    }

    /**
     * Uncomment last line for a new key
     * 
     * @throws Exception
     */
    @Test
    public void testSecretKeyTextEncryptDecrypt() throws Exception {
        String secretKey = PangoUtility.secretKeyText();
        String encryptedText = PangoUtility.encrypt("OmG", secretKey);
        assertTrue("OmG".equals(PangoUtility.decrypt(encryptedText, secretKey)));
        System.out.println("Secret Key: " + secretKey);
    }

    @Ignore
    @Test
    public void testSecretKeyTextEncryptDecryptBean() throws Exception {
        String secretKey = PangoUtility.secretKeyText();
       // String encryptedText = PangoUtility.encrypt("Emefana2014", secretKey); ceitechsender@gmail.com
       // assertTrue("Emefana2014".equals(PangoUtility.decrypt(encryptedText, secretKey)));Ceitechs2015

       // System.out.println("Encrypted : " + encryptedText);
//        String encryptedText = PangoUtility.encrypt("ceitechsender@gmail.com", secretKey);
//        String encryptedString = PangoUtility.encrypt("Ceitechs2015", secretKey);
//        assertTrue("ceitechsender@gmail.com".equals(PangoUtility.decrypt(encryptedText, secretKey)));
//        assertTrue("Ceitechs2015".equals(PangoUtility.decrypt(encryptedString, secretKey)));
//        System.out.println(secretKey);
//        System.out.println(encryptedText);
//        System.out.println(encryptedString);
    }

    @Test
    public void testGenerateIdAsString() {
        String id = PangoUtility.generateIdAsString();
        assertNotNull("Generated Id should not be null", id);
        assertTrue("Generated Id should not contain hyphens", !id.contains("-"));
    }

    @Test
    public void remainingDurationTest(){
        System.out.println(PangoUtility.remainingDurationBtnDateTimes(LocalDateTime.now(), LocalDateTime.now().plusDays(2)));
        System.out.println(PangoUtility.remainingDurationBtnDateTimes(LocalDateTime.now(), LocalDateTime.now().plusDays(2).plusHours(1)));
        System.out.println(PangoUtility.remainingDurationBtnDateTimes(LocalDateTime.now(), LocalDateTime.now().plusDays(2).plusHours(1).plusMinutes(24)));
    }

    @Ignore
    public void reftest() throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        User usr = new User();
        usr.setEmailAddress("iddy85@gmail.com");
        String value = (String)new PropertyDescriptor("emailAddress", User.class).getReadMethod().invoke(usr);
        System.out.println(value);

        for (PropertyDescriptor pd : Introspector.getBeanInfo(User.class).getPropertyDescriptors()) {
            if (pd.getReadMethod() != null && !"class".equals(pd.getName()))
                System.out.println(pd.getName());
                //System.out.println(pd.getReadMethod().invoke(foo));
        }
    }

    @Test
    public void updateSomeObjectProperties() throws IntrospectionException {
        User usr = new User();
        usr.setEmailAddress("iddy85@gmail.com");
        usr.setFirstName("iddy");
        assertNull(usr.getAddress());

        User user1 = new User();
        user1.setEmailAddress("iddy.magohe@pango.com");
        Address address = new Address();
        address.setAddressLine1("10000 Palace VCT");
        address.setCity("Richmond");
        address.setCountry("US");
        user1.setAddress(address);
        user1.setFirstName("Magohe");

        assertTrue(PangoUtility.updatedSomeObjectProperties(usr,user1, Arrays.asList("emailAddress","address"),User.class));
        assertEquals("Emails must be equals",user1.getEmailAddress(),usr.getEmailAddress());
        assertEquals("First Name must not be changed","iddy",usr.getFirstName());
        assertNotNull(usr.getAddress());
        System.out.println(usr);
    }

    @Test
    public void testAnnotaionExtraction(){
        System.out.println(PangoUtility.fieldNamesByAnnotation(User.class,Updatable.class));
    }

}
