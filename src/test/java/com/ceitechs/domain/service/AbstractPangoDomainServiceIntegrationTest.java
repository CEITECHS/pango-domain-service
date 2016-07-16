package com.ceitechs.domain.service;

import com.ceitechs.domain.service.config.PangoDomainServiceConfig;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"db.host.name = localhost:27017", "db.password = pangoPreprodWriteUsrPass10",
        "db.user =pangoWriteUser", "db.name = pango", "bucket.name = picture"})
@ContextConfiguration(classes = {PangoDomainServiceConfig.class})
public class AbstractPangoDomainServiceIntegrationTest {
    protected static final Resource resource = new ClassPathResource("ceitechs.png");
}
