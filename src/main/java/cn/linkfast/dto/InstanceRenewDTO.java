package cn.linkfast.dto;

import lombok.Data;

/**
 * TODO
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/25 08:24
 */
@Data
public class InstanceRenewDTO {
    private String instanceNo;

    private Integer duration;
    // 周期单位
    private Integer unit;
    // 续费月数
    private Integer renewMonths;

}
