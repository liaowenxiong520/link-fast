package cn.linkfast.service;

import cn.linkfast.vo.AreaVO;

import java.util.List;

/**
 * 地域服务接口
 */
public interface AreaService {

    /**
     * 调用第三方API查询地域树形列表
     *
     * @param codes 地域代码列表，为null时获取全部
     * @return 地域树形列表
     */
    List<AreaVO> queryAreaTree(List<String> codes);
}

