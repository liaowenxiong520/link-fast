package cn.linkfast.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author liaowenxiong
 * @version 1.0
 * @description TODO
 * @since 2026/3/10 20:34
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyOrder {
    private Long id;
    private String orderNo;       // 平台订单编号
    private String appOrderNo;    // 渠道商订单号
    private Long userId;
    @JsonProperty("type")
    private Integer orderType;    // 订单类型: 1-购买, 2-续费, 3-释放
    private Integer status;       // 状态: 1=待处理, 2=处理中, 3=处理成功, 4=处理失败, 5=部分完成
    @JsonProperty("count")
    private Integer totalQuantity; // 代理购买订单：购买总数，代理续费订单：续费实例总数，代理释放订单：释放实例总数
    private BigDecimal amount;    // 总价
    @JsonProperty("refund")
    private Integer hasRefund;    // 是否存在退费: 0=无, 1=存在
    @JsonProperty("total")
    private Integer instanceTotal;   // 订单对应实例总数量
    private Date createTime;
    private Date updateTime;

    // 封装订单下的所有实例明细
    private List<ProxyInstance> instances;
    // 封装代理购买订单的购买明细
    private List<ProxyPurchaseOrderItem> purchaseItems;
    // 封装代理续费订单的续费明细
    private List<ProxyRenewOrderItem> renewItems;
}