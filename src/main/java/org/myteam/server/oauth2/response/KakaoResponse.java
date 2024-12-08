package org.myteam.server.oauth2.response;

import java.util.Collections;
import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        System.out.println("Raw Kakao attribute: " + attribute);
        System.out.println("Kakao account: " + kakaoAccount(attribute));
        System.out.println("Kakao profile: " + kakaoProfile(attribute));
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    public Map<String, Object> kakaoAccount() {
        return kakaoAccount(this.attribute);
    }

    private Map<String, Object> kakaoAccount(Map<String, Object> attr) {
        return (Map<String, Object>) attr.getOrDefault("kakao_account", Collections.emptyMap());
    }

    public Map<String, Object> kakaoProfile() {
        return kakaoProfile(this.attribute);
    }

    private Map<String, Object> kakaoProfile(Map<String, Object> attr) {
        return (Map<String, Object>) kakaoAccount(attr).getOrDefault("profile", Collections.emptyMap());
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attribute.get("id"));
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccount().get("email");
    }

    public String getName() {
//        return (String) kakaoProfile().get("name");
        return (String) kakaoProfile().get("nickname");
    }

    public String getProfileImageUrl() {
        return (String) kakaoProfile().get("profile_image_url");
    }

    public String getThumbnailImageUrl() {
        return (String) kakaoProfile().get("thumbnail_image_url");
    }
}
