package com.ceitechs.domain.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author iddymagohe on 12/16/16.
 */
public class JwtTokenUtil implements Serializable {

    static final String CLAIM_KEY_SUBJECT = "sub";
    static final String CLAIM_KEY_AUDIENCE = "audience";
    static final String CLAIM_KEY_CREATED = "created";


    private static final String AUDIENCE_UNKNOWN = "unknown";
    public static final String AUDIENCE_WEB = "web";
    public static final String AUDIENCE_UNEXPIRED = "un-expiring";


    private static Long expiration = 3600L; // 1hr


    public static Object getSubjectFromClaims(final Claims claims) {
            return claims.getSubject();
    }

    public static String getAudienceFromClaims(final Claims claims) {
       return  (String) claims.get(CLAIM_KEY_AUDIENCE);
    }

    public static Date getExpirationDateFromClaims(final Claims claims) {
            return claims.getExpiration();
    }

    public static Date getCreatedDateFromClaims(final Claims claims) {
        return new Date((Long) claims.get(CLAIM_KEY_CREATED));
    }

    public static Claims getClaimsFromToken(final String token, final String secret) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private static Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    public static boolean isTokenExpired(final Claims claims) {
        final Date expiration = getExpirationDateFromClaims(claims);
        return expiration.before(new Date());
    }

    private static  Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }



    private static Boolean ignoreTokenExpiration(final Claims claims) {
        String audience = getAudienceFromClaims(claims);
        return (AUDIENCE_UNEXPIRED.equals(audience));
    }

    public static String generateToken(Object subject, String audience, final String secret) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_SUBJECT, subject);
        claims.put(CLAIM_KEY_AUDIENCE, audience);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims,secret);
    }

    private static String generateToken(Map<String, Object> claims, final String secret) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public static boolean isTokenRefreshable(final Claims claims, Date lastPasswordReset) {
        final Date created = getCreatedDateFromClaims(claims);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(claims) || ignoreTokenExpiration(claims));
    }

    public static String refreshToken(final String token, final String secret) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token,secret);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims, secret);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }


    /**
     * Validates token in different ways , by using parts of the tokens via the predicate
     *
     * @param token
     * @param validate
     * @return
     *
     */
    public static boolean validateToken(String token, Predicate<String> validate) {

        /**
         * <pre>
         *  * usage: validate
         * (String token) -> {
         *     Claims claim = {@link JwtTokenUtil#getClaimsFromToken(String, String)}
         *     String subject = {@link JwtTokenUtil#getSubjectFromClaims(Claims)}
         *     Date  createdDate = {@link JwtTokenUtil#getCreatedDateFromClaims(Claims)}
         *     return subject.equals(storedSubject) &&
         *       (! {@link JwtTokenUtil#isTokenExpired(Claims)} || {@link JwtTokenUtil#ignoreTokenExpiration(Claims)}) &&
         *       ! {@link JwtTokenUtil#isCreatedBeforeLastPasswordReset(Date, Date)}
         *
         * }
         * </pre>
         */
        return validate.test(token);
    }

}
