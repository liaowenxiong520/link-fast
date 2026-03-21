package cn.linkfast.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProxyOrderCreateDTO {
    private String payPassword;
    private Integer orderType;
    private Integer totalQuantity;
    private List<ProxyOrderItemDTO> params;
}


