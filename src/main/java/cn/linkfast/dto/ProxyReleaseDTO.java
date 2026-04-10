package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 释放代理实例请求入参DTO
 */
@Data
public class ProxyReleaseDTO {

    /**
     * 支付密码（必传）
     */
    @NotBlank(message = "支付密码不能为空")
    private String payPassword;

    /**
     * 平台实例编号列表（必传，至少一个）
     */
    @NotEmpty(message = "实例列表不能为空")
    private List<String> instanceNos;
}
