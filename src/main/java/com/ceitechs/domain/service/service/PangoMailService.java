package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.util.PangoUtility;
import com.sun.source.tree.AssertTree;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author  by iddymagohe on 8/20/16.
 * @since 1.0
 */
public interface PangoMailService {

    /**
     *  sends email to one or many recipients based on {@link EmailModel}
     * @param emailModel
     */
     <T> void sendEmail(EmailModel<T> emailModel);
}

@Service
class PangoMailServiceImpl implements PangoMailService {

    private static final Logger logger = LoggerFactory.getLogger(PangoMailServiceImpl.class);
    protected static final Resource resource = new ClassPathResource("templates/logo.png");

    private final JavaMailSender mailSender;

    private final VelocityEngine velocityEngine;

    @Autowired
    public PangoMailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {
        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
    }

    /**
     * sends email to one or many recipients based on {@link EmailModel}
     *
     * @param emailModel
     */
    @Override
    public <T> void sendEmail(EmailModel<T> emailModel) {

        Assert.notNull(emailModel, "EmailModel can not be null");
        Assert.hasText(emailModel.getSubject(), "email subject can not be empty or null");
        Assert.isTrue(emailModel.getBccRecipients().length > 0 || emailModel.getRecipients().length >0 ,"recipient list can not be null or empty");
        Assert.notNull(emailModel.getModel() , "model can not be null ");
        Assert.isTrue(emailModel.getModel() instanceof String || StringUtils.hasText(emailModel.getTemplate()));


        mailSender.send(mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            if (emailModel.getRecipients().length > 0) message.setTo(emailModel.getRecipients());
            if (emailModel.getBccRecipients().length > 0) message.setBcc(emailModel.getBccRecipients());
            if (emailModel.getCopiedRecipients().length > 0) message.setCc(emailModel.getCopiedRecipients());
            message.setSentDate(new Date());
            message.setSubject(emailModel.getSubject());

            StringBuilder emailText = null;
            if (emailModel.getModel() instanceof String) {
                // sending plain text message without template
                emailText = new StringBuilder((String) emailModel.getModel());
            } else if (StringUtils.hasText(emailModel.getTemplate())) {
                // sending email using a template
                String templatePath = new String(emailModel.getTemplate() + ".vm");
                Map model = new HashMap();
                model.put("model", emailModel.getModel());
                emailText = new StringBuilder(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templatePath, "UTF-8", model));
            }

            message.setText(emailText.toString(), true);

        });
    }
}
