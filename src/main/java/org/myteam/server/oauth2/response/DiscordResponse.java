package org.myteam.server.oauth2.response;

import java.util.Map;

public class DiscordResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public DiscordResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "discord";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    public String getName() {
        return attribute.get("global_name").toString();
    }
}
