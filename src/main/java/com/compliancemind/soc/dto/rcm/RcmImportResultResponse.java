package com.compliancemind.soc.dto.rcm;

import lombok.Data;

import java.util.List;

@Data
public class RcmImportResultResponse {

    private int total;
    private int success;
    private int failed;
    private List<String> errors;
}

