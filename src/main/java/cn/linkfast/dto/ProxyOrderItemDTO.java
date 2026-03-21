package cn.linkfast.dto;

import lombok.Data;

@Data
public class ProxyOrderItemDTO {
    private String productNo;
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


