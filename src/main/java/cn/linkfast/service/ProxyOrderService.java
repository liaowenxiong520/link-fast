package cn.linkfast.service;

import cn.linkfast.common.PageResult;
import cn.linkfast.dto.ProxyOrderQueryDTO;
import cn.linkfast.dto.ProxyOrderUpdateResultDTO;
import cn.linkfast.dto.ProxyPurchaseDTO;
import cn.linkfast.dto.ProxyRenewItemDTO;
import cn.linkfast.exception.NoRollbackBusinessException;
import cn.linkfast.vo.ProxyOrderVO;
import cn.linkfast.vo.ProxyPurchaseResultVO;
import cn.linkfast.vo.ProxyRenewResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ProxyOrderService {
    /**
     * 同步订单完整信息并保存
     */
    ProxyOrderUpdateResultDTO syncOrderDetails(Map<String, Object> params) throws Exception;

    /**
     * 查询订单列表（分页）
     *
     * @param dto 入参DTO
     * @return 分页VO结果
     */
    PageResult<ProxyOrderVO> queryOrders(ProxyOrderQueryDTO dto);

    /**
     * 开通代理（购买代理）
     */
    ProxyPurchaseResultVO purchaseProxies(ProxyPurchaseDTO dto);

    /**
     * 根据渠道商订单号查询单个订单
     *
     * @param appOrderNo 渠道商订单号
     * @return 订单VO
     */
    ProxyOrderVO getOrderByAppOrderNo(String appOrderNo);

    @Transactional(rollbackFor = Exception.class, noRollbackFor = NoRollbackBusinessException.class)
    ProxyRenewResultVO renewProxies(List<ProxyRenewItemDTO> items);

    /**
     * 续费代理实例
     *
     * @param dto 续费入参DTO
     * @return 续费结果VO
     */
//    ProxyRenewResultVO renewProxies(ProxyRenewDTO dto);
}