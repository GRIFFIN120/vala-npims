package com.vala.base.bean;

import com.vala.commons.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SearchBean<T> {

    private T exact;
    private T fuzzy;

    private Integer page = 1;
    private Integer size = 0;

    private String sortColumn = Constants.DEFAULT_ORDER_COLUMN;
    private String direction = Constants.DIR_DESC;



    // 基础查询
    public SearchBean(T exact) {
        this.exact = exact;
    }
    // 基础查询 + 分页
    public SearchBean(T ext, Integer page, Integer size) {
        this.exact = ext;
        this.page = page<1 ? 1: page;
        this.size = size<0 ? 0: size;
    }
    // 基础查询 + 分页 + 排序
    public SearchBean(T exact, Integer page, Integer size, String sortColumn, String direction) {
        this.exact = exact;
        this.page = page<1 ? 1: page;
        this.size = size<0 ? 0: size;
        this.sortColumn = sortColumn;
        this.direction = direction;
    }


    // 模糊查询
    public SearchBean(T exact, T fuzzy) {
        this.exact = exact;
        this.fuzzy = fuzzy;
    }
    // 模糊查询 + 分页
    public SearchBean(T exact, T fuzzy, Integer page, Integer size) {
        this.exact = exact;
        this.fuzzy = fuzzy;
        this.page = page<1 ? 1: page;
        this.size = size<0 ? 0: size;
    }
    // 模糊查询 + 分页 + 排序
    public SearchBean(T exact, T fuzzy, Integer page, Integer size, String sortColumn, String direction) {
        this.exact = exact;
        this.fuzzy = fuzzy;
        this.page = page<1 ? 1: page;
        this.size = size<0 ? 0: size;
        this.sortColumn = sortColumn;
        this.direction = direction;
    }

}
