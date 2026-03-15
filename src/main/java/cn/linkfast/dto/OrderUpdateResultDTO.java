package cn.linkfast.dto;

import lombok.Data;

/**
 * 订单更新结果返回类，封装订单主表、子表的更新条数，支持扩展通用返回字段
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/11 09:35
 */
@Data
public class OrderUpdateResultDTO {
    /**
     * 订单主表更新条数
     */
    private int proxyOrderUpdatedRows;
    /**
     * 代理实例更新条数
     */
    private int proxyInstanceUpdatedRows;

    /**
     * 订单明细表更新条数
     */
    private int proxyOrderItemUpdatedRows;

    // 基础构造器（仅包含核心字段）
    public OrderUpdateResultDTO(int proxyOrderUpdatedRows, int proxyInstanceUpdatedRows, int proxyOrderItemUpdatedRows) {
        this.proxyOrderUpdatedRows = proxyOrderUpdatedRows;
        this.proxyInstanceUpdatedRows = proxyInstanceUpdatedRows;
        this.proxyOrderItemUpdatedRows = proxyOrderItemUpdatedRows;

    }
    // 默认构造器（无参）
    public OrderUpdateResultDTO() {
    }
}