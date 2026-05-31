package com.compliancemind.soc.auth;

import com.compliancemind.soc.dto.auth.LoginRequest;
import com.compliancemind.soc.dto.auth.LoginResponse;
import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.service.auth.AuthService;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.service.invitation.InvitationCodeService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.JwtService;
import com.compliancemind.soc.security.RoleCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 对照 {@code API_Doc_v4.md} §2.1 登录的业务逻辑覆盖。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    private static final String EMAIL = "test@test.com";
    private static final String PHONE = "13800000000";
    private static final String RAW_PASSWORD = "Test@123456";
    private static final String HASH = "{bcrypt}encoded";

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

    private UserAccount baseUser() {
        UserAccount u = new UserAccount();
        u.setUserId(1001);
        u.setCompanyId(2001);
        u.setDisplayName("George Yao");
        u.setEmail(EMAIL);
        u.setPhone(PHONE);
        u.setAvatarUrl("");
        u.setJobTitle("Auditor");
        u.setPasswordHash(HASH);
        u.setRoleCode(RoleCodes.GENERAL_USER);
        u.setStatus(SocConstants.Account.STATUS_ENABLED);
        return u;
    }

    @Nested
    @DisplayName("成功路径（PRD：返回 token / expire_in / purchase_status / redirect_to / user_info）")
    class SuccessCases {

        @Test
        void login_with_email_returns_contract_fields_and_active_subscription() {
            UserAccount user = baseUser();
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(companyDemo());
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(2L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("jwt-token-mock");

            LoginResponse res = authService.login(loginReq(EMAIL, RAW_PASSWORD));

            assertThat(res.getToken()).isEqualTo("jwt-token-mock");
            assertThat(res.getExpireSeconds()).isEqualTo(7200L);
            assertThat(res.getPurchaseStatus()).isEqualTo(1);
            assertThat(res.getRedirectTo()).isEqualTo("order");
            assertThat(res.getUser().getUserId()).isEqualTo(1001);
            assertThat(res.getUser().getCompanyId()).isEqualTo(2001);
            assertThat(res.getUser().getCompanyName()).isEqualTo("Demo Company");
            assertThat(res.getUser().getDisplayName()).isEqualTo("George Yao");
            assertThat(res.getUser().getEmail()).isEqualTo(EMAIL);
            assertThat(res.getUser().getPhone()).isEqualTo(PHONE);
            assertThat(res.getUser().getAvatarUrl()).isEmpty();
            assertThat(res.getUser().getJobTitle()).isEqualTo("Auditor");
            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.GENERAL_USER);

            verify(jwtService).generateToken(1001, "George Yao", RoleCodes.GENERAL_USER);
        }

        @Test
        void login_with_phone_same_as_email_lookup() {
            UserAccount user = baseUser();
            when(userAccountMapper.selectByAccount(PHONE)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(companyDemo());
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(0L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("jwt-2");

            LoginResponse res = authService.login(loginReq(PHONE, RAW_PASSWORD));

            assertThat(res.getPurchaseStatus()).isZero();
            assertThat(res.getRedirectTo()).isEqualTo("payment");
            assertThat(res.getToken()).isEqualTo("jwt-2");
        }

        @Test
        void login_trims_account_before_lookup() {
            UserAccount user = baseUser();
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(companyDemo());
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(0L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            authService.login(loginReq("  " + EMAIL + "  ", RAW_PASSWORD));

            verify(userAccountMapper).selectByAccount(EMAIL);
        }

        @Test
        void company_missing_user_info_company_name_empty_string() {
            UserAccount user = baseUser();
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(null);
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(0L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            LoginResponse res = authService.login(loginReq(EMAIL, RAW_PASSWORD));

            assertThat(res.getUser().getCompanyName()).isEmpty();
        }

        @Test
        void role_alias_user_normalizes_to_general_user_in_response() {
            UserAccount user = baseUser();
            user.setRoleCode("USER");
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(companyDemo());
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(0L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            LoginResponse res = authService.login(loginReq(EMAIL, RAW_PASSWORD));

            assertThat(res.getUser().getRoleCode()).isEqualTo(RoleCodes.GENERAL_USER);
        }
    }

    @Nested
    @DisplayName("失败与边界")
    class FailureCases {

        @Test
        void account_not_found() {
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(null);

            assertThatThrownBy(() -> authService.login(loginReq(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_ACCOUNT_NOT_FOUND.getCode());
        }

        @Test
        void password_mismatch() {
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(baseUser());
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginReq(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", BizErrorCode.AUTH_PASSWORD_MISMATCH.getCode());
        }

        @Test
        void disabled_account_can_still_login_current_impl() {
            UserAccount user = baseUser();
            user.setStatus(0);
            when(userAccountMapper.selectByAccount(EMAIL)).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
            when(companyMapper.selectById(2001)).thenReturn(companyDemo());
            when(userProductMapper.countActiveByUserId(1001)).thenReturn(0L);
            when(jwtService.generateToken(1001, "George Yao", RoleCodes.GENERAL_USER)).thenReturn("t");

            LoginResponse res = authService.login(loginReq(EMAIL, RAW_PASSWORD));

            assertThat(res.getToken()).isEqualTo("t");
            // PRD/API 文档未写明禁用账号不可登录；当前实现未校验 status，此为行为文档化用例。
        }
    }

    private static Company companyDemo() {
        Company c = new Company();
        c.setCompanyId(2001);
        c.setCompanyName("Demo Company");
        return c;
    }

    private static LoginRequest loginReq(String account, String password) {
        LoginRequest r = new LoginRequest();
        r.setAccount(account);
        r.setPassword(password);
        return r;
    }
}
