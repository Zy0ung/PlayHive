package org.myteam.server.common.certification.dto;

import lombok.Getter;

@Getter
public class CertificationEmailRequest {
    private String email;
    private String code;
}
