package com.compliancemind.soc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestVersionCreateRequest {

    @NotBlank(message = "版本说明不能为空")
    private String changeSummary;
}

