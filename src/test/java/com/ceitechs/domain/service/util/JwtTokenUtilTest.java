package com.ceitechs.domain.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.Test;

import java.util.Base64;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author iddymagohe on 12/16/16.
 */
public class JwtTokenUtilTest {

    @Test
    public void tokensAndDeTokensTest(){
        String key  = Base64.getEncoder().encodeToString(MacProvider.generateKey().getEncoded());
        String compactJws = JwtTokenUtil.generateToken("Pango",JwtTokenUtil.AUDIENCE_WEB,key);
        assertNotNull(compactJws);
        assertTrue(JwtTokenUtil.validateToken(compactJws, (token) -> {
            Claims claims = JwtTokenUtil.getClaimsFromToken(token, key);
            String subject = (String) JwtTokenUtil.getSubjectFromClaims(claims);
            return !JwtTokenUtil.isTokenExpired(claims) && subject.equals("Pango");
        }));
       //System.out.println(compactJws);
    }
}
