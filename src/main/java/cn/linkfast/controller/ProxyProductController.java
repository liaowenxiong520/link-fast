package cn.linkfast.controller;

import cn.linkfast.common.PageResult;
import cn.linkfast.common.Result;
import cn.linkfast.dto.ProxyProductQueryDTO;
import cn.linkfast.service.ProxyProductService;
import cn.linkfast.vo.ProxyProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代理产品控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/proxy-product")
public class ProxyProductController {

    private final ProxyProductService productService;

    /**
     * 分页查询代理产品列表
     * Spring 会自动根据参数名将 URL 中的查询参数映射到 ProxyProductQueryDTO 的字段中
     * 例如：/api/proxy/list?countryCode=US&page=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<PageResult<ProxyProductVO>> getProxyProductList(@Validated ProxyProductQueryDTO queryDto) {
        PageResult<ProxyProductVO> pageResult = productService.getProxyProducts(queryDto);
        return Result.success(pageResult);
    }
}