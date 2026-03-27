package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProxyPurchaseItemDTO {
    @NotBlank(message = "产品编号不能为空")
    private String productNo;
    @NotNull(message = "代理类型不能为空")
    private Integer proxyType;
    private String countryCode;
    private String stateCode;
    private String cityCode;
    private Integer unit;
    private Integer duration;
    private Integer count;
    private Integer cycleTimes;
    /* 购买项目code */
//    private String projectId;
}


