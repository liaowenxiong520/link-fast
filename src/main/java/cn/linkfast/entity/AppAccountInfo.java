package cn.linkfast.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * App账户信息实体类
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未定义的字段，提高兼容性
public class AppAccountInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * app名称
     */
    private String appName;

    /**
     * 账户余额
     */
    private String coin;

    /**
     * 授信额度
     */
    private String credit;

    /**
     * 使用桥：1-不使用 2-使用
     */
    private Integer useBridge;

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 状态：1-正常 -1-禁用
     */
    private Integer status;
}
