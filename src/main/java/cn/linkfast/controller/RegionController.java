package cn.linkfast.controller;

import cn.linkfast.common.Result;
import cn.linkfast.service.RegionService;
import cn.linkfast.vo.AreaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地域接口控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/area")
public class RegionController {

    private final RegionService regionService;

    /**
     * 获取地域树形列表
     *
     * @param codes 地域代码列表（可选，不传则获取全部）
     * @return 地域树形数据
     */
    @GetMapping("/tree")
    public Result<List<AreaDTO>> queryRegionTree(
            @RequestParam(value = "codes", required = false) List<String> codes) {
        List<AreaDTO> areaTree = regionService.queryRegionTree(codes);
        return Result.success(areaTree);
    }
}

