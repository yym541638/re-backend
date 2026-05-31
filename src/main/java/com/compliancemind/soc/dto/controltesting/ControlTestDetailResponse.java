package com.compliancemind.soc.dto.controltesting;

import com.compliancemind.soc.entity.controltesting.ControlTest;
import com.compliancemind.soc.entity.controltesting.ControlTestVersion;
import lombok.Data;

import java.util.List;

@Data
public class ControlTestDetailResponse {

    private ControlTest controlTest;
    private List<ControlTestVersion> versions;
}

