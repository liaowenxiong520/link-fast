package cn.linkfast.service;

import cn.linkfast.dto.OrderUpdateResultDTO;

import java.util.Map;

public interface ProxyOrderService {
    /**
     * 同步订单完整信息并保存
     */
    OrderUpdateResultDTO syncOrderDetails(Map<String, Object> params) throws Exception;

}