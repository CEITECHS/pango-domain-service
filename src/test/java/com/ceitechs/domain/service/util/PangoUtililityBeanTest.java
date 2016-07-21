/**
 * 
 */
package com.ceitechs.domain.service.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.Test;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PangoUtililityBeanTest extends AbstractPangoDomainServiceIntegrationTest {

    @Test
    public void InputStreamToBase64Test() throws IOException {
        String base64 = PangoUtility.InputStreamToBase64(Optional.ofNullable(resource.getInputStream()), "png").get();
        assertNotNull(base64);
        assertTrue(base64.contains("data:image/png;base64"));
    }

    @Test
    public void Base64ToInputStreamTest() throws IOException {
        String base64 = PangoUtility.InputStreamToBase64(Optional.ofNullable(resource.getInputStream()), "png").get();
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

    @Test
    public void testSecretKeyTextEncryptDecryptBean() throws Exception {
        String secretKey = PangoUtility.secretKeyText();
        String encryptedText = PangoUtility.encrypt("Emefana2014", secretKey);
        assertTrue("Emefana2014".equals(PangoUtility.decrypt(encryptedText, secretKey)));
       // System.out.println("Encrypted : " + encryptedText);
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

}
