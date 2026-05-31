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
 * 对照 {@code API_Doc_v4.md} §2.2 Register 的业务逻辑。
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

    private RegisterRequest legacyRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("George");
        r.setLastName("Yao");
        r.setEmail("test@test.com");
        r.setPassword("Test@123456");
        r.setPhone("13800000000");
        r.setCompanyName("Demo Company");
        r.setRoleCode("GENERAL_USER");
        return r;
    }

    private RegisterRequest altRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setDisplayName("George Yao");
        r.setEmail("test@test.com");
        r.setPassword("Test@123456");
        r.setPhone("13800000000");
        r.setCompanyName("Demo Company");
        r.setRoleCode("GENERAL_USER");
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
    @DisplayName("成功路径（注册后等同登录返回：purchase_status=0、redirect_to=payment）")
    class SuccessCases {

        @Test
        void register_legacy_first_last_joins_existing_company_by_name() {
            RegisterRequest req = legacyRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(0L);
            when(userAccountMapper.countByPhone(req.getPhone())).thenReturn(0L);

            Company existing = new Company();
            existing.setCompanyId(2001);
            existing.setCompanyName("Demo Company");
            when(companyMapper.selectByName("Demo Company")).thenReturn(existing);

            stubInsertUserReturnsId(1002);
            when(passwordEncoder.encode(req.getPassword())).thenReturn("{bcrypt}x");
            when(userProductMapper.countActiveByUserId(1002)).thenReturn(0L);
            when(companyMapper.selectById(2001)).thenReturn(existing);
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("jwt-token");

            LoginResponse res = authService.register(req);

            assertThat(res.getPurchaseStatus()).isZero();
            assertThat(res.getRedirectTo()).isEqualTo("payment");
            assertThat(res.getExpireSeconds()).isEqualTo(7200L);
            assertThat(res.getToken()).isEqualTo("jwt-token");
            assertThat(res.getUser().getDisplayName()).isEqualTo("George Yao");
            assertThat(res.getUser().getEmail()).isEqualTo("test@test.com");
            assertThat(res.getUser().getCompanyId()).isEqualTo(2001);
            assertThat(res.getUser().getCompanyName()).isEqualTo("Demo Company");
            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.GENERAL_USER);

            verify(companyMapper, never()).insert(any());
            verify(invitationCodeService, never()).consumeForUser(any(), any());

            ArgumentCaptor<UserAccount> cap = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountMapper).insert(cap.capture());
            UserAccount saved = cap.getValue();
            assertThat(saved.getCompanyId()).isEqualTo(2001);
            assertThat(saved.getPasswordHash()).isEqualTo("{bcrypt}x");
            assertThat(saved.getStatus()).isEqualTo(SocConstants.Account.STATUS_ENABLED);
        }

        @Test
        void register_alt_display_name_creates_company_when_missing() {
            RegisterRequest req = altRequest();
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
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("jwt");

            authService.register(req);

            ArgumentCaptor<Company> companyCap = ArgumentCaptor.forClass(Company.class);
            verify(companyMapper).insert(companyCap.capture());
            assertThat(companyCap.getValue().getCompanyName()).isEqualTo("Demo Company");
        }

        @Test
        void register_role_alias_general_user_normalized() {
            RegisterRequest req = altRequest();
            req.setRoleCode("USER");
            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            Company existing = new Company();
            existing.setCompanyId(1);
            existing.setCompanyName("Demo Company");
            when(companyMapper.selectByName(any())).thenReturn(existing);
            stubInsertUserReturnsId(1002);
            when(passwordEncoder.encode(any())).thenReturn("h");
            when(userProductMapper.countActiveByUserId(1002)).thenReturn(0L);
            when(companyMapper.selectById(1)).thenReturn(existing);
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            LoginResponse res = authService.register(req);

            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.GENERAL_USER);
        }

        @Test
        void register_with_invitation_code_uses_company_from_invitation_and_consumes_code() {
            RegisterRequest req = altRequest();
            req.setInvitationCode("INV-ABC");

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
            when(jwtService.generateToken(1002, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            LoginResponse res = authService.register(req);

            assertThat(res.getUser().getCompanyId()).isEqualTo(3001);
            assertThat(res.getUser().getCompanyName()).isEqualTo("Invited Co");

            verify(companyMapper, never()).insert(any());
            verify(companyMapper, never()).selectByName(any());
            verify(invitationCodeService).consumeForUser(eq(inv), any(UserAccount.class));
        }

        @Test
        void register_trims_email_phone_company_invitation_strings() {
            RegisterRequest req = altRequest();
            req.setEmail("  test@test.com  ");
            req.setPhone("  13800000000 ");
            req.setCompanyName(" Demo Company ");
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
            when(jwtService.generateToken(2, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            authService.register(req);

            ArgumentCaptor<UserAccount> cap = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountMapper).insert(cap.capture());
            assertThat(cap.getValue().getEmail()).isEqualTo("test@test.com");
            assertThat(cap.getValue().getPhone()).isEqualTo("13800000000");
        }
    }

    @Nested
    @DisplayName("失败路径")
    class FailureCases {

        @Test
        void email_already_registered() {
            RegisterRequest req = altRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_EMAIL_REGISTERED.getCode());
        }

        @Test
        void phone_already_registered() {
            RegisterRequest req = altRequest();
            when(userAccountMapper.countByEmail(req.getEmail())).thenReturn(0L);
            when(userAccountMapper.countByPhone(req.getPhone())).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_PHONE_REGISTERED.getCode());
        }

        @Test
        void invitation_company_missing() {
            RegisterRequest req = altRequest();
            req.setInvitationCode("INV");

            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            InvitationCode inv = new InvitationCode();
            inv.setCompanyId(999);
            when(invitationCodeService.requireUsableCode("INV")).thenReturn(inv);
            when(companyMapper.selectById(999)).thenReturn(null);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_INVITATION_COMPANY_MISSING.getCode());
        }

        @Test
        void unsupported_company_role_project_owner() {
            RegisterRequest req = altRequest();
            req.setRoleCode("PROJECT_OWNER");

            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            Company existing = new Company();
            existing.setCompanyId(1);
            when(companyMapper.selectByName(any())).thenReturn(existing);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_UNSUPPORTED_USER_ROLE.getCode());
        }

        @Test
        void display_name_required_when_no_display_no_first_last() {
            RegisterRequest req = altRequest();
            req.setDisplayName(null);
            req.setFirstName(null);
            req.setLastName(null);

            when(userAccountMapper.countByEmail(any())).thenReturn(0L);
            when(userAccountMapper.countByPhone(any())).thenReturn(0L);
            Company existing = new Company();
            existing.setCompanyId(1);
            when(companyMapper.selectByName(any())).thenReturn(existing);

            assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_DISPLAY_NAME_REQUIRED.getCode());
        }
    }
}
