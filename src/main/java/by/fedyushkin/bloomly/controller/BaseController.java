package by.fedyushkin.bloomly.controller;


import by.fedyushkin.bloomly.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal jwt) {
            return jwt.id();
        }
        return null;
    }
}
