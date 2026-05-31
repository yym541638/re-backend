package com.compliancemind.soc.dto.rcm;

import com.compliancemind.soc.entity.rcm.RcmRecord;
import com.compliancemind.soc.entity.rcm.RcmVersion;
import lombok.Data;

import java.util.List;

@Data
public class RcmDetailResponse {

    private RcmRecord rcm;
    private List<RcmVersion> versions;
}

