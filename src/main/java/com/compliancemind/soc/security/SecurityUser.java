package com.compliancemind.soc.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} 实现，承载 JWT 中的用户标识与角色。
 */
@Getter
public class SecurityUser implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String password;
    private final String roleCode;

    public SecurityUser(Integer userId, String username, String password, String roleCode) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.roleCode = roleCode == null ? RoleCodes.GENERAL_USER : RoleCodes.normalizeCompanyRole(roleCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
