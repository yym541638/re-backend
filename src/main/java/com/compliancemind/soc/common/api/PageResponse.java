package com.compliancemind.soc.common.api;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageResponse<T> {

    private long total;
    private long page;
    private long pageSize;
    private List<T> list;

    public static <T> PageResponse<T> of(long total, long page, long pageSize, List<T> list) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setList(list == null ? Collections.emptyList() : list);
        return response;
    }
}

