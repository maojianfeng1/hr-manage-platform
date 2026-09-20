package com.hr.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询统一返回
 *
 * 知识点：前后端分页约定
 * 前端表格（Element Plus el-table + el-pagination）需要两个数据：
 *  - total：符合条件的总记录数（用于算页码）
 *  - list ：当前页的数据行
 * 不要把分页逻辑散落在各接口，统一用 PageResult 承载。
 *
 * @param <T> 行数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private Long total;
    private List<T> list;

    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }
}
