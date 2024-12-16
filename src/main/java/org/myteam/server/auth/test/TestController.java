package org.myteam.server.auth.test;

import java.util.UUID;

import lombok.Getter;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/user-access-test")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String userAccessTest(@AuthenticationPrincipal UUID publicId) {
        System.out.println("publicId = " + publicId);

        return "User Access Test";
    }

    @GetMapping("/manager-access-test")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String managerAccessTest(@AuthenticationPrincipal UUID publicId, Authentication authentication) {
        System.out.println("publicId = " + publicId);

        return "Admin Access Test";
    }

    @GetMapping("/exception-test")
    public String exceptionTest() {
        System.out.println("exception test");
        throw new PlayHiveException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
