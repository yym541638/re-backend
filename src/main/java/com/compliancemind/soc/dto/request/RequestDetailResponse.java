package com.compliancemind.soc.dto.request;

import com.compliancemind.soc.entity.request.ComplianceRequest;
import com.compliancemind.soc.entity.request.RequestAttachment;
import com.compliancemind.soc.entity.request.RequestVersion;
import lombok.Data;

import java.util.List;

@Data
public class RequestDetailResponse {

    private ComplianceRequest request;
    private List<RequestAttachment> attachments;
    private List<RequestVersion> versions;
}

