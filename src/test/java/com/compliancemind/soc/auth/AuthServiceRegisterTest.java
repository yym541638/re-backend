package com.compliancemind.soc.auth;

import com.compliancemind.soc.dto.auth.RegisterRequest;
import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.dto.auth.LoginResponse;
import com.compliancemind.soc.service.auth.AuthService;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.invitation.InvitationCode;
import com.compliancemind.soc.service.invitation.InvitationCodeService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.JwtService;
import com.compliancemind.soc.security.RoleCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 注册：开户强制 SYS_ADMIN + 公司名唯一；加入必须邀请码。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock
    UserAccountMapper userAccountMapper;
    @Mock
    CompanyMapper companyMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    CurrentUserAccessor currentUserAccessor;
    @Mock
    UserProductMapper userProductMapper;
    @Mock
    InvitationCodeService invitationCodeService;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void injectExpireSeconds() {
        ReflectionTestUtils.setField(authService, "expireSeconds", 7200L);
    }

    private RegisterRequest bootstrapRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("George");
        r.setLastName("Yao");
        r.setEmail("test@test.com");
        r.setPassword("Test@123456");
        r.setPhone("13800000000");
        r.setCompanyName("Demo Company");
        r.setUserType("CLIENT");
        // 客户端即使传 User，开户也应被忽略并强制 Admin
        r.setPermissionCode("SYS_USER");
        return r;
    }

    private RegisterRequest inviteRequest() {
        RegisterRequest r = bootstrapRequest();
        r.setInvitationCode("INV-ABC");
        return r;
    }

    private void stubInsertUserReturnsId(int userId) {
        when(userAccountMapper.insert(any(UserAccount.class))).thenAnswer(inv -> {
            UserAccount u = inv.getArgument(0);
            u.setUserId(userId);
            return 1;
        });
    }

    private void stubInsertCompanyReturnsId(int companyId) {
        when(companyMapper.insert(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setCompanyId(companyId);
            return 1;
        });
    }

    @Nested
    @DisplayName("开户（无邀请码）")
    class BootstrapCases {

        @Test
        void creates_company_and_forces_sys_admin() {
            RegisterRequest req = bootstrapRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(0L);
            when(userAccountMapper.countByPhone(req.getPhone())).thenReturn(0L);
            when(companyMapper.selectByName("Demo Company")).thenReturn(null);
            stubInsertCompanyReturnsId(2001);
            stubInsertUserReturnsId(1002);

            Company insertedCo = new Company();
            insertedCo.setCompanyId(2001);
            insertedCo.setCompanyName("Demo Company");
            when(companyMapper.selectById(2001)).thenReturn(insertedCo);

            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userProductMapper.countActiveByUserId(1002)).thenReturn(0L);
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.SYSTEM_ADMIN)).thenReturn("jwt");

            LoginResponse res = authService.register(req);

            assertThat(res.getPurchaseStatus()).isZero();
            assertThat(res.getRedirectTo()).isEqualTo("payment");
            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.SYSTEM_ADMIN);
            assertThat(res.getUser().getSystemRole()).isEqualTo(RoleCodes.SYSTEM_ADMIN);

            ArgumentCaptor<Company> companyCap = ArgumentCaptor.forClass(Company.class);
            verify(companyMapper).insert(companyCap.capture());
            assertThat(companyCap.getValue().getCompanyName()).isEqualTo("Demo Company");

            ArgumentCaptor<UserAccount> userCap = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountMapper).insert(userCap.capture());
            assertThat(userCap.getValue().getRoleCode()).isEqualTo(RoleCodes.SYSTEM_ADMIN);
            assertThat(userCap.getValue().getStatus()).isEqualTo(SocConstants.Account.STATUS_ENABLED);
        }

        @Test
        void rejects_existing_company_name_even_if_client_asks_user_role() {
            RegisterRequest req = bootstrapRequest();
            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            Company existing = new Company();
            existing.setCompanyId(2001);
            existing.setCompanyName("Demo Company");
            when(companyMapper.selectByName("Demo Company")).thenReturn(existing);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_COMPANY_ALREADY_EXISTS.getCode());

            verify(companyMapper, never()).insert(any());
            verify(userAccountMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("邀请加入")
    class InviteCases {

        @Test
        void uses_invitation_company_and_defaults_sys_user() {
            RegisterRequest req = inviteRequest();
            req.setPermissionCode("SYS_ADMIN"); // 客户端自选 Admin 应被忽略

            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);

            InvitationCode inv = new InvitationCode();
            inv.setInvitationId(50L);
            inv.setCompanyId(3001);
            when(invitationCodeService.requireUsableCode("INV-ABC")).thenReturn(inv);

            Company invitedCompany = new Company();
            invitedCompany.setCompanyId(3001);
            invitedCompany.setCompanyName("Invited Co");
            when(companyMapper.selectById(3001)).thenReturn(invitedCompany);

            stubInsertUserReturnsId(1002);
            when(passwordEncoder.encode(any())).thenReturn("h");
            when(userProductMapper.countActiveByUserId(1002)).thenReturn(0L);
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.SYSTEM_USER)).thenReturn("t");

            LoginResponse res = authService.register(req);

            assertThat(res.getUser().getCompanyId()).isEqualTo(3001);
            assertThat(res.getUser().getCompanyName()).isEqualTo("Invited Co");
            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.SYSTEM_USER);

            verify(companyMapper, never()).insert(any());
            verify(companyMapper, never()).selectByName(any());
            verify(invitationCodeService).consumeForUser(eq(inv), any(UserAccount.class));
        }

        @Test
        void invite_member_role_sys_admin_honored_when_company_has_no_admin() {
            RegisterRequest req = inviteRequest();
            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);

            InvitationCode inv = new InvitationCode();
            inv.setCompanyId(3001);
            inv.setMemberRole("SYS_ADMIN");
            when(invitationCodeService.requireUsableCode("INV-ABC")).thenReturn(inv);

            Company invitedCompany = new Company();
            invitedCompany.setCompanyId(3001);
            invitedCompany.setCompanyName("Invited Co");
            when(companyMapper.selectById(3001)).thenReturn(invitedCompany);
            when(userAccountMapper.countByCompanyIdAndRoleCode(3001, RoleCodes.SYSTEM_ADMIN)).thenReturn(0L);

            stubInsertUserReturnsId(1002);
            when(passwordEncoder.encode(any())).thenReturn("h");
            when(userProductMapper.countActiveByUserId(1002)).thenReturn(0L);
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.SYSTEM_ADMIN)).thenReturn("t");

            LoginResponse res = authService.register(req);

            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.SYSTEM_ADMIN);
        }

        @Test
        void invite_member_role_sys_admin_rejected_when_admin_exists() {
            RegisterRequest req = inviteRequest();
            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);

            InvitationCode inv = new InvitationCode();
            inv.setCompanyId(3001);
            inv.setMemberRole("SYS_ADMIN");
            when(invitationCodeService.requireUsableCode("INV-ABC")).thenReturn(inv);

            Company invitedCompany = new Company();
            invitedCompany.setCompanyId(3001);
            when(companyMapper.selectById(3001)).thenReturn(invitedCompany);
            when(userAccountMapper.countByCompanyIdAndRoleCode(3001, RoleCodes.SYSTEM_ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_COMPANY_ADMIN_EXISTS.getCode());
        }

        @Test
        void invitation_company_missing() {
            RegisterRequest req = inviteRequest();
            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            InvitationCode inv = new InvitationCode();
            inv.setCompanyId(999);
            when(invitationCodeService.requireUsableCode("INV-ABC")).thenReturn(inv);
            when(companyMapper.selectById(999)).thenReturn(null);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_INVITATION_COMPANY_MISSING.getCode());
        }

        @Test
        void trims_email_phone_invitation_strings() {
            RegisterRequest req = inviteRequest();
            req.setEmail("  test@test.com  ");
            req.setPhone("  13800000000 ");
            req.setInvitationCode("  INV-X  ");

            when(userAccountMapper.countByEmail("test@test.com")).thenReturn(0L);
            when(userAccountMapper.countByPhone("13800000000")).thenReturn(0L);

            InvitationCode inv = new InvitationCode();
            inv.setCompanyId(9);
            when(invitationCodeService.requireUsableCode("INV-X")).thenReturn(inv);
            Company c = new Company();
            c.setCompanyId(9);
            c.setCompanyName("C");
            when(companyMapper.selectById(9)).thenReturn(c);

            stubInsertUserReturnsId(2);
            when(passwordEncoder.encode(any())).thenReturn("h");
            when(userProductMapper.countActiveByUserId(2)).thenReturn(0L);
            when(jwtService.generateToken(2, "George Yao", RoleCodes.SYSTEM_USER)).thenReturn("t");

            authService.register(req);

            ArgumentCaptor<UserAccount> cap = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountMapper).insert(cap.capture());
            assertThat(cap.getValue().getEmail()).isEqualTo("test@test.com");
            assertThat(cap.getValue().getPhone()).isEqualTo("13800000000");
            assertThat(cap.getValue().getRoleCode()).isEqualTo(RoleCodes.SYSTEM_USER);
        }
    }

    @Nested
    @DisplayName("失败路径")
    class FailureCases {

        @Test
        void email_already_registered() {
            RegisterRequest req = bootstrapRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_EMAIL_REGISTERED.getCode());
        }

        @Test
        void phone_already_registered() {
            RegisterRequest req = bootstrapRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(0L);
            when(userAccountMapper.countByPhone(req.getPhone())).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_PHONE_REGISTERED.getCode());
        }

        @Test
        void display_name_required_when_no_display_no_first_last() {
            RegisterRequest req = bootstrapRequest();
            req.setDisplayName(null);
            req.setFirstName(null);
            req.setLastName(null);

            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            when(companyMapper.selectByName(any())).thenReturn(null);
            stubInsertCompanyReturnsId(1);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_DISPLAY_NAME_REQUIRED.getCode());
        }
    }
}
