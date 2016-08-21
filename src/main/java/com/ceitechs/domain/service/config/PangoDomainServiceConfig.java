package com.ceitechs.domain.service.config;

import com.ceitechs.domain.service.util.PangoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

/**
 * @author abhisheksingh -
 * @since 1.0
 */
@Configuration
@Import({PangoDomainServiceMongoConfiguration.class})
@ComponentScan(basePackages = "com.ceitechs.domain.service.service")
public class PangoDomainServiceConfig {

     @Value("${mail.smtp.host}")
     private String mailHost;

     @Value("${mail.smtp.port}")
     private int mailPort;

     @Value("${mail.smtp.protocal}")
     private String mailProtocal;

     @Value("${mail.smtp.user}")
     private String mailUser;

     @Value("${mail.smtp.password}")
     private String mailPassword;

     @Value("${mail.smtp.auth}")
     private boolean mailAuth;

     @Value("${mail.smtp.starttls.enable}")
     private boolean mailStarttls;

     @Value("${magic.key}")
     private String appliedKey;

     @Value("${templates.root.uri}")
     private String templatesRoot;


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Lazy(true)
    public JavaMailSenderImpl javaMailSenderImpl() throws Exception {
        JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
        mailSenderImpl.setHost(mailHost);
        mailSenderImpl.setPort(mailPort);

        // mailSenderImpl.setProtocol(mailProtocal); // use default
        mailSenderImpl.setUsername(PangoUtility.decrypt(mailUser,appliedKey));
        mailSenderImpl.setPassword(PangoUtility.decrypt(mailPassword,appliedKey));

        Properties javaMailProps = new Properties();
        javaMailProps.put("mail.smtp.auth", mailAuth);
        javaMailProps.put("mail.smtp.starttls.enable", mailStarttls);

        mailSenderImpl.setJavaMailProperties(javaMailProps);

        return mailSenderImpl;
    }

    @Bean
    public VelocityEngineFactoryBean velocityEngine() {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("resource.loader", "url"); //jar, class, file
        velocityProperties.setProperty("url.resource.loader.class","org.apache.velocity.runtime.resource.loader.URLResourceLoader");//ClasspathResourceLoader
        velocityProperties.setProperty("url.resource.loader.root",templatesRoot);
        velocityProperties.setProperty("url.resource.loader.cache","true");
        //velocityProperties.setProperty("url.resource.loader.modificationCheckInterval","5");
        velocityEngineFactoryBean.setVelocityProperties(velocityProperties);
        return velocityEngineFactoryBean;
    }
}
