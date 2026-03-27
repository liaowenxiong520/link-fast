package cn.linkfast.controller;

import cn.linkfast.common.PageResult;
import cn.linkfast.common.Result;
import cn.linkfast.dto.ProxyPurchaseDTO;
import cn.linkfast.dto.ProxyOrderQueryDTO;
import cn.linkfast.dto.ProxyRenewItemDTO;
import cn.linkfast.exception.BusinessException;
import cn.linkfast.service.ProxyOrderService;
import cn.linkfast.vo.ProxyPurchaseResultVO;
import cn.linkfast.vo.ProxyOrderVO;
import cn.linkfast.vo.ProxyRenewResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口控制器
 */
@Slf4j
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
    public Result<PageResult<ProxyOrderVO>> queryOrders(@Validated ProxyOrderQueryDTO dto) {
        return Result.success(proxyOrderService.queryOrders(dto));
    }

    /**
     * 开通代理（创建订单）
     */
    @PostMapping("/open")
    public Result<ProxyPurchaseResultVO> purchaseProxies(@RequestBody @Validated ProxyPurchaseDTO dto) {
        return Result.success(proxyOrderService.purchaseProxies(dto));
    }


    /**
     * 续费代理实例
     */
    @PostMapping("/renew")
    public Result<ProxyRenewResultVO> renewProxies(@RequestBody List<ProxyRenewItemDTO> items) {
        try {
            ProxyRenewResultVO vo = proxyOrderService.renewProxies(items);
            return Result.success(vo);
        } catch (BusinessException e) {
            log.error("续费代理失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("续费代理异常", e);
            return Result.error("续费代理失败，请稍后重试");
        }
    }

}
