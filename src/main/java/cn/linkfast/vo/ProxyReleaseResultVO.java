package cn.linkfast.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 代理释放订单创建成功后返回给前端展示的信息
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/29
 */
@Data
public class ProxyReleaseResultVO {
    private String appOrderNo;
    private String orderNo;
    private Integer status;
    private BigDecimal amount;
}
