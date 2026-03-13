package cn.linkfast.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProxyOrderCreateDTO {
    private String userId;
    private Integer orderType;
    private Integer totalQuantity;
    private List<ProxyOrderItemDTO> params;
}


