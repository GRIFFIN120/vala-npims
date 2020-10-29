package com.vala.base.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchResult<T>{
    // 结果参数
    private Long resultsTotal = Long.valueOf(0);
    private Long resultsCount = Long.valueOf(0);

    // 分页参数
    private Integer pagingNumber;
    private Integer pagingSize;
    private Integer pagingTotal;

    // 排序参数
    private String orderColumn;
    private String orderDirection;

    // 搜索结果
    private List<T> list;

    // 用于显示全部记录
    public SearchResult(Long total, String order_column, String order_direction, List<T> results) {
        this.resultsTotal = total;
        this.resultsCount = total;
        this.pagingNumber = 1;
        this.pagingSize = 0;
        this.pagingTotal = 1;
        this.orderColumn = order_column;
        this.orderDirection = order_direction;
        this.list = results;
    }
}
