package cn.linkfast.service;

import cn.linkfast.common.PageResult;
import cn.linkfast.dto.ProxyInstanceQueryDTO;
import cn.linkfast.vo.ProxyInstanceVO;

/**
 * 代理实例服务接口
 */
public interface ProxyInstanceService {

    /**
     * 从第三方同步代理实例信息到我方数据库
     *
     * @param instanceNo 供应商实例编号
     * @return 成功处理的实例条数
     */
    int syncProxyInstance(String instanceNo) throws Exception;

    /**
     * 分页查询本地代理实例列表
     *
     * @param queryDto 查询入参
     * @return 分页VO结果
     */
    PageResult<ProxyInstanceVO> getProxyInstances(ProxyInstanceQueryDTO queryDto);
}

