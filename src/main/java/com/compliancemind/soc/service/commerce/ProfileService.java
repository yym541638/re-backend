package com.compliancemind.soc.service.commerce;

import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.dto.commerce.CompanyProfileResponse;
import com.compliancemind.soc.dto.commerce.CompanyProfileUpdateRequest;
import com.compliancemind.soc.dto.commerce.ProfileResponse;
import com.compliancemind.soc.dto.commerce.ProfileUpdateRequest;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.RoleCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录用户资料与公司资料读写（按公司隔离）。
 */
@Service
public class ProfileService {

    private final UserAccountMapper userAccountMapper;
    private final CompanyMapper companyMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;

    public ProfileService(UserAccountMapper userAccountMapper,
                          CompanyMapper companyMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          OperationLogService operationLogService) {
        this.userAccountMapper = userAccountMapper;
        this.companyMapper = companyMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
    }

    public ProfileResponse me() {
        UserAccount userAccount = currentUser();
        Company company = companyMapper.selectById(userAccount.getCompanyId());
        ProfileResponse response = new ProfileResponse();
        response.setUserId(userAccount.getUserId());
        response.setDisplayName(userAccount.getDisplayName());
        response.setEmail(userAccount.getEmail());
        response.setPhone(userAccount.getPhone());
        response.setAvatarUrl(userAccount.getAvatarUrl());
        response.setJobTitle(userAccount.getJobTitle());
        response.setRoleCode(userAccount.getRoleCode());
        response.setCompany(toCompanyResponse(company));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public ProfileResponse updateMe(ProfileUpdateRequest request) {
        UserAccount userAccount = currentUser();
        userAccount.setDisplayName(request.getDisplayName().trim());
        userAccount.setEmail(request.getEmail().trim());
        userAccount.setPhone(request.getPhone().trim());
        userAccount.setAvatarUrl(request.getAvatarUrl());
        userAccount.setJobTitle(request.getJobTitle());
        userAccountMapper.updateProfile(userAccount);
        operationLogService.record(SocConstants.OperationLog.Module.PROFILE,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.USER,
            String.valueOf(userAccount.getUserId()),
            userAccount.getDisplayName(),
            null,
            SocConstants.OperationLog.Detail.PROFILE_UPDATE_USER_ZH);
        return me();
    }

    public CompanyProfileResponse company() {
        Company company = currentCompany();
        return toCompanyResponse(company);
    }

    @Transactional(rollbackFor = Exception.class)
    public CompanyProfileResponse updateCompany(CompanyProfileUpdateRequest request) {
        authorizationService.requireCompanyManagement();
        Company company = currentCompany();
        company.setCompanyName(request.getCompanyName().trim());
        company.setCompanyCode(request.getCompanyCode());
        company.setIndustry(request.getIndustry());
        company.setWebsite(request.getWebsite());
        company.setContactName(request.getContactName());
        company.setContactPhone(request.getContactPhone());
        company.setAddress(request.getAddress());
        companyMapper.update(company);
        operationLogService.record(SocConstants.OperationLog.Module.PROFILE,
            SocConstants.OperationLog.Action.UPDATE_COMPANY,
            SocConstants.OperationLog.EntityType.COMPANY,
            String.valueOf(company.getCompanyId()),
            company.getCompanyName(),
            null,
            SocConstants.OperationLog.Detail.PROFILE_UPDATE_COMPANY_ZH);
        return toCompanyResponse(companyMapper.selectById(company.getCompanyId()));
    }

    private Company currentCompany() {
        UserAccount userAccount = currentUser();
        Company company = companyMapper.selectById(userAccount.getCompanyId());
        if (company == null) {
            throw new BizException(BizErrorCode.COMPANY_NOT_FOUND);
        }
        return company;
    }

    private UserAccount currentUser() {
        UserAccount userAccount = userAccountMapper.selectById(currentUserAccessor.requireUserId());
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
        userAccount.setRoleCode(RoleCodes.normalizeCompanyRole(userAccount.getRoleCode()));
        return userAccount;
    }

    private CompanyProfileResponse toCompanyResponse(Company company) {
        CompanyProfileResponse response = new CompanyProfileResponse();
        if (company == null) {
            return response;
        }
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
