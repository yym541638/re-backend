package com.compliancemind.soc.security;

import com.compliancemind.soc.common.constants.SocConstants;

import java.util.Locale;
import java.util.Set;

/**
 * 公司与项目维度角色常量及别名归一化（如 USER → GENERAL_USER）。
 */
public final class RoleCodes {

    public static final String COMPANY_ADMIN = "COMP_ADMIN";
    public static final String DOCUMENT_OWNER = "DOCUMENT_OWNER";
    public static final String GENERAL_USER = "GENERAL_USER";
    public static final String MANAGER = "MANAGER";
    public static final String MANAGER_2 = "MANAGER_2";
    public static final String PROJECT_OWNER = "PROJECT_OWNER";

    private static final Set<String> COMPANY_ROLES = Set.of(
        COMPANY_ADMIN,
        DOCUMENT_OWNER,
        GENERAL_USER,
        MANAGER,
        MANAGER_2
    );

    private static final Set<String> PROJECT_ROLES = Set.of(
        COMPANY_ADMIN,
        DOCUMENT_OWNER,
        GENERAL_USER,
        MANAGER,
        MANAGER_2,
        PROJECT_OWNER
    );

    private static final Set<String> USER_TYPES = Set.of(
        SocConstants.Account.USER_TYPE_CLIENT,
        SocConstants.Account.USER_TYPE_CONSULTANT,
        SocConstants.Account.USER_TYPE_AUDITOR
    );

    private RoleCodes() {
    }

    /**
     * 注册页用户类型（Clients / Consultant / Auditor），与权限 role_code 独立。
     */
    public static String normalizeUserType(String userType) {
        String normalized = normalizeToken(userType);
        return switch (normalized) {
            case "CLIENT", "CLIENTS" -> SocConstants.Account.USER_TYPE_CLIENT;
            case "CONSULTANT", "CONSULTANTS" -> SocConstants.Account.USER_TYPE_CONSULTANT;
            case "AUDITOR", "AUDITORS" -> SocConstants.Account.USER_TYPE_AUDITOR;
            default -> normalized;
        };
    }

    public static boolean isUserType(String userType) {
        return USER_TYPES.contains(normalizeUserType(userType));
    }

    public static String normalizeCompanyRole(String roleCode) {
        String normalized = normalizeToken(roleCode);
        return switch (normalized) {
            case "", "USER", "GENERALUSER", "GENERAL_USER" -> GENERAL_USER;
            case "ADMIN", "COMPADMIN", "COMP_ADMIN", "COMPANYADMIN", "COMPANY_ADMIN", "ADMINISTRATOR" -> COMPANY_ADMIN;
            case "DOCUMENTOWNER", "DOCUMENT_OWNER" -> DOCUMENT_OWNER;
            case "MANAGER", "MANAGERTIER1", "MANAGER_TIER1", "MANAGER_TIER_1" -> MANAGER;
            case "MANAGER2", "MANAGER_2", "MANAGERTIER2", "MANAGER_TIER2", "MANAGER_TIER_2" -> MANAGER_2;
            default -> normalized;
        };
    }

    public static String normalizeProjectRole(String roleCode) {
        String normalized = normalizeCompanyRole(roleCode);
        if ("PROJECTOWNER".equals(normalized) || "PROJECT_OWNER".equals(normalized)) {
            return PROJECT_OWNER;
        }
        if ("ADMINISTRATOR".equals(normalized)) {
            return COMPANY_ADMIN;
        }
        if ("1ST_TIER_MANAGER_USER".equals(normalized)
            || "FIRST_TIER_MANAGER_USER".equals(normalized)
            || "MANAGER_TIER_1".equals(normalized)) {
            return MANAGER;
        }
        if ("2ND_TIER_MANAGER_USER".equals(normalized)
            || "2ND_TIER_MANAGER_USER_1".equals(normalized)
            || "SECOND_TIER_MANAGER_USER".equals(normalized)
            || "MANAGER_TIER_2".equals(normalized)) {
            return MANAGER_2;
        }
        return normalized.isBlank() ? GENERAL_USER : normalized;
    }

    public static boolean isCompanyRole(String roleCode) {
        return COMPANY_ROLES.contains(normalizeCompanyRole(roleCode));
    }

    public static boolean isProjectRole(String roleCode) {
        return PROJECT_ROLES.contains(normalizeProjectRole(roleCode));
    }

    public static boolean canManageCompany(String roleCode) {
        return COMPANY_ADMIN.equals(normalizeCompanyRole(roleCode));
    }

    public static boolean canAccessAllProjects(String roleCode) {
        String normalized = normalizeCompanyRole(roleCode);
        return COMPANY_ADMIN.equals(normalized) || MANAGER.equals(normalized) || MANAGER_2.equals(normalized);
    }

    public static boolean canManageProject(String roleCode) {
        String normalized = normalizeProjectRole(roleCode);
        return COMPANY_ADMIN.equals(normalized)
            || MANAGER.equals(normalized)
            || MANAGER_2.equals(normalized)
            || PROJECT_OWNER.equals(normalized);
    }

    public static boolean canEditProjectContent(String roleCode) {
        String normalized = normalizeProjectRole(roleCode);
        return COMPANY_ADMIN.equals(normalized)
            || GENERAL_USER.equals(normalized)
            || MANAGER.equals(normalized)
            || MANAGER_2.equals(normalized)
            || PROJECT_OWNER.equals(normalized);
    }

    private static String normalizeToken(String roleCode) {
        if (roleCode == null) {
            return "";
        }
        return roleCode.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);
    }
}
