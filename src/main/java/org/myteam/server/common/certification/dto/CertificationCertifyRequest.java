package org.myteam.server.common.certification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class CertificationCertifyRequest {
    @Pattern(regexp = "^[a-zA-Z가-힣]{1,40}$", message = "한글/영문 1~40자 이내로 작성해주세요")
    private String email;
    @NotBlank
    private String code;
}
