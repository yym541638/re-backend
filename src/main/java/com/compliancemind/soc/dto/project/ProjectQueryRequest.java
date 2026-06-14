package com.compliancemind.soc.dto.project;

import lombok.Data;

@Data
public class ProjectQueryRequest {

    private String keyword;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
    /** 由服务层根据 pageNum、pageSize 计算，勿由客户端传入。 */
    private Long offset;
}
