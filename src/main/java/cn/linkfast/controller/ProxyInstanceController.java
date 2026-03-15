package cn.linkfast.controller;

import cn.linkfast.common.PageResult;
import cn.linkfast.common.Result;
import cn.linkfast.dto.ProxyInstanceQueryDTO;
import cn.linkfast.service.ProxyInstanceService;
import cn.linkfast.vo.ProxyInstanceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代理实例接口控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/instance")
public class ProxyInstanceController {

    private final ProxyInstanceService proxyInstanceService;

    /**
     * 获取代理实例列表（分页）
     *
     * @param queryDto 查询入参（自动校验必传参数）
     * @return 分页实例VO列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProxyInstanceVO>> getProxyInstanceList(@Validated ProxyInstanceQueryDTO queryDto) {
        PageResult<ProxyInstanceVO> pageResult = proxyInstanceService.getProxyInstances(queryDto);
        return Result.success(pageResult);
    }
}

