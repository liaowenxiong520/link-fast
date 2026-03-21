package cn.linkfast.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 支付密码校验结果VO
 */
@Data
public class PayPasswordVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 校验是否通过
     */
    private Boolean passed;

    /**
     * 提示信息
     */
    private String message;
}

