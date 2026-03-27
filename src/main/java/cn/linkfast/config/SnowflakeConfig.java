package cn.linkfast.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/26 21:48
 */
@Configuration
public class SnowflakeConfig {


    /**
     * 机器 ID（0~31），自动根据 IP 最后一段计算，确保不同服务器/实例唯一
     */
    private static final long WORKER_ID = getWorkerId();

    /**
     * 数据中心 ID（0~31），可根据机房或环境固定，此处设为 0
     */
    private static final long DATACENTER_ID = 0L;

    /**
     * 根据本机 IP 的最后一段计算 workerId
     * 例如 IP 为 192.168.1.100，最后一段 100 % 32 = 4
     */
    private static long getWorkerId() {
        String ip = NetUtil.getLocalhostStr();
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            int lastPart = Integer.parseInt(parts[3]);
            return lastPart % 32;
        }
        return 0L; // 默认值
    }

    @Bean
    public Snowflake snowflake() {
        return IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID);
    }
}
