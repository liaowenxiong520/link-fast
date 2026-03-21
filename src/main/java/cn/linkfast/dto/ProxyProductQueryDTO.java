package cn.linkfast.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 代理产品查询参数传输对象 (Data Transfer Object)
 * 接收前端传来的所有筛选和分页参数
 */
@Data
public class ProxyProductQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 国家代码（可选）
     */
    private String countryCode;

    /**
     * 城市代码（可选）
     */
    private String cityCode;

    /**
     * 页码（必传，大于0）
     */
    @NotNull(message = "页码pageNum不能为空")
    @Min(value = 1, message = "页码pageNum必须大于0")
    private Integer pageNum;

    /**
     * 每页条数（必传，大于0且不超过100）
     */
    @NotNull(message = "每页条数pageSize不能为空")
    @Min(value = 1, message = "每页条数pageSize必须大于0")
    @Max(value = 100, message = "每页条数pageSize不能超过100")
    private Integer pageSize;

    /**
     * 代理类型列表（可选，为null或空集合时查询全部类型）
     */
    private List<Integer> proxyType;
}
