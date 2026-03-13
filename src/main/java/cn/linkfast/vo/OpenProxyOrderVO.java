package cn.linkfast.vo;

import lombok.Data;

@Data
public class OpenProxyOrderVO {
    private String appOrderNo;
    private Integer status;
    private String orderNo;
    private java.math.BigDecimal amount;
}
