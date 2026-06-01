package com.compliancemind.soc.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 全站错误码枚举：接口层使用的数字码 + i18n 文案键。
 * <p>Application-wide error codes and i18n keys.</p>
 */
@Getter
@RequiredArgsConstructor
public enum BizErrorCode {

    COMMON_BAD_REQUEST(400, "error.common.bad_request"),
    COMMON_INTERNAL_ERROR(500, "error.common.internal"),

    AUTH_ACCOUNT_NOT_FOUND(401, "error.auth.account_not_found"),
    AUTH_PASSWORD_MISMATCH(401, "error.auth.password_mismatch"),
    AUTH_EMAIL_REGISTERED(400, "error.auth.email_registered"),
    AUTH_PHONE_REGISTERED(400, "error.auth.phone_registered"),
    AUTH_INVITATION_COMPANY_MISSING(400, "error.auth.invitation_company_missing"),
    AUTH_USER_NOT_FOUND(404, "error.auth.user_not_found"),
    AUTH_UNSUPPORTED_USER_ROLE(400, "error.auth.unsupported_user_role"),
    AUTH_DISPLAY_NAME_REQUIRED(400, "error.auth.display_name_required"),
    AUTH_CURRENT_USER_NOT_FOUND(401, "error.auth.current_user_not_found"),
    AUTH_USER_NOT_LOGGED_IN(401, "error.auth.user_not_logged_in"),

    AUTH_COMPANY_ADMIN_REQUIRED(403, "error.auth.company_admin_required"),
    AUTH_PROJECT_MANAGE_DENIED(403, "error.auth.project_manage_denied"),
    AUTH_PROJECT_READ_ONLY(403, "error.auth.project_read_only"),
    AUTH_USER_NOT_IN_PROJECT(403, "error.auth.user_not_in_project"),

    PROJECT_ID_REQUIRED(400, "error.project.id_required"),
    PROJECT_NOT_FOUND(404, "error.project.not_found"),
    PROJECT_MEMBER_ROLE_UNSUPPORTED(400, "error.project.member_role_unsupported"),

    COMMERCE_PRODUCT_NOT_FOUND(404, "error.commerce.product_not_found"),
    COMMERCE_USER_PRODUCT_NOT_FOUND(404, "error.commerce.user_product_not_found"),
    COMMERCE_PACKAGE_NOT_FOUND(404, "error.commerce.package_not_found"),
    COMMERCE_PRICE_INVALID(400, "error.commerce.price_invalid"),

    RCM_SAVE_VERSION_FAILED(500, "error.rcm.save_version_failed"),
    RCM_NOT_FOUND(404, "error.rcm.not_found"),
    RCM_SNAPSHOT_GENERATION_FAILED(500, "error.rcm.snapshot_failed"),
    RCM_UPLOAD_READ_FAILED(500, "error.rcm.upload_read_failed"),

    CONTROL_TEST_SAVE_VERSION_FAILED(500, "error.control_test.save_version_failed"),
    CONTROL_TEST_NOT_FOUND(404, "error.control_test.not_found"),
    CONTROL_TEST_SNAPSHOT_GENERATION_FAILED(500, "error.control_test.snapshot_failed"),

    REQUEST_SAVE_VERSION_FAILED(500, "error.request.save_version_failed"),
    REQUEST_ATTACHMENT_NOT_FOUND(404, "error.request.attachment_not_found"),
    REQUEST_NOT_FOUND(404, "error.request.not_found"),
    REQUEST_SNAPSHOT_GENERATION_FAILED(500, "error.request.snapshot_failed"),

    REPORT_NOT_READY(400, "error.report.not_ready"),
    REPORT_FILE_NOT_FOUND(404, "error.report.file_not_found"),
    REPORT_DOWNLOAD_FAILED(500, "error.report.download_failed"),
    REPORT_TASK_NOT_FOUND(404, "error.report.task_not_found"),

    ORDER_INVALID_PACKAGE_PRICE(400, "error.order.invalid_package_price"),
    ORDER_CANCEL_INVALID_STATE(400, "error.order.cancel_invalid_state"),
    ORDER_NOT_FOUND(404, "error.order.not_found"),
    ORDER_VIEW_DENIED(403, "error.order.view_denied"),

    COMPANY_NOT_FOUND(404, "error.company.not_found"),

    GAP_ANALYSIS_PROJECT_ID_REQUIRED(400, "error.gap_analysis.project_id_required"),

    INVITATION_NOT_FOUND(404, "error.invitation.not_found"),
    INVITATION_REVOKED(400, "error.invitation.revoked"),
    INVITATION_USED(400, "error.invitation.used"),
    INVITATION_EXPIRED(400, "error.invitation.expired"),
    INVITATION_UNAVAILABLE(400, "error.invitation.unavailable"),

    RCM_EXCEL_PARSE_FAILED(500, "error.rcm.excel.parse_failed"),
    RCM_EXCEL_EXPORT_FAILED(500, "error.rcm.excel.export_failed"),

    STORAGE_FILE_EMPTY(400, "error.storage.file_empty"),
    STORAGE_FILENAME_EMPTY(400, "error.storage.filename_empty"),
    STORAGE_FILE_TYPE_INVALID(400, "error.storage.file_type_invalid"),
    STORAGE_SAVE_FAILED(500, "error.storage.save_failed");

    private final int code;
    private final String messageKey;
}
