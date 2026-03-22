package cn.linkfast.dao;

import cn.linkfast.dto.OrderUpdateResultDTO;
import cn.linkfast.dto.ProxyOrderSearchCondition;
import cn.linkfast.entity.ProxyOrder;

import java.util.List;

public interface ProxyOrderDAO {
    /**
     * 更新订单及其关联的实例数据
     *
     * @param order 包含实例列表的订单对象
     */
    OrderUpdateResultDTO updateByAppOrderNo(ProxyOrder order);

    /**
     * 分页查询订单列表
     *
     * @param condition 查询条件
     * @return 订单实体列表
     */
    List<ProxyOrder> selectListByCondition(ProxyOrderSearchCondition condition);

    /**
     * 查询订单总数（分页用）
     *
     * @param condition 查询条件
     * @return 总条数
     */
    int countByCondition(ProxyOrderSearchCondition condition);

    /**
     * 回写第三方返回的 orderNo 和 amount（同时更新 proxy_order 和 proxy_order_item）
     */
    OrderUpdateResultDTO updateByAppOrderNo(String appOrderNo, String orderNo, java.math.BigDecimal amount);

    /**
     * 保存订单主数据和项目数据到数据库
     * 用于开通代理等新建订单场景
     *
     * @param order 包含主表信息和 items 列表的订单对象
     * @return 保存的订单的 appOrderNo
     */
    String insert(ProxyOrder order);

    /**
     * 根据渠道商订单号查询单个订单
     *
     * @param appOrderNo 渠道商订单号
     * @return 订单实体，不存在则返回 null
     */
    ProxyOrder selectByAppOrderNo(String appOrderNo);
}