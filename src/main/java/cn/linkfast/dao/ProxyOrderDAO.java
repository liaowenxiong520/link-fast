package cn.linkfast.dao;

import cn.linkfast.dto.OrderUpdateResultDTO;
import cn.linkfast.entity.ProxyOrder;

public interface ProxyOrderDAO {
    /**
     * 保存或更新订单及其关联的实例数据
     * @param order 包含实例列表的订单对象
     */
    OrderUpdateResultDTO saveOrder(ProxyOrder order);
}