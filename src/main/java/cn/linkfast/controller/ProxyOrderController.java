package cn.linkfast.controller;


import cn.linkfast.common.PageResult;
import cn.linkfast.dto.ProxyOrderCreateDTO;
import cn.linkfast.dto.ProxyOrderQueryDTO;
import cn.linkfast.service.ProxyOrderService;
import cn.linkfast.vo.OpenProxyOrderVO;
import cn.linkfast.vo.ProxyOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
public class ProxyOrderController {
    private final ProxyOrderService proxyOrderService;

    /**
     * 获取订单列表（分页）
     *
     * @param dto 前端入参（自动校验必传参数）
     * @return 分页订单VO列表
     */
    @GetMapping("/list")
    public PageResult<ProxyOrderVO> getProxyOrders(@Validated ProxyOrderQueryDTO dto) {
        return proxyOrderService.getProxyOrders(dto);
    }

    /**
     * 开通代理（创建订单）
     */
    @PostMapping("/open")
    public OpenProxyOrderVO createProxyOrder(@RequestBody @Validated ProxyOrderCreateDTO dto) {
        return proxyOrderService.createProxyOrder(dto);
    }
}
