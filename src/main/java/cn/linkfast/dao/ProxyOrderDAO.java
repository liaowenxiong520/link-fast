package cn.linkfast.dao;

import cn.linkfast.dto.ProxyOrderSearchCondition;
import cn.linkfast.dto.ProxyOrderUpdateResultDTO;
import cn.linkfast.entity.ProxyOrder;

import java.math.BigDecimal;
import java.util.List;

public interface ProxyOrderDAO {
    /**
     * 更新订单及其关联的实例数据
     *
     * @param order 包含实例列表的订单对象
     */
    ProxyOrderUpdateResultDTO updateProxyPurchaseOrderByAppOrderNo(ProxyOrder order);

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
    ProxyOrderUpdateResultDTO updateProxyPurchaseOrderByAppOrderNo(String appOrderNo, String orderNo, java.math.BigDecimal amount);

    ProxyOrderUpdateResultDTO updateProxyRenewOrderByAppOrderNo(String appOrderNo, String orderNo, BigDecimal amount);

    /**
     * 保存订单主数据和项目数据到数据库
     * 用于开通代理等新建订单场景
     *
     * @param order 包含主表信息和 purchaseItems 列表的订单对象
     * @return 保存的订单的 appOrderNo
     */
    String insertOrderWithItems(ProxyOrder order);

    /**
     * 根据渠道商订单号查询单个订单
     *
     * @param appOrderNo 渠道商订单号
     * @return 订单实体，不存在则返回 null
     */
    ProxyOrder selectByAppOrderNo(String appOrderNo);

    /**
     * 仅将主订单数据插入 proxy_order 表
     *
     * @param order 主订单对象
     * @return 数据库自增生成的主键 id
     */
    Long insertOrder(ProxyOrder order);

    /**
     * 将 ProxyOrder 中的 purchaseItems 批量插入 proxy_purchase_order_item 表
     *
     * @param order 包含 purchaseItems 的订单对象
     * @return 实际插入的行数
     */
    int insertProxyPurchaseOrderItems(ProxyOrder order);

    /**
     * 将 ProxyOrder 中的 renewItems 批量插入 proxy_renew_order_item 表
     *
     * @param order 包含 renewItems 的订单对象
     * @return 实际插入的行数
     */
    int insertProxyRenewOrderItems(ProxyOrder order);

    /**
     * 将 ProxyOrder 中的 releaseOrderItems 批量插入 proxy_release_order_item 表
     *
     * @param order 包含 releaseOrderItems 的订单对象
     * @return 实际插入的行数
     */
    int insertProxyReleaseOrderItems(ProxyOrder order);

    /**
     * 代理释放业务，回写第三方返回的 orderNo 和 amount（同时更新 proxy_order 和 proxy_release_order_item）
     */
    ProxyOrderUpdateResultDTO updateProxyReleaseOrderByAppOrderNo(String appOrderNo, String orderNo, java.math.BigDecimal amount);

    /**
     * 根据渠道商订单号查询代理购买订单明细列表
     *
     * @param appOrderNo 渠道商订单号
     * @return 购买订单明细列表
     */
    List<cn.linkfast.entity.ProxyPurchaseOrderItem> selectPurchaseItemsByAppOrderNo(String appOrderNo);
}