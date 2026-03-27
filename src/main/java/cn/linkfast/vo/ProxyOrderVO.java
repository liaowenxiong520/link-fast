package cn.linkfast.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表展示VO（前端需要的字段）
 */
@Data
public class ProxyOrderVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台订单号
     */
    private String orderNo;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单对应实例总数量
     */
    private Integer instanceTotal;

    /**
     * 买家用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;
}