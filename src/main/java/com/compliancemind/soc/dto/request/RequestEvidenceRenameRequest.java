package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestEvidenceRenameRequest {

    @NotBlank(message = "文件名不能为空")
    @JsonAlias({"file", "file_name"})
    private String fileName;
}
