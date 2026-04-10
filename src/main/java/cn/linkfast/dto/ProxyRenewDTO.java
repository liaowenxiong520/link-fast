package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 续费代理实例请求入参DTO
 */
@Data
public class ProxyRenewDTO {

    /**
     * 支付密码（必传）
     */
    @NotBlank(message = "支付密码不能为空")
    private String payPassword;

    /**
     * 续费实例信息列表（必传，至少一个）
     */
    @NotEmpty(message = "续费实例列表不能为空")
    private List<ProxyRenewItemDTO> items;
}
