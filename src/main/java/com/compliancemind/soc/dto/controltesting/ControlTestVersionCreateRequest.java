package com.compliancemind.soc.dto.controltesting;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ControlTestVersionCreateRequest {

    @NotBlank(message = "版本说明不能为空")
    private String changeSummary;
}

