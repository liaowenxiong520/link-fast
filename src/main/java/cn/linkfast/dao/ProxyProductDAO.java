package cn.linkfast.dao;

import cn.linkfast.entity.ProxyProduct;
import cn.linkfast.dto.ProxyProductSearchCondition;

import java.util.List;

public interface ProxyProductDAO {
    /**
     * 批量保存或更新产品信息
     */
    int batchSaveOrUpdate(List<ProxyProduct> products);

    List<ProxyProduct> selectListByCondition(ProxyProductSearchCondition condition);

    int count(ProxyProductSearchCondition condition);

    /**
     * 根据产品编号查询单个代理产品
     *
     * @param productNo 产品编号
     * @return 代理产品信息
     */
    ProxyProduct selectByProductNo(String productNo);
}