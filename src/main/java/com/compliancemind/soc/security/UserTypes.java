package com.compliancemind.soc.security;

import java.util.Locale;
import java.util.Set;

/**
 * 用户类型（Clients / Consultant / Auditor），与公司权限 {@link RoleCodes} 分离。
 */
public final class UserTypes {

    public static final String CLIENT = "CLIENT";
    public static final String CONSULTANT = "CONSULTANT";
    public static final String AUDITOR = "AUDITOR";

    private static final Set<String> ALL = Set.of(CLIENT, CONSULTANT, AUDITOR);

    private UserTypes() {
    }

    public static boolean isUserType(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = normalizeToken(value);
        return switch (normalized) {
            case "CLIENT", "CLIENTS", "CUSTOMER" -> true;
            case "CONSULTANT", "CONSULTANTS" -> true;
            case "AUDITOR", "AUDITORS" -> true;
            default -> ALL.contains(normalized);
        };
    }

    /** 归一化用户类型；空值默认 {@link #CLIENT}。非法值抛给调用方自行处理时可先用 {@link #isUserType}。 */
    public static String normalize(String value) {
        String normalized = normalizeToken(value);
        return switch (normalized) {
            case "", "CLIENT", "CLIENTS", "CUSTOMER" -> CLIENT;
            case "CONSULTANT", "CONSULTANTS" -> CONSULTANT;
            case "AUDITOR", "AUDITORS" -> AUDITOR;
            default -> normalized;
        };
    }

    public static boolean isSupported(String value) {
        return ALL.contains(normalize(value));
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        int paren = text.indexOf('(');
        if (paren > 0) {
            text = text.substring(0, paren).trim();
        }
        return text.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}
