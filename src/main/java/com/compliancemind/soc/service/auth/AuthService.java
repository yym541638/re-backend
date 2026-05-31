package com.compliancemind.soc.service.auth;

import com.compliancemind.soc.dto.auth.LoginRequest;
import com.compliancemind.soc.dto.auth.LoginResponse;
import com.compliancemind.soc.dto.auth.RegisterRequest;
import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.invitation.InvitationCode;
import com.compliancemind.soc.service.invitation.InvitationCodeService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.JwtService;
import com.compliancemind.soc.security.RoleCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证：登录校验、注册（含邀请码 joining 公司）、会话信息构建。
 */
@Service
public class AuthService {

    private final UserAccountMapper userAccountMapper;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserAccessor currentUserAccessor;
    private final UserProductMapper userProductMapper;
    private final InvitationCodeService invitationCodeService;

    @Value("${app.jwt.expire-seconds}")
    private long expireSeconds;

    public AuthService(UserAccountMapper userAccountMapper,
                       CompanyMapper companyMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       CurrentUserAccessor currentUserAccessor,
                       UserProductMapper userProductMapper,
                       InvitationCodeService invitationCodeService) {
        this.userAccountMapper = userAccountMapper;
        this.companyMapper = companyMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserAccessor = currentUserAccessor;
        this.userProductMapper = userProductMapper;
        this.invitationCodeService = invitationCodeService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountMapper.selectByAccount(request.getAccount().trim());
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), userAccount.getPasswordHash())) {
            throw new BizException(BizErrorCode.AUTH_PASSWORD_MISMATCH);
        }
        return buildLoginResponse(userAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        //验证邮箱唯一性
        if (userAccountMapper.countByEmail(request.getEmail().trim()) > 0) {
            throw new BizException(BizErrorCode.AUTH_EMAIL_REGISTERED);
        }
        //验证电话唯一性
        if (userAccountMapper.countByPhone(request.getPhone().trim()) > 0) {
            throw new BizException(BizErrorCode.AUTH_PHONE_REGISTERED);
        }

        InvitationCode invitationCode = null;
        Company company;
        if (request.getInvitationCode() != null && !request.getInvitationCode().isBlank()) {
            //验证邀请码是否在系统中存在（邀请码应该是和一个公司绑定的一对一）
            invitationCode = invitationCodeService.requireUsableCode(request.getInvitationCode().trim());
            company = companyMapper.selectById(invitationCode.getCompanyId());
            if (company == null) {
                throw new BizException(BizErrorCode.AUTH_INVITATION_COMPANY_MISSING);
            }
        } else {
            //查询公司主体 是否存在
            company = companyMapper.selectByName(request.getCompanyName().trim());
            //不存在新增
            if (company == null) {
                company = new Company();
                company.setCompanyName(request.getCompanyName().trim());
                companyMapper.insert(company);
            }
        }
           //存在就创建用户账号
        UserAccount userAccount = new UserAccount();
        userAccount.setCompanyId(company.getCompanyId());
        userAccount.setDisplayName(resolveDisplayName(request));
        userAccount.setEmail(request.getEmail().trim());
        userAccount.setPhone(request.getPhone().trim());
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        //userAccount.setRoleCode(normalizeRole(request.getRoleCode()));
        //todo 角色就真的只是角色，权限是在permissions给的？？？
        userAccount.setRoleCode(request.getRoleCode());
        userAccount.setStatus(SocConstants.Account.STATUS_ENABLED);
        userAccountMapper.insert(userAccount);
        // 注册时若填写了邀请码，消费邀请码并将用户加入对应项目
        if (invitationCode != null) {
            invitationCodeService.consumeForUser(invitationCode, userAccount);
        }

        return buildLoginResponse(userAccount);
    }

    public LoginResponse me() {
        Integer userId = currentUserAccessor.requireUserId();
        UserAccount userAccount = userAccountMapper.selectById(userId);
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_USER_NOT_FOUND);
        }
        return buildLoginResponse(userAccount);
    }

    /**
     * 组装登录/注册成功后的统一响应：JWT、购买状态、前端跳转及用户概要信息。
     * <p>由 {@link #login}、{@link #register}、{@link #me} 共用。</p>
     */
    private LoginResponse buildLoginResponse(UserAccount userAccount) {
        // 根据用户所属公司 ID 查询企业信息，用于填充 company_name
        Company company = companyMapper.selectById(userAccount.getCompanyId());
        // 将库中角色编码归一化为公司维度标准角色（如 USER → GENERAL_USER）
        String roleCode = RoleCodes.normalizeCompanyRole(userAccount.getRoleCode());
        LoginResponse response = new LoginResponse();
        // 签发 JWT，claims 含 userId、username（展示名）、roleCode
        response.setToken(jwtService.generateToken(userAccount.getUserId(), userAccount.getDisplayName(), roleCode));
        // token 有效时长（秒），来自配置 app.jwt.expire-seconds
        response.setExpireSeconds(expireSeconds);
        // 统计用户当前生效中的已购产品数量
        long activeProducts = userProductMapper.countActiveByUserId(userAccount.getUserId());
        // 购买状态：1=已购买，0=未购买（供前端展示或逻辑判断）
        response.setPurchaseStatus(activeProducts > 0 ? 1 : 0);
        // 登录后建议跳转页：已购 → 订单/业务页 order，未购 → 支付页 payment
        response.setRedirectTo(activeProducts > 0 ? "order" : "payment");

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        // 用户主键，序列化为 user_info.id
        userInfo.setUserId(userAccount.getUserId());
        // 所属企业 ID
        userInfo.setCompanyId(userAccount.getCompanyId());
        // 企业名称；公司记录缺失时返回空字符串避免 NPE
        userInfo.setCompanyName(company == null ? "" : company.getCompanyName());
        // 用户展示名，序列化为 user_info.username
        userInfo.setDisplayName(userAccount.getDisplayName());
        userInfo.setEmail(userAccount.getEmail());
        userInfo.setPhone(userAccount.getPhone());
        userInfo.setAvatarUrl(userAccount.getAvatarUrl());
        userInfo.setJobTitle(userAccount.getJobTitle());
        // 归一化后的角色，序列化为 user_info.role
        userInfo.setRoleCode(roleCode);
        response.setUser(userInfo);
        return response;
    }

    private String normalizeRole(String roleCode) {
        String normalized = RoleCodes.normalizeCompanyRole(roleCode);
        if (!RoleCodes.isCompanyRole(normalized)) {
            throw new BizException(BizErrorCode.AUTH_UNSUPPORTED_USER_ROLE);
        }
        return normalized;
    }

    private String resolveDisplayName(RegisterRequest request) {
        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            return request.getDisplayName().trim();
        }
        String firstName = request.getFirstName() == null ? "" : request.getFirstName().trim();
        String lastName = request.getLastName() == null ? "" : request.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        throw new BizException(BizErrorCode.AUTH_DISPLAY_NAME_REQUIRED);
    }
}
