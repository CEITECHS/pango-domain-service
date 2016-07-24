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
        "pango.domain.service.db.user =pangoWriteUser", "pango.domain.service.db.name = pango", "pango.domain.service.bucket.name = picture"})
@ContextConfiguration(classes = {PangoDomainServiceConfig.class})
public class AbstractPangoDomainServiceIntegrationTest {
    protected static final Resource resource = new ClassPathResource("ceitechs.png");
}
