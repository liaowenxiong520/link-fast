package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 支付密码校验请求DTO
 */
@Data
public class PayPasswordDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付密码
     */
    @NotBlank(message = "支付密码不能为空")
    private String payPassword;
}

