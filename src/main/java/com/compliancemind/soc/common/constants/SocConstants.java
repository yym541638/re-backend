package com.compliancemind.soc.common.constants;

import java.math.BigDecimal;

/**
 * 全工程魔法值汇聚：订单/RCM/安全白名单等嵌套常量，优先引用嵌套类型避免散落字面量。
 * <p>Application-wide constants; prefer nested types, e.g. {@link Order#STATUS_PENDING}.</p>
 */
public final class SocConstants {

    private SocConstants() {
    }

    /** HTTP layer & public integration. */
    public static final class Http {
        public static final String HEADER_AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    }

    /** Values stored in JWT payloads (must stay stable for clients). */
    public static final class JwtClaim {
        public static final String USER_ID = "userId";
        public static final String USERNAME = "username";
        public static final String ROLE_CODE = "roleCode";
    }

    /** API envelope / gateway conventions. */
    public static final class Api {
        public static final int SUCCESS_CODE = 0;
        public static final String CONTEXT_PREFIX = "/api";
    }

    /** Paths permitted without authentication ({@code HttpSecurity}). */
    public static final class Security {
        public static final String PATH_AUTH_LOGIN = "/auth/login";
        public static final String PATH_AUTH_REGISTER = "/auth/register";
        public static final String PATH_AUTH_COMPANY_BY_INVITATION = "/auth/company-by-invitation";
        public static final String PATH_INVITATION_VALIDATE = "/invitation-code/validate";
        public static final String PATH_PRODUCT_LIST = "/product/list";
        public static final String PATH_PRODUCT_PACKAGES = "/product/packages";
        public static final String PATH_PAYMENT_QUERY = "/payment/query/**";
        public static final String PATH_PAYMENT_NOTIFY = "/payment/notify";
        public static final String PATH_PAYMENT_MOCK_SUCCESS = "/payment/mock/success";

        public static final String[] PERMIT_ALL_PATHS = {
            PATH_AUTH_LOGIN,
            PATH_AUTH_REGISTER,
            PATH_AUTH_COMPANY_BY_INVITATION,
            PATH_INVITATION_VALIDATE,
            PATH_PRODUCT_LIST,
            PATH_PRODUCT_PACKAGES,
            PATH_PAYMENT_QUERY,
            PATH_PAYMENT_NOTIFY,
            PATH_PAYMENT_MOCK_SUCCESS
        };
    }

    /** Common {@link java.time.format.DateTimeFormatter} patterns. */
    public static final class Format {
        public static final String DATETIME_SECONDS = "yyyy-MM-dd HH:mm:ss";
        public static final String COMPACT_TIMESTAMP = "yyyyMMddHHmmss";
    }

    /** Mock payment URLs & redirect query keys (includes servlet {@code context-path} where applicable). */
    public static final class PaymentCallback {
        public static final int MOCK_EXPIRE_MINUTES = 30;
        public static final String MOCK_SUCCESS_URL_PREFIX = "/api/payment/mock/success";
        public static final String ORDER_DETAIL_URL_PREFIX = "/api/order/detail/";
        public static final String PARAM_ORDER_NO = "orderNo";
        public static final String PARAM_PAY_STATUS = "payStatus";
        public static final String PAY_STATUS_SUCCESS = "success";
    }

    /** Commerce orders & payments. */
    public static final class Order {
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_PAID = "PAID";
        public static final String STATUS_FAILED = "FAILED";
        public static final String STATUS_CANCELED = "CANCELED";
        public static final String PAYMENT_METHOD_MOCK = "MOCK";
        public static final String ORDER_NO_PREFIX = "ORD";
        public static final String NOTIFY_STATUS_SUCCESS = "SUCCESS";
        public static final String MOCK_TX_PREFIX = "MOCK";
        /** {@link ThreadLocalRandom#nextInt(int, int)} origin / bound for synthetic order suffix. */
        public static final int ORDER_NO_RANDOM_ORIGIN = 1000;
        public static final int ORDER_NO_RANDOM_BOUND = 9999;
    }

    /** User subscription row ({@code sys_user_product}). */
    public static final class UserProduct {
        public static final String STATUS_ACTIVE = "ACTIVE";
    }

    /** Catalog ({@code sys_product} / package). */
    public static final class Product {
        /** Row enabled flag in DB. */
        public static final int STATUS_ENABLED = 1;
        /** 新用户购买页默认展示的产品编码（SOC 2）。 */
        public static final String CODE_SOC2 = "soc2";
    }

    /** Account entity numeric flags. */
    public static final class Account {
        public static final int STATUS_ENABLED = 1;
        /** 注册页用户类型：Clients。 */
        public static final String USER_TYPE_CLIENT = "CLIENT";
        /** 注册页用户类型：Consultant。 */
        public static final String USER_TYPE_CONSULTANT = "CONSULTANT";
        /** 注册页用户类型：Auditor。 */
        public static final String USER_TYPE_AUDITOR = "AUDITOR";
    }

    /** SOC audit type normalization (internal vs persisted display). */
    public static final class AuditType {
        public static final String INTERNAL_TYPE1 = "TYPE1";
        public static final String INTERNAL_TYPE2 = "TYPE2";
        public static final String DISPLAY_TYPE1 = "Type1";
        public static final String DISPLAY_TYPE2 = "Type2";
    }

    /** Invitation codes. */
    public static final class Invitation {
        public static final String TYPE_PROJECT = "PROJECT";
        public static final String STATUS_ACTIVE = "ACTIVE";
        public static final String STATUS_USED = "USED";
        public static final String STATUS_REVOKED = "REVOKED";
        /** Response payload when code string is unknown (not DB row status). */
        public static final String RESPONSE_CODE_NOT_FOUND = "NOT_FOUND";
        public static final int DEFAULT_MAX_USES = 1;
        public static final String LABEL_GENERIC_ZH = "邀请码";
    }

    /** Async report generation tasks. */
    public static final class ReportTask {
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_PROCESSING = "PROCESSING";
        public static final String STATUS_SUCCESS = "SUCCESS";
        public static final String STATUS_FAILED = "FAILED";
        public static final String DEFAULT_REPORT_TYPE = "summary";
        public static final String DEFAULT_FORMAT = "md";
        public static final String DEFAULT_LANGUAGE = "zh-CN";
        public static final int PROGRESS_INITIAL = 0;
        public static final int PROGRESS_AFTER_SNAPSHOT = 20;
        public static final int PROGRESS_AFTER_DATA = 55;
        public static final int PROGRESS_DONE = 100;
        public static final int EXECUTOR_POOL_SIZE = 2;
        public static final String STORAGE_SUBDIR = "reports";
        public static final String FILE_PREFIX = "report-";
        public static final String FILE_SUFFIX = ".md";
        public static final String CONTENT_TYPE_MARKDOWN_UTF8 = "text/markdown;charset=UTF-8";
        public static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename*=UTF-8''report-";
    }

    /** RCM records & stages. */
    public static final class Rcm {
        public static final String STAGE_MANUAL = "MANUAL";
        public static final String STAGE_FINAL = "FINAL";
        public static final String STAGE_AI_GENERATED = "AI_GENERATED";
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_DRAFT = "DRAFT";
        public static final String STATUS_IMPORTED = "IMPORTED";
        public static final String STATUS_AI_GENERATED = "AI_GENERATED";
        public static final String CONTROL_RISK_MEDIUM = "MEDIUM";
        public static final String CC_DEFAULT_SECURITY = "Security";
        public static final String KEYWORD_AVAILABILITY = "AVAILABILITY";
        public static final String KEYWORD_PRIVACY = "PRIVACY";
        public static final String KEYWORD_CONFIDENTIAL = "CONFIDENTIAL";
        public static final String KEYWORD_PROCESS = "PROCESS";
        public static final String MODULE_AVAILABILITY = "Availability";
        public static final String MODULE_PRIVACY = "Privacy";
        public static final String MODULE_CONFIDENTIALITY = "Confidentiality";
        public static final String MODULE_PROCESSING_INTEGRITY = "Processing Integrity";
        public static final String MODULE_SECURITY = "Security";

        public static final String VERSION_PREFIX = "V";
        /** Prefix when cloning control code from parent request row id. */
        public static final String CONTROL_CODE_FROM_REQUEST_PREFIX = "REQ-";

        /** Legacy placeholder English strings copied onto AI-assisted drafts. */
        public static final class DraftAiText {
            public static final String RISK_PENDING_ANALYSIS = "Pending further risk analysis based on request content";
            public static final String CONTROL_OBJECTIVE_AUTOFILL_ROW = "Auto-complete control objective based on the current RCM item";
            public static final String IMPLEMENTATION_AUTOFILL_ROW = "Auto-complete implementation method based on current RCM content and linked request";
            public static final String EVIDENCE_AUTOFILL_ROW = "Policy files, execution records, system screenshots, and approval records";
            public static final String CONTROL_OBJECTIVE_FROM_REQUEST = "Auto-generate control objective based on request content";
            public static final String IMPLEMENTATION_PENDING = "Pending further completion by AI or manual review";
            public static final String EVIDENCE_PENDING = "Pending completion";
        }
    }

    /** Control testing workflow. */
    public static final class ControlTest {
        public static final String RISK_MEDIUM = "MEDIUM";
        public static final String RESULT_PENDING = "PENDING";
        public static final String RESULT_PASS = "PASS";
        public static final String RESULT_FAIL = "FAIL";
        public static final String RISK_HIGH = "HIGH";
    }

    /** Compliance request documents. */
    public static final class Request {
        public static final String CODE_PREFIX = "REQ";
        public static final String DOCUMENT_STATUS_PENDING = "PENDING";
    }

    /** Gap analysis rows. */
    public static final class GapAnalysis {
        public static final String STATUS_OPEN = "OPEN";
        public static final String RISK_HIGH = "HIGH";
        public static final String RISK_MEDIUM = "MEDIUM";
        public static final String TITLE_DEFAULT = "Gap Analysis";
        public static final String DESCRIPTION_PREFIX_ZH = "控制测试未达到预期结果，需关注：";
        public static final String SUGGESTION_PREFIX_ZH = "建议补充控制流程、责任人和证据材料，重点修复：";
        public static final String DEFAULT_CONTROL_TITLE_ZH = "未命名控制项";
    }

    /** Projects. */
    public static final class Project {
        public static final String CODE_PREFIX = "PRJ";
        public static final String INITIAL_VERSION = "V1";
        public static final int SOFT_DELETE_FLAG = 0;
        /** 项目进行中（新建默认）。 */
        public static final String STATUS_ACTIVE = "Active";
        /** 项目已结束（Passing Scores 打分完成后）。 */
        public static final String STATUS_END = "End";
    }

    /** File storage layout & allowed uploads. */
    public static final class Storage {
        public static final String REQUEST_PATH_PREFIX = "request/";
        public static final String PROJECT_PATH_PREFIX = "project/";
        public static final String EXT_PDF = ".pdf";
        public static final String EXT_DOC = ".doc";
        public static final String EXT_DOCX = ".docx";
        public static final String EXT_XLS = ".xls";
        public static final String EXT_XLSX = ".xlsx";
    }

    /** AI / Ollama integration defaults. */
    public static final class Ai {
        public static final String DEFAULT_COMPLIANCE_FRAMEWORK = "SOC2";
        public static final String CONTROL_RISK_HIGH = "HIGH";
        public static final String CONTROL_RISK_MEDIUM = "MEDIUM";
    }

    /** Pass-rate assessment (PRD thresholds). */
    public static final class Scoring {
        public static final BigDecimal PASS_RATE_GREEN_MIN = new BigDecimal("95");
        public static final BigDecimal PASS_RATE_YELLOW_MIN = new BigDecimal("70");
        public static final String ASSESSMENT_CERTAIN_PASS_ZH = "必过";
        public static final String ASSESSMENT_LIKELY_PASS_ZH = "大概率能过";
        public static final String ASSESSMENT_HIGH_RISK_ZH = "风险较高";
    }

    /** MyBatis aggregate row keys ({@code countByResultStatus}). */
    public static final class SqlAgg {
        public static final String KEY_NAME = "name";
        public static final String KEY_TOTAL = "total";
    }

    /** Unified operation-log dimensions & recurring detail text. */
    public static final class OperationLog {
        public static final class Module {
            public static final String ORDER = "ORDER";
            public static final String PAYMENT = "PAYMENT";
            public static final String PROFILE = "PROFILE";
            public static final String PROJECT = "PROJECT";
            public static final String REQUEST = "REQUEST";
            public static final String RCM = "RCM";
            public static final String CONTROL_TEST = "CONTROL_TEST";
            public static final String INVITATION_CODE = "INVITATION_CODE";
            public static final String REPORT = "REPORT";
            public static final String GAP_ANALYSIS = "GAP_ANALYSIS";
        }

        public static final class Action {
            public static final String CREATE = "CREATE";
            public static final String UPDATE = "UPDATE";
            public static final String DELETE = "DELETE";
            public static final String SAVE_VERSION = "SAVE_VERSION";
            public static final String CANCEL = "CANCEL";
            public static final String UPDATE_MEMBERS = "UPDATE_MEMBERS";
            public static final String IMPORT = "IMPORT";
            public static final String AI_GENERATE = "AI_GENERATE";
            public static final String FILL_BY_AI = "FILL_BY_AI";
            public static final String SYNC_FROM_REQUEST = "SYNC_FROM_REQUEST";
            public static final String PROMOTE_STAGE = "PROMOTE_STAGE";
            public static final String CREATE_TASK = "CREATE_TASK";
            public static final String REGENERATE = "REGENERATE";
            public static final String UPLOAD_ATTACHMENT = "UPLOAD_ATTACHMENT";
            public static final String DELETE_ATTACHMENT = "DELETE_ATTACHMENT";
            public static final String UPDATE_COMPANY = "UPDATE_COMPANY";
            public static final String REVOKE = "REVOKE";
            public static final String PAID = "PAID";
            public static final String FAILED = "FAILED";
        }

        public static final class EntityType {
            public static final String ORDER = "ORDER";
            public static final String PROJECT = "PROJECT";
            public static final String REQUEST = "REQUEST";
            public static final String RCM = "RCM";
            public static final String CONTROL_TEST = "CONTROL_TEST";
            public static final String INVITATION_CODE = "INVITATION_CODE";
            public static final String USER = "USER";
            public static final String COMPANY = "COMPANY";
        }

        public static final class Detail {
            public static final String ORDER_CREATE_EN = "Create order";
            public static final String ORDER_CANCEL_EN = "Cancel order";
            public static final String PAYMENT_SUCCESS_EN = "Payment success";
            public static final String PAYMENT_FAILED_EN = "Payment failed";
            public static final String PROFILE_UPDATE_USER_ZH = "更新个人资料";
            public static final String PROFILE_UPDATE_COMPANY_ZH = "更新公司资料";
            public static final String PROJECT_CREATE_ZH = "创建项目";
            public static final String PROJECT_UPDATE_ZH = "编辑项目";
            public static final String PROJECT_DELETE_ZH = "删除项目";
            public static final String PROJECT_UPDATE_MEMBERS_ZH = "更新项目成员";
            public static final String GAP_REGENERATE_ZH = "重新生成差距分析";
            public static final String REPORT_CREATE_TASK_ZH = "创建报告任务";
            public static final String INVITATION_REVOKE_ZH = "撤销邀请码";
            public static final String RCM_IMPORT_TITLE = "RCM Import";
            public static final String RCM_IMPORT_DETAIL_EN = "Import RCM Excel";
            public static final String RCM_AI_TITLE = "AI RCM";
            public static final String RCM_AI_DETAIL_EN = "Generate RCM by AI";
            public static final String REQUEST_CREATE_EN = "Create request";
            public static final String REQUEST_UPDATE_EN = "Update request";
            public static final String REQUEST_DELETE_EN = "Delete request";
            public static final String CONTROL_TEST_CREATE_EN = "Create control test";
            public static final String CONTROL_TEST_UPDATE_EN = "Update control test";
            public static final String CONTROL_TEST_DELETE_EN = "Delete control test";
            public static final String RCM_CREATE_EN = "Create RCM";
            public static final String RCM_UPDATE_EN = "Update RCM";
            public static final String RCM_DELETE_EN = "Delete RCM";
            public static final String RCM_FILL_AI_EN = "Fill single RCM by AI";
            public static final String RCM_SNAPSHOT_INITIAL_VERSION_EN = "Initial version";
            public static final String RCM_SNAPSHOT_UPDATE_FALLBACK_EN = "Update RCM";
            public static final String RCM_SNAPSHOT_IMPORT_EXCEL_EN = "Import from Excel";
            public static final String RCM_SNAPSHOT_GENERATE_AI_EN = "Generate by AI";
            public static final String RCM_SNAPSHOT_FILL_AI_ROW_EN = "Fill by AI";
            public static final String RCM_PROMOTE_FINAL_EN = "Promote to FINAL";
            public static final String RCM_PROMOTE_MANUAL_EN = "Promote to MANUAL";
            public static final String RCM_SNAPSHOT_SYNC_GENERATE_EN = "Generate AI draft from request";
            public static final String RCM_SNAPSHOT_SYNC_UPDATE_EN = "Update AI draft from request";
            public static final String RCM_SYNC_GENERATE_ZH = "Generate AI draft from request";
            public static final String RCM_SYNC_UPDATE_ZH = "Update AI draft from request";
            public static final String REQUEST_UPLOAD_ATTACHMENT_PREFIX_EN = "Upload attachment: ";
            public static final String REQUEST_DELETE_ATTACHMENT_PREFIX_EN = "Delete attachment: ";
            public static final String INVITE_CREATE_PREFIX_ZH = "创建邀请码：";
        }
    }

    /** Keys passed to {@link org.springframework.context.MessageSource}. */
    public static final class MessageKeys {
        public static final String INVITATION_VALIDATE_AVAILABLE = "invitation.validate.available";
    }
}
