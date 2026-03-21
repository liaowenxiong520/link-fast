package cn.linkfast.service.impl;

import cn.linkfast.service.PayService;
import cn.linkfast.vo.PayPasswordVO;
import org.springframework.stereotype.Service;

/**
 * 支付密码服务实现类
 */
@Service
public class PayServiceImpl implements PayService {

    /**
     * 正确的支付密码（暂时写死，后续从数据库或配置中获取）
     */
    private static final String CORRECT_PAY_PASSWORD = "168888";

    @Override
    public PayPasswordVO verifyPayPassword(String payPassword) {
        PayPasswordVO vo = new PayPasswordVO();
        if (CORRECT_PAY_PASSWORD.equals(payPassword)) {
            vo.setPassed(true);
            vo.setMessage("支付密码正确");
        } else {
            vo.setPassed(false);
            vo.setMessage("支付密码错误");
        }
        return vo;
    }
}

