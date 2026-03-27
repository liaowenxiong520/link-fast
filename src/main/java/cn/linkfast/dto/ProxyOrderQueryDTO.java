package cn.linkfast.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * TODO
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/12 14:18
 */
@Data
public class ProxyOrderQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 页码（必传）
     */
    @NotNull(message = "页码pageNum不能为空")
    @Min(value = 1, message = "页码pageNum必须大于0")
    private Integer pageNum;

    /**
     * 每页条数（必传）
     */
    @NotNull(message = "每页条数pageSize不能为空")
    @Min(value = 1, message = "每页条数pageSize必须大于0")
    @Max(value = 100, message = "每页条数pageSize不能超过100")
    private Integer pageSize;


    /**
     * 订单号（非必传）
     */
    private String orderNo;


    /**
     * 订单类型（非必传）
     */
    private Integer orderType;


}