package org.myteam.server.oauth2.response;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static org.myteam.server.oauth2.constant.OAuth2ServiceProvider.NAVER;

public class NaverResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return NAVER;
    }

    @Override
    public String getProviderId() {
        return StringUtils.defaultString((String) attribute.get("id"), "");
    }

    @Override
    public String getEmail() {
        return StringUtils.defaultString((String)attribute.get("email"), "");
    }

    public String getName() {
        return StringUtils.defaultString((String) attribute.get("name"), "");
    }
}
