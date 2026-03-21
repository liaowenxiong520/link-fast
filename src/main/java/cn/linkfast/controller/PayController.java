package cn.linkfast.controller;

import cn.linkfast.common.Result;
import cn.linkfast.dto.PayPasswordDTO;
import cn.linkfast.service.PayService;
import cn.linkfast.vo.PayPasswordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付密码接口控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pay")
public class PayController {

    private final PayService payPasswordService;

    /**
     * 校验支付密码
     *
     * @param dto 包含支付密码的请求体
     * @return 校验结果
     */
    @PostMapping("/verify")
    public Result<PayPasswordVO> verifyPayPassword(@RequestBody @Validated PayPasswordDTO dto) {
        PayPasswordVO vo = payPasswordService.verifyPayPassword(dto.getPayPassword());
        return Result.success(vo);
    }
}

