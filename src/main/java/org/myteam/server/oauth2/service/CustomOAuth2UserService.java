package org.myteam.server.oauth2.service;

import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.util.PasswordUtil;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.domain.MemberType;
import org.myteam.server.member.entity.Member;
import org.myteam.server.member.repository.MemberRepository;
import org.myteam.server.oauth2.constant.OAuth2ServiceProvider;
import org.myteam.server.oauth2.dto.CustomOAuth2User;
import org.myteam.server.oauth2.response.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("CustomOAuth2UserService > Oauth2User Request: {}", oAuth2User.toString());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals(OAuth2ServiceProvider.NAVER)) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals(OAuth2ServiceProvider.GOOGLE)) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals(OAuth2ServiceProvider.DISCORD)) {
            oAuth2Response = new DiscordResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals(OAuth2ServiceProvider.KAKAO)) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String providerId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<Member> existDataOP = memberRepository.findByEmail(oAuth2Response.getEmail());

        if (existDataOP.isPresent()) {
            // 유저가 이미 존재하는 경우 업데이트 처리
            log.debug("CustomOAuth2UserService isPresentUser");
            Member existData = existDataOP.get();
            log.debug("providerId : {}", providerId);
            log.debug("email : {}", oAuth2Response.getEmail());
            log.debug("name : {}", oAuth2Response.getName());
            log.debug("provider : {}", oAuth2Response.getProvider());

            existData.updateEmail(oAuth2Response.getEmail());

            memberRepository.save(existData);

            return new CustomOAuth2User(existData.getEmail(), existData.getRole().toString());
        } else {
            // 신규 회원
            log.debug("CustomOAuth2UserService create NewUser");
            log.debug("providerId : {}", providerId);
            log.debug("email : {}", oAuth2Response.getEmail());
            log.debug("name : {}", oAuth2Response.getName());
            log.debug("MemberType.SOCIAL : {}", MemberType.LOCAL);
            log.debug("MemberRole.ROLE_USER : {}", MemberRole.USER);
            log.debug("Provider() : {}", oAuth2Response.getProvider());
            log.debug("registrationId : {}", registrationId);

            Member member = Member.builder()
                    .email(oAuth2Response.getEmail())
                    .password(PasswordUtil.generateRandomPassword())
                    .name(oAuth2Response.getName())
                    .role(MemberRole.USER)
                    .tel("01023232122") // TODO_ : 추후 정리 필요
                    .nickname(null) // TODO_ : 추후 정리 필요
                    .gender(null) // TODO_ : 추후 정리 필요
                    .birthdate(null) // TODO_ : 추후 정리 필요
                    .publicId(UUID.randomUUID()) // TODO_ : 추후 정리 필요
                    .type(MemberType.fromOAuth2Provider(oAuth2Response.getProvider()))
                    .build();

            memberRepository.save(member);

            return new CustomOAuth2User(oAuth2Response.getEmail(), MemberRole.USER.toString());
        }
    }
}