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
 * HTTP 层对照 {@code API_Doc_v4.md} §2.2 Register（PRD 常见编号 2.1.3）：路径、两种请求体别名、响应形态。
 */
@ExtendWith(MockitoExtension.class)
class AuthRegisterSliceTest {

    private static final String LEGACY_BODY = """
        {"firstName":"George","lastName":"Yao","email":"test@test.com","password":"Test@123456",\
        "phone":"13800000000","companyName":"Demo Company","permissions":"General User"}\
        """;

    private static final String ALT_BODY = """
        {"displayName":"George Yao","email":"test@test.com","password":"Test@123456",\
        "phone":"13800000000","companyName":"Demo Company","permissionCode":"GENERAL_USER"}\
        """;

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
    @DisplayName("§2.2 legacy 请求体（firstName/lastName + role）→ 响应含 purchase_status=0、job_title 可为空串")
    void register_legacy_json_matches_contract() throws Exception {
        LoginResponse data = sampleRegisterResponse();
        when(authService.register(any())).thenReturn(data);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LEGACY_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.expire_in").value(7200))
            .andExpect(jsonPath("$.data.purchase_status").value(0))
            .andExpect(jsonPath("$.data.redirect_to").value("payment"))
            .andExpect(jsonPath("$.data.user_info.id").value(1002))
            .andExpect(jsonPath("$.data.user_info.username").value("George Yao"))
            .andExpect(jsonPath("$.data.user_info.job_title").value(""))
            .andExpect(jsonPath("$.data.user_info.role").value("GENERAL_USER"));
    }

    @Test
    @DisplayName("§2.2 alt 请求体（displayName + roleCode）")
    void register_alt_json() throws Exception {
        LoginResponse data = sampleRegisterResponse();
        when(authService.register(any())).thenReturn(data);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ALT_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.user_info.username").value("George Yao"));
    }

    @Test
    @DisplayName("邀请码 snake_case：invitation_code 绑定 RegisterRequest")
    void register_invitation_code_alias_json_reaches_service() throws Exception {
        LoginResponse data = sampleRegisterResponse();
        when(authService.register(any())).thenReturn(data);

        String body = """
            {"displayName":"George Yao","email":"new@test.com","password":"Test@123456",\
            "phone":"13900000000","companyName":"Demo Company","permissionCode":"GENERAL_USER",\
            "invitation_code":"INV-1"}\
            """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("密码长度不足：校验失败 → code=400")
    void register_password_too_short() throws Exception {
        String body = ALT_BODY.replace("Test@123456", "12345");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("邮箱格式非法：400")
    void register_invalid_email() throws Exception {
        String body = ALT_BODY.replace("test@test.com", "not-an-email");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("缺少公司名称：400")
    void register_blank_company() throws Exception {
        String body = ALT_BODY.replace("\"Demo Company\"", "\"\"");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("邮箱已注册：400")
    void register_email_taken_returns_biz_error() throws Exception {
        doThrow(new BizException(BizErrorCode.AUTH_EMAIL_REGISTERED))
            .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ALT_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("邮箱已注册"));
    }

    private static LoginResponse sampleRegisterResponse() {
        LoginResponse r = new LoginResponse();
        r.setToken("jwt-token");
        r.setExpireSeconds(7200L);
        r.setPurchaseStatus(0);
        r.setRedirectTo("payment");
        LoginResponse.UserInfo u = new LoginResponse.UserInfo();
        u.setUserId(1002);
        u.setCompanyId(2001);
        u.setCompanyName("Demo Company");
        u.setDisplayName("George Yao");
        u.setEmail("test@test.com");
        u.setPhone("13800000000");
        u.setAvatarUrl("");
        u.setJobTitle("");
        u.setUserType("CLIENT");
        u.setRoleCode("GENERAL_USER");
        r.setUser(u);
        return r;
    }
}
