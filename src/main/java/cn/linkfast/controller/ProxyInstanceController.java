package cn.linkfast.controller;

import cn.linkfast.common.PageResult;
import cn.linkfast.common.Result;
import cn.linkfast.dto.ProxyInstanceQueryDTO;
import cn.linkfast.dto.ProxyInstanceRemarkDTO;
import cn.linkfast.service.ProxyInstanceService;
import cn.linkfast.vo.ProxyInstanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 代理实例接口控制器
 */
@Slf4j
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
    public Result<PageResult<ProxyInstanceVO>> queryProxyInstances(@Validated ProxyInstanceQueryDTO queryDto) {
        log.info("Controller层开始查询代理实例列表，查询条件：{}", queryDto);
        PageResult<ProxyInstanceVO> pageResult = proxyInstanceService.queryProxyInstances(queryDto);
        return Result.success(pageResult);
    }

    /**
     * 更新代理实例备注
     *
     * @param dto instanceNo（必传）+ remark
     * @return 操作结果
     */
    @PutMapping("/remark")
    public Result<Void> updateRemark(@RequestBody @Validated ProxyInstanceRemarkDTO dto) {
        proxyInstanceService.updateRemark(dto.getInstanceNo(), dto.getRemark());
        return Result.success(null);
    }

}

