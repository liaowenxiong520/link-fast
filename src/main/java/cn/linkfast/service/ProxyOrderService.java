package cn.linkfast.service;

import cn.linkfast.common.PageResult;
import cn.linkfast.dto.ProxyOrderCreateDTO;
import cn.linkfast.dto.OrderUpdateResultDTO;
import cn.linkfast.dto.ProxyOrderQueryDTO;
import cn.linkfast.vo.ProxyOrderCreateVO;
import cn.linkfast.vo.ProxyOrderVO;

import java.util.Map;

public interface ProxyOrderService {
    /**
     * 同步订单完整信息并保存
     */
    OrderUpdateResultDTO syncOrderDetails(Map<String, Object> params) throws Exception;

    /**
     * 查询订单列表（分页）
     *
     * @param dto 入参DTO
     * @return 分页VO结果
     */
    PageResult<ProxyOrderVO> getProxyOrders(ProxyOrderQueryDTO dto);

    /**
     * 开通代理（创建订单）
     */
    ProxyOrderCreateVO createProxyOrder(ProxyOrderCreateDTO dto);

    /**
     * 根据渠道商订单号查询单个订单
     *
     * @param appOrderNo 渠道商订单号
     * @return 订单VO
     */
    ProxyOrderVO getProxyOrder(String appOrderNo);
}