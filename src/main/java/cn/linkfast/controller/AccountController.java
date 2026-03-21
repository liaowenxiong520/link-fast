package cn.linkfast.controller;

import cn.linkfast.common.Result;
import cn.linkfast.service.AccountService;
import cn.linkfast.vo.AccountInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/info")
    public Result<AccountInfoVO> getAccountInfo() {
        AccountInfoVO vo = accountService.getAccountInfo();
        return Result.success(vo);
    }
}

