package cn.linkfast.dto;

import lombok.Data;

import java.util.List;

/**
 * 代理产品查询条件（DAO层SQL拼接使用）
 */
@Data
public class ProxyProductSearchCondition {
    private String countryCode;        // 可选
    private String cityCode;           // 可选
    private List<Integer> proxyType;   // 可选，为null或空则不限制
    private Integer limit;             // 对应 pageSize
    private Integer offset;            // 对应 (pageNum-1) * pageSize
}
