package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.User;
import lombok.Getter;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author iddymagohe on 8/20/16.
 */
public class PangoEmailServiceTest extends AbstractPangoDomainServiceIntegrationTest {
    @Autowired PangoMailService mailService;

    //@Ignore
    @Test
    public void sendPlainTextEmailTest(){
        EmailModel<String> emailModel = new EmailModel<>();
        emailModel.setBccRecipients(new String[]{"iddyiam@gmail.com","abhikumar.singh@gmail.com"});
        emailModel.setSubject("Pango Test Email");
        StringBuilder str = new StringBuilder("This is a test email from Pango service");
        str.append(System.lineSeparator());
        str.append("Cheers");
        str.append(System.lineSeparator());
        str.append("Pango Team");
        emailModel.setModel(str.toString());
        mailService.sendEmail(emailModel);
    }

    @Ignore
    @Test
    public void sendTemplateEMailTest(){
        EmailModel<User> emailModel = new EmailModel<>();
        User usr = new User();
        usr.setEmailAddress("idrak@pango.com");
        usr.setFirstName("Idrak");
        usr.setLastName("The Lucy One");
        emailModel.setModel(usr);
        emailModel.setSubject("Pango Test Email using a github hosted template");
        emailModel.setBccRecipients(new String[]{"iddy85@gmail.com","abhikumar.singh@gmail.com"});
        emailModel.setTemplate("registration-confirmation");
        mailService.sendEmail(emailModel);
    }
}
