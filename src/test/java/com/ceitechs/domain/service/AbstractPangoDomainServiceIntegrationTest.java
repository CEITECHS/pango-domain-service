package com.ceitechs.domain.service;

import com.ceitechs.domain.service.config.PangoDomainServiceConfig;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"pango.domain.service.db.host.name = localhost:27017", "pango.domain.service.db.password = pangoPreprodWriteUsrPass10",
        "pango.domain.service.db.user =pangoWriteUser", "pango.domain.service.db.name = pango", "pango.domain.service.bucket.name = picture","magic.key=5rGXHCU2yoGTn600Gz9i5A==",
        "mail.smtp.host=smtp.gmail.com",
        "mail.smtp.port=587",
        "mail.smtp.protocal=",
        "mail.smtp.user=KrWdWgeYKiIMQqEUVbWMG6xUaIIsDfPJhEVr/VsXUOQ=",
        "mail.smtp.password=NkCHfPStANtsWkcTbsoaDQ==",
        "mail.smtp.auth=true",
        "mail.smtp.starttls.enable=true"})
@ContextConfiguration(classes = {PangoDomainServiceConfig.class})
public class AbstractPangoDomainServiceIntegrationTest {
    protected static final Resource resource = new ClassPathResource("ceitechs.png");
}
