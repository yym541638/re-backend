package com.compliancemind.soc.common.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageResponse<T> {

    /** 符合条件的总条数。 */
    private long totalCount;
    private long pageNum;
    private long pageSize;
    private List<T> list;

    /** 兼容读取 {@code total} 字段的前端。 */
    @JsonGetter("total")
    public long getTotal() {
        return totalCount;
    }

    /** 兼容读取 {@code page} 字段的前端。 */
    @JsonGetter("page")
    public long getPage() {
        return pageNum;
    }

    public static <T> PageResponse<T> of(long totalCount, long pageNum, long pageSize, List<T> list) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotalCount(totalCount);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setList(list == null ? Collections.emptyList() : list);
        return response;
    }
}
