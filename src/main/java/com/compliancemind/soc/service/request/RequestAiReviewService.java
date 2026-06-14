package com.compliancemind.soc.service.request;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.entity.request.RequestAttachment;
import org.springframework.stereotype.Service;

import java.util.List;

/** Request Individual 证据 AI 审核（规则 + 可扩展 LLM）。 */
@Service
public class RequestAiReviewService {

    public AiReviewResult review(List<RequestAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new AiReviewResult(
                SocConstants.RequestIndividual.AI_REVIEW_RED,
                "No evidence uploaded. Please upload supporting documents before sending the request."
            );
        }
        boolean hasPdfOrDoc = attachments.stream()
            .anyMatch(item -> item.getFileType() != null
                && (item.getFileType().contains("pdf")
                || item.getFileType().contains("doc")
                || item.getFileType().contains("xls")));
        if (!hasPdfOrDoc) {
            return new AiReviewResult(
                SocConstants.RequestIndividual.AI_REVIEW_YELLOW,
                "Evidence uploaded but file types may need review. Prefer PDF, Word, or Excel documents."
            );
        }
        return new AiReviewResult(
            SocConstants.RequestIndividual.AI_REVIEW_GREEN,
            "Evidence documents uploaded and appear acceptable for initial AI review."
        );
    }

    public record AiReviewResult(String status, String comment) {
    }
}
