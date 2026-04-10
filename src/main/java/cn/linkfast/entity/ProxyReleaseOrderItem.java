package cn.linkfast.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 代理释放订单明细表实体类
 * 对应表：proxy_release_order_item
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/29
 */
@Data
public class ProxyReleaseOrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** 代理释放订单的id */
    private Long orderId;

    /** 代理释放订单的订单号 */
    private String orderNo;

    /** 代理释放订单的渠道商订单号 */
    private String appOrderNo;

    /** 实例编号 */
    private String instanceNo;

    /** 代理实例花费总金额 */
    private BigDecimal totalAmount;

    private Date createTime;
    private Date updateTime;
}
