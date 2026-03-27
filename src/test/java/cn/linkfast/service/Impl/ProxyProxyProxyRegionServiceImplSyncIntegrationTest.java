package cn.linkfast.service.Impl;

import cn.linkfast.config.AppConfig;
import cn.linkfast.service.ProxyRegionService;
import cn.linkfast.service.impl.ProxyProxyRegionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProxyProxyRegionServiceImpl#syncRegionTreeToDb} 集成测试。
 * <p>
 * <b>特点：</b>
 * <ul>
 *   <li>真实 HTTP 请求第三方 {@code /api/open/app/area/v2}（依赖 {@code classpath:api.properties} 中的 env、URL、appKey、appSecret）</li>
 *   <li>真实写入本地数据库 {@code region} 表（依赖 {@code classpath:jdbc.properties}）</li>
 *   <li>测试类<b>未</b>使用 {@code @Transactional}，Spring 测试不会自动回滚；方法内事务正常提交，数据会保留</li>
 * </ul>
 * <p>
 * 运行前请确认网络可访问 sandbox/prod 地址，且数据库账号有 region 表写权限。
 * <p>
 * <b>说明：</b>第三方可能对 {@code codes} 过滤不严格，传入 {@code CN, US} 仍可能返回大量节点（如整洲树），
 * 批量写入已按 DAO 分块执行；若仍遇 MySQL 断连，请检查服务端 {@code wait_timeout} / {@code net_read_timeout} 等。
 */
@Tag("integration")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class ProxyProxyProxyRegionServiceImplSyncIntegrationTest {

    @Autowired
    private ProxyRegionService proxyRegionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(value = 30, unit = TimeUnit.MINUTES)
    @DisplayName("syncRegionTreeToDb：全量同步（codes=null）— 真实请求第三方并落库，不回滚")
    void syncRegionTreeToDb_fullTree_realApiAndPersist() throws Exception {
        // null 表示获取全部地域（与第三方文档一致）
        System.out.println("========== 测试全量同步地域树（codes=null） ==========");
        Long beforeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM region", Long.class);
        assertNotNull(beforeCount);

        // 返回的是总处理数（插入+更新）
        int batchModifiedCount = proxyRegionService.syncRegionTreeToDb(null);
        Long dbCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM region", Long.class);
        assertNotNull(dbCount);
        assertTrue(dbCount > 0,"同步完成后Region表的总记录数不应该等于0");

        System.out.println("[ProxyProxyProxyRegionServiceImplSyncIntegrationTest] 实际处理的地域记录数: " + batchModifiedCount);
        System.out.println("[ProxyProxyProxyRegionServiceImplSyncIntegrationTest] 同步前 region 表总行数: " + beforeCount);
        System.out.println("[ProxyProxyProxyRegionServiceImplSyncIntegrationTest] 当前 region 表总行数: " + dbCount);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MINUTES)
    @DisplayName("syncRegionTreeToDb：按 codes 同步 — 真实请求第三方并落库，不回滚")
    void syncRegionTreeToDb_byCodes_realApiAndPersist() throws Exception {
        // 仅同步部分国家/地区（第三方可能仍返回较大子树，见类注释）
        List<String> codes = List.of("CN", "US");
        System.out.println("========== 测试按 codes 同步地域树（codes=" + codes + "） ==========");
        int synced = proxyRegionService.syncRegionTreeToDb(codes);

        assertTrue(synced > 0, "指定 codes 时应写入至少 1 条，实际=" + synced);

        Long dbCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM region", Long.class);
        assertNotNull(dbCount);
        assertTrue(dbCount > 0);

        System.out.println("[ProxyProxyProxyRegionServiceImplSyncIntegrationTest] codes=" + codes + " 同步节点数: " + synced);
        System.out.println("[ProxyProxyProxyRegionServiceImplSyncIntegrationTest] 当前 region 表总行数: " + dbCount);
    }
}
