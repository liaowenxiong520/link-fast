package cn.linkfast.utils;

import cn.hutool.core.lang.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/26 21:57
 */
@RequiredArgsConstructor
@Component
public class AppOrderNoGenerator {
    // 业务前缀
    private static final String PREFIX_BUY = "P";     // 购买订单
    private static final String PREFIX_RENEW = "R";   // 续费订单
    private static final String PREFIX_RELEASE = "F"; // 释放订单

    private final Snowflake snowflake;

    /**
     * 生成购买订单号
     */
    public String generateBuyOrderId() {
        return PREFIX_BUY + snowflake.nextIdStr();
    }

    /**
     * 生成续费订单号
     */
    public String generateRenewOrderId() {
        return PREFIX_RENEW + snowflake.nextIdStr();
    }

    /**
     * 生成释放订单号
     */
    public String generateReleaseOrderId() {
        return PREFIX_RELEASE + snowflake.nextIdStr();
    }
}
