package cn.linkfast.service;

import cn.linkfast.vo.PayPasswordVO;

/**
 * 支付密码服务接口
 */
public interface PayService {

    /**
     * 校验支付密码是否正确
     *
     * @param payPassword 用户输入的支付密码
     * @return 校验结果
     */
    PayPasswordVO verifyPayPassword(String payPassword);
}

