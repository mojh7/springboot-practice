package com.example.api.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final MemberRepository memberRepository;
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long expireLength;

    public JwtTokenProvider(
            MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String payload) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date now = new Date();
        Date validity = new Date(now.getTime() + expireLength);

        return Jwts.builder()
                   .setHeaderParam("typ", "JWT")
                   .setClaims(claims)
                   .setIssuedAt(now)
                   .setExpiration(validity)
                   .signWith(SignatureAlgorithm.HS256, secretKey)
                   .compact();
    }

    public String getPayload(String token) {
        return Jwts.parser()
                   .setSigningKey(secretKey)
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    public Authentication getAuthentication(String token) {
        String credential = getPayload(token);
        Member member = memberRepository.findOptionalMemberByKakaoId(credential)
                                        .orElseThrow(
                                                () -> new AuthenticationCredentialsNotFoundException(
                                                        "credential error: 존재하지 않는 회원입니다."));

        Map<String, Object> attribute = createAttribute(member);

        if (member.hasNotRole()) {
            member.addRole("ROLE_USER");
        }

        OAuth2User AuthenticatedMember = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                attribute, "kakaoId");

        return new OAuth2AuthenticationToken(AuthenticatedMember,
                Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                "kakao");
    }

    private Map<String, Object> createAttribute(Member member) {
        Map<String, Object> attribute = new HashMap<>();

        attribute.put("kakaoId", member.getKakaoId());
        return attribute;
    }

    public String resolveToken(HttpServletRequest request) {
        return AuthorizationExtractor.extract(request, AuthorizationType.BEARER);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                                     .setSigningKey(secretKey)
                                     .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("유효하지 않는 토큰입니다.");
        }
    }
}
