package com.compliancemind.soc.security;

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

    private RoleCodes() {
    }

    public static String normalizeCompanyRole(String roleCode) {
        String normalized = normalizeToken(roleCode);
        return switch (normalized) {
            case "", "USER", "GENERALUSER", "GENERAL_USER" -> GENERAL_USER;
            case "ADMIN", "COMPADMIN", "COMP_ADMIN", "COMPANYADMIN", "COMPANY_ADMIN", "ADMINISTRATOR" -> COMPANY_ADMIN;
            case "DOCUMENTOWNER", "DOCUMENT_OWNER" -> DOCUMENT_OWNER;
            case "AUDITOR", "MANAGER" -> MANAGER;
            case "MANAGER2", "MANAGER_2" -> MANAGER_2;
            default -> normalized;
        };
    }

    public static String normalizeProjectRole(String roleCode) {
        String normalized = normalizeCompanyRole(roleCode);
        if ("PROJECTOWNER".equals(normalized) || "PROJECT_OWNER".equals(normalized)) {
            return PROJECT_OWNER;
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
