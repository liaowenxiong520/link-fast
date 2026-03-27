package cn.linkfast.dto;

import lombok.Data;

/**
 * TODO
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/26 22:42
 */
@Data
public class ProxyRenewItemDTO {
    private String instanceNo;
    private Integer duration;
    private Integer unit;
    private Integer cycleTimes;
}
