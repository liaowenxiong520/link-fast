package cn.linkfast.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新代理实例备注入参DTO
 */
@Data
public class ProxyInstanceRemarkDTO {

    /**
     * 平台实例编号（必传）
     */
    @NotBlank(message = "实例编号instanceNo不能为空")
    private String instanceNo;

    /**
     * 备注内容（可为空，传空字符串表示清空备注）
     */
    private String remark;
}
