package com.compliancemind.soc.auth;

import com.compliancemind.soc.controller.auth.AuthController;
import com.compliancemind.soc.dto.auth.LoginResponse;
import com.compliancemind.soc.service.auth.AuthService;
import com.compliancemind.soc.common.api.ApiSuccessMessageAdvice;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP 层对照 {@code API_Doc_v4.md} §2.1：路径、请求体验证、响应字段命名（snake_case）。
 * 使用 standalone MockMvc，避免拉起带 {@code @MapperScan} 的全应用上下文。
 */
@ExtendWith(MockitoExtension.class)
class AuthLoginSliceTest {

    @Mock
    AuthService authService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MessageSource messageSource = messageSource();
        AuthController controller = new AuthController(authService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .setControllerAdvice(
                new GlobalExceptionHandler(messageSource),
                new ApiSuccessMessageAdvice(messageSource))
            .build();
    }

    private static MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Test
    @DisplayName("POST /auth/login 成功时 JSON 含 PRD 所列 snake_case 字段（网关前缀 /api 由部署约定）")
    void login_success_json_matches_api_doc_shape() throws Exception {
        LoginResponse data = sampleLoginResponse();
        when(authService.login(any())).thenReturn(data);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"test@test.com\",\"password\":\"Test@123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.expire_in").value(7200))
            .andExpect(jsonPath("$.data.purchase_status").value(1))
            .andExpect(jsonPath("$.data.redirect_to").value("order"))
            .andExpect(jsonPath("$.data.user_info.id").value(1001))
            .andExpect(jsonPath("$.data.user_info.company_id").value(2001))
            .andExpect(jsonPath("$.data.user_info.company_name").value("Demo Company"))
            .andExpect(jsonPath("$.data.user_info.username").value("George Yao"))
            .andExpect(jsonPath("$.data.user_info.email").value("test@test.com"))
            .andExpect(jsonPath("$.data.user_info.phone").value("13800000000"))
            .andExpect(jsonPath("$.data.user_info.avatar_url").value(""))
            .andExpect(jsonPath("$.data.user_info.job_title").value("Auditor"))
            .andExpect(jsonPath("$.data.user_info.role").value("GENERAL_USER"));
    }

    @Test
    @DisplayName("account 为空：业务包装 code=400")
    void login_blank_account_bad_request() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"\",\"password\":\"x\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("password 为空：400")
    void login_blank_password_bad_request() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"a@b.com\",\"password\":\"\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("账号不存在：401 + i18n 文案键解析")
    void login_account_not_found_returns_401() throws Exception {
        doThrow(new BizException(BizErrorCode.AUTH_ACCOUNT_NOT_FOUND))
            .when(authService).login(any());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"nobody@test.com\",\"password\":\"x\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("账号不存在"))
            .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("密码错误：401")
    void login_password_mismatch_returns_401() throws Exception {
        doThrow(new BizException(BizErrorCode.AUTH_PASSWORD_MISMATCH))
            .when(authService).login(any());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"test@test.com\",\"password\":\"wrong\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    @DisplayName("非法 JSON：400")
    void login_malformed_json_bad_request() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("account 仅空白字符：校验失败 → 400")
    void login_whitespace_only_account_bad_request() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"   \",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    private static LoginResponse sampleLoginResponse() {
        LoginResponse r = new LoginResponse();
        r.setToken("jwt-token");
        r.setExpireSeconds(7200L);
        r.setPurchaseStatus(1);
        r.setRedirectTo("order");
        LoginResponse.UserInfo u = new LoginResponse.UserInfo();
        u.setUserId(1001);
        u.setCompanyId(2001);
        u.setCompanyName("Demo Company");
        u.setDisplayName("George Yao");
        u.setEmail("test@test.com");
        u.setPhone("13800000000");
        u.setAvatarUrl("");
        u.setJobTitle("Auditor");
        u.setRoleCode("GENERAL_USER");
        r.setUser(u);
        return r;
    }
}
