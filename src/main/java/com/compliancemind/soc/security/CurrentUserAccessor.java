package com.compliancemind.soc.security;

import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 读取当前登录用户 ID（基于 SecurityContext）；未登录时抛出业务异常。
 */
@Component
public class CurrentUserAccessor {

    public Integer requireUserId() {
        Integer userId = currentUserId();
        if (userId == null) {
            throw new BizException(BizErrorCode.AUTH_USER_NOT_LOGGED_IN);
        }
        return userId;
    }

    public Integer currentUserId() {
        SecurityUser user = currentUser();
        if (user == null) {
            return null;
        }
        return user.getUserId();
    }

    public String currentUsername() {
        SecurityUser user = currentUser();
        return user == null ? "" : user.getUsername();
    }

    public String currentRoleCode() {
        SecurityUser user = currentUser();
        return user == null ? "" : user.getRoleCode();
    }

    private SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }
        return null;
    }
}
