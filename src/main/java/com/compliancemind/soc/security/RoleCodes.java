package com.compliancemind.soc.security;

import java.util.Locale;
import java.util.Set;

/**
 * 公司与项目维度角色常量及别名归一化（如 USER → GENERAL_USER）。
 * <p>系统角色统一 {@code SYS_} 前缀，与项目角色（如 {@code COMP_ADMIN}）区分。</p>
 */
public final class RoleCodes {

    /** 系统角色：公司管理员。 */
    public static final String SYSTEM_ADMIN = "SYS_ADMIN";
    /** 系统角色：普通公司用户。 */
    public static final String SYSTEM_USER = "SYS_USER";

    /**
     * 项目角色：Administrator（Access Management 槽位）。
     * <p>勿与系统角色 {@link #SYSTEM_ADMIN} 混淆。</p>
     */
    public static final String COMPANY_ADMIN = "COMP_ADMIN";
    /**
     * @deprecated 系统角色请使用 {@link #SYSTEM_USER}；保留别名避免旧引用编译失败。
     */
    @Deprecated
    public static final String COMPANY_USER = SYSTEM_USER;

    public static final String DOCUMENT_OWNER = "DOCUMENT_OWNER";
    public static final String GENERAL_USER = "GENERAL_USER";
    public static final String MANAGER = "MANAGER";
    public static final String MANAGER_2 = "MANAGER_2";
    public static final String PROJECT_OWNER = "PROJECT_OWNER";

    private static final Set<String> COMPANY_ROLES = Set.of(
        SYSTEM_ADMIN,
        SYSTEM_USER,
        COMPANY_ADMIN,
        DOCUMENT_OWNER,
        GENERAL_USER,
        MANAGER,
        MANAGER_2
    );

    private static final Set<String> SYSTEM_ROLES = Set.of(SYSTEM_ADMIN, SYSTEM_USER);

    private static final Set<String> PROJECT_ROLES = Set.of(
        COMPANY_ADMIN,
        DOCUMENT_OWNER,
        GENERAL_USER,
        MANAGER,
        MANAGER_2,
        PROJECT_OWNER
    );

    private RoleCodes() {
    }

    public static String normalizeCompanyRole(String roleCode) {
        String normalized = normalizeToken(roleCode);
        return switch (normalized) {
            case "", "USER", "GENERALUSER", "GENERAL_USER", "GENERAL" -> GENERAL_USER;
            case "SYSUSER", "SYS_USER",
                 "COMPUSER", "COMP_USER", "COMPANYUSER", "COMPANY_USER" -> SYSTEM_USER;
            case "SYSADMIN", "SYS_ADMIN", "SYSTEMADMIN", "SYSTEM_ADMIN" -> SYSTEM_ADMIN;
            case "ADMIN", "COMPADMIN", "COMP_ADMIN", "COMPANYADMIN", "COMPANY_ADMIN",
                 "ADMINISTRATOR", "ADMINISTRATOR_ONLY_1_ACCOUNT" -> COMPANY_ADMIN;
            case "DOCUMENTOWNER", "DOCUMENT_OWNER", "DOCUMENT" -> DOCUMENT_OWNER;
            case "MANAGER", "MANAGERTIER1", "MANAGER_TIER1", "MANAGER_TIER_1",
                 "1ST_TIER_MANAGER_USER", "FIRST_TIER_MANAGER_USER", "MANAGER_TIER1_USER" -> MANAGER;
            case "MANAGER2", "MANAGER_2", "MANAGERTIER2", "MANAGER_TIER2", "MANAGER_TIER_2",
                 "2ND_TIER_MANAGER_USER", "2ND_TIER_MANAGER_USER_1", "SECOND_TIER_MANAGER_USER",
                 "MANAGER_TIER2_USER" -> MANAGER_2;
            default -> normalized;
        };
    }

    /**
     * 双层权限中的系统角色：仅 {@link #SYSTEM_ADMIN} / {@link #SYSTEM_USER}。
     * <p>历史 {@code COMP_ADMIN} 及细粒度公司角色分别映射为 SYS_ADMIN / SYS_USER。</p>
     */
    public static String toSystemRole(String roleCode) {
        String company = normalizeCompanyRole(roleCode);
        if (SYSTEM_ADMIN.equals(company) || COMPANY_ADMIN.equals(company)) {
            return SYSTEM_ADMIN;
        }
        return SYSTEM_USER;
    }

    /** 归一化并可校验的系统角色（仅接受 Sys Admin / Sys User 及其别名，含旧 COMP_*）。 */
    public static String normalizeSystemRole(String roleCode) {
        String normalized = normalizeToken(roleCode);
        return switch (normalized) {
            case "SYSADMIN", "SYS_ADMIN", "SYSTEMADMIN", "SYSTEM_ADMIN",
                 "ADMIN", "COMPADMIN", "COMP_ADMIN", "COMPANYADMIN", "COMPANY_ADMIN",
                 "ADMINISTRATOR", "ADMINISTRATOR_ONLY_1_ACCOUNT" -> SYSTEM_ADMIN;
            case "SYSUSER", "SYS_USER", "SYSTEMUSER", "SYSTEM_USER",
                 "USER", "COMPUSER", "COMP_USER", "COMPANYUSER", "COMPANY_USER",
                 "GENERALUSER", "GENERAL_USER", "GENERAL" -> SYSTEM_USER;
            default -> normalized;
        };
    }

    /**
     * 是否为注册/变更接口显式传入的系统角色编码（不含历史细粒度 GENERAL_USER 等，避免误归一化）。
     */
    public static boolean isExplicitSystemRoleInput(String roleCode) {
        String normalized = normalizeToken(roleCode);
        return switch (normalized) {
            case "SYSADMIN", "SYS_ADMIN", "SYSTEMADMIN", "SYSTEM_ADMIN",
                 "ADMIN", "COMPADMIN", "COMP_ADMIN", "COMPANYADMIN", "COMPANY_ADMIN",
                 "ADMINISTRATOR", "ADMINISTRATOR_ONLY_1_ACCOUNT",
                 "SYSUSER", "SYS_USER", "SYSTEMUSER", "SYSTEM_USER",
                 "COMPUSER", "COMP_USER", "COMPANYUSER", "COMPANY_USER" -> true;
            default -> false;
        };
    }

    public static boolean isSystemRole(String roleCode) {
        return SYSTEM_ROLES.contains(normalizeSystemRole(roleCode));
    }

    public static String normalizeProjectRole(String roleCode) {
        String normalized = normalizeCompanyRole(roleCode);
        if (SYSTEM_ADMIN.equals(normalized)) {
            // 误把系统角色当作项目角色时，落到 Administrator 槽位
            return COMPANY_ADMIN;
        }
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
        return SYSTEM_ADMIN.equals(toSystemRole(roleCode));
    }

    /**
     * 公司级可见全部项目：仅系统管理员 {@link #SYSTEM_ADMIN}。
     * <p>双层权限下 MANAGER / DOCUMENT_OWNER 等只作为项目成员角色，不再赋予公司级全项目访问。</p>
     */
    public static boolean canAccessAllProjects(String roleCode) {
        return canManageCompany(roleCode);
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
            || SYSTEM_USER.equals(normalized)
            || GENERAL_USER.equals(normalized)
            || MANAGER.equals(normalized)
            || MANAGER_2.equals(normalized)
            || PROJECT_OWNER.equals(normalized);
    }

    private static String normalizeToken(String roleCode) {
        if (roleCode == null) {
            return "";
        }
        String value = roleCode.trim();
        // 前端下拉文案常带说明：administrator(Only 1 account) → administrator
        int paren = value.indexOf('(');
        if (paren > 0) {
            value = value.substring(0, paren).trim();
        }
        return value
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);
    }
}
