package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ProxyPurchaseDTO {
    @NotBlank(message = "支付密码不能为空")
    private String payPassword;

    @NotNull(message = "总数量不能为空")
    private Integer totalQuantity;

    @NotEmpty(message = "订单项列表不能为空")
    private List<ProxyPurchaseItemDTO> params;
}


