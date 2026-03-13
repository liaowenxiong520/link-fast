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
    OrderUpdateResultDTO updateProxyOrder(ProxyOrder order);

    /**
     * 分页查询订单列表
     *
     * @param condition 查询条件
     * @return 订单实体列表
     */
    List<ProxyOrder> findProxyOrderList(ProxyOrderSearchCondition condition);

    /**
     * 查询订单总数（分页用）
     *
     * @param condition 查询条件
     * @return 总条数
     */
    int countProxyOrder(ProxyOrderSearchCondition condition);

    /**
     * 回写第三方返回的 orderNo 和 amount
     */
    int updateProxyOrder(String appOrderNo, String orderNo, java.math.BigDecimal amount);

    /**
     * 保存订单主数据和项目数据到数据库
     * 用于开通代理等新建订单场景
     *
     * @param order 包含主表信息和 items 列表的订单对象
     * @return 保存的订单的 appOrderNo
     */
    String saveProxyOrder(ProxyOrder order);
}