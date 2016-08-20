package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author iddymagohe on 8/20/16.
 */
public class PangoEmailServiceTest extends AbstractPangoDomainServiceIntegrationTest {
    @Autowired PangoMailService mailService;

    @Test
    public void sendPlainTextEmailTest(){
        EmailModel<String> emailModel = new EmailModel<>();
        emailModel.setBccRecipients(new String[]{"iddy85@gmail.com","abhikumar.singh@gmail.com"});
        emailModel.setSubject("Pango Test Email");
        StringBuilder str = new StringBuilder("This is a test email from Pango service");
        str.append(System.lineSeparator());
        str.append("Cheers");
        str.append(System.lineSeparator());
        str.append("Pango Team");
        emailModel.setModel(str.toString());
        mailService.sendEmail(emailModel);
    }
}
