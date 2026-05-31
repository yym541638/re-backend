package com.compliancemind.soc.dto.rcm;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RcmVersionCreateRequest {

    @NotBlank(message = "版本说明不能为空")
    private String changeSummary;
}

