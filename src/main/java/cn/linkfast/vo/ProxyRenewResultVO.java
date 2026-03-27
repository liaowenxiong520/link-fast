package cn.linkfast.vo;

import lombok.Data;

/**
 * 代理续费订单创建成功后返回给前端展示的信息
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/25 07:38
 */
@Data
public class ProxyRenewResultVO {
    private String appOrderNo;
    private String orderNo;
    private Integer status;
    private java.math.BigDecimal amount;

}
