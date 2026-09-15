package com.compliancemind.soc.service.auth;

import com.compliancemind.soc.dto.auth.LoginRequest;
import com.compliancemind.soc.dto.auth.LoginResponse;
import com.compliancemind.soc.dto.auth.RegisterRequest;
import com.compliancemind.soc.dto.commerce.CompanyProfileResponse;
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
import com.compliancemind.soc.security.UserTypes;
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
        boolean hasInvitation = request.getInvitationCode() != null
            && !request.getInvitationCode().isBlank();
        if (hasInvitation) {
            // 加入公司：必须持有可用邀请码，公司由邀请码绑定
            invitationCode = invitationCodeService.requireUsableCode(request.getInvitationCode().trim());
            company = companyMapper.selectById(invitationCode.getCompanyId());
            if (company == null) {
                throw new BizException(BizErrorCode.AUTH_INVITATION_COMPANY_MISSING);
            }
        } else {
            // 开户：创建新公司；同名（忽略大小写）禁止挂靠，须走邀请码加入
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new BizException(BizErrorCode.COMMON_BAD_REQUEST);
            }
            String companyName = request.getCompanyName().trim();
            Company existing = companyMapper.selectByName(companyName);
            if (existing != null) {
                throw new BizException(BizErrorCode.AUTH_COMPANY_ALREADY_EXISTS);
            }
            company = new Company();
            company.setCompanyName(companyName);
            companyMapper.insert(company);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setCompanyId(company.getCompanyId());
        userAccount.setDisplayName(resolveDisplayName(request));
        userAccount.setEmail(request.getEmail().trim());
        userAccount.setPhone(request.getPhone().trim());
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // 开户强制 SYS_ADMIN；邀请加入默认 SYS_USER（或邀请码指定的系统角色）
        String permissionCode = resolvePermissionCode(request, invitationCode);
        // 有邀请码时业务身份不再由注册页选择，统一默认 CLIENT
        String userType = hasInvitation ? UserTypes.CLIENT : resolveUserType(request);
        ensureCompanyAdminAssignable(company.getCompanyId(), permissionCode);
        userAccount.setRoleCode(permissionCode);
        userAccount.setUserType(userType);
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
     * 根据邀请码返回关联公司的完整资料（注册页展示用）。
     */
    public CompanyProfileResponse getCompanyByInvitationCode(String code) {
        Company company = invitationCodeService.resolveCompanyByCode(code);
        return toCompanyProfileResponse(company);
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
        // 个人已购，或同公司任一账号已购（邀请加入的同事可共享公司套餐权益）
        long activeProducts = userProductMapper.countActiveByUserId(userAccount.getUserId());
        if (activeProducts <= 0 && userAccount.getCompanyId() != null) {
            activeProducts = userProductMapper.countActiveByCompanyId(userAccount.getCompanyId());
        }
        // 购买状态：1=已购买（含公司共享），0=未购买
        response.setPurchaseStatus(activeProducts > 0 ? 1 : 0);
        // 登录后建议跳转：已购 → 业务页，未购 → 支付页
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
        // 用户类型：CLIENT / CONSULTANT / AUDITOR
        userInfo.setUserType(UserTypes.normalize(userAccount.getUserType()));
        // 归一化后的权限，序列化为 user_info.role
        userInfo.setRoleCode(roleCode);
        // 双层权限：系统角色 SYS_ADMIN / SYS_USER
        String systemRole = RoleCodes.toSystemRole(roleCode);
        userInfo.setSystemRole(systemRole);
        // 旧前端兼容：permissionCode=administrator → SYS_ADMIN
        userInfo.setPermissionCode(RoleCodes.SYSTEM_ADMIN.equals(systemRole) ? "administrator" : "user");
        response.setUser(userInfo);
        return response;
    }

    private String resolveUserType(RegisterRequest request) {
        String raw = request.getUserType();
        if (raw == null || raw.isBlank()) {
            // 前端常把 Clients 放在 roleCode
            if (UserTypes.isUserType(request.getRoleCode())) {
                raw = request.getRoleCode();
            }
        }
        String normalized = UserTypes.normalize(raw);
        if (!UserTypes.isSupported(normalized)) {
            throw new BizException(BizErrorCode.AUTH_UNSUPPORTED_USER_TYPE);
        }
        return normalized;
    }

    /**
     * 解析系统角色。
     * <ul>
     *   <li>无邀请码（开户）：强制 {@link RoleCodes#SYSTEM_ADMIN}，忽略客户端传入的角色。</li>
     *   <li>有邀请码（加入）：忽略客户端自选角色；优先邀请码 {@code member_role} 中的系统角色，否则 {@link RoleCodes#SYSTEM_USER}。</li>
     * </ul>
     */
    private String resolvePermissionCode(RegisterRequest request, InvitationCode invitationCode) {
        if (invitationCode == null) {
            return RoleCodes.SYSTEM_ADMIN;
        }
        if (invitationCode.getMemberRole() != null && !invitationCode.getMemberRole().isBlank()) {
            String fromInvite = RoleCodes.normalizeSystemRole(invitationCode.getMemberRole());
            if (RoleCodes.SYSTEM_ADMIN.equals(fromInvite) || RoleCodes.SYSTEM_USER.equals(fromInvite)) {
                return fromInvite;
            }
        }
        return RoleCodes.SYSTEM_USER;
    }

    private void ensureCompanyAdminAssignable(Integer companyId, String permissionCode) {
        if (!RoleCodes.SYSTEM_ADMIN.equals(permissionCode)) {
            return;
        }
        if (userAccountMapper.countByCompanyIdAndRoleCode(companyId, RoleCodes.SYSTEM_ADMIN) > 0) {
            throw new BizException(BizErrorCode.AUTH_COMPANY_ADMIN_EXISTS);
        }
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

    private CompanyProfileResponse toCompanyProfileResponse(Company company) {
        CompanyProfileResponse response = new CompanyProfileResponse();
        response.setCompanyId(company.getCompanyId());
        response.setCompanyName(company.getCompanyName());
        response.setCompanyCode(company.getCompanyCode());
        response.setIndustry(company.getIndustry());
        response.setWebsite(company.getWebsite());
        response.setContactName(company.getContactName());
        response.setContactPhone(company.getContactPhone());
        response.setAddress(company.getAddress());
        return response;
    }
}
