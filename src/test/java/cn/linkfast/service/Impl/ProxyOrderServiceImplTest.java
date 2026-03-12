package cn.linkfast.service.Impl;

import cn.linkfast.dao.ProxyOrderDAO;
import cn.linkfast.entity.ProxyOrder;
import cn.linkfast.service.impl.ProxyOrderServiceImpl;
import cn.linkfast.utils.ApiPacketUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProxyOrderServiceImplTest {

    @Mock
    private ProxyOrderDAO proxyOrderDAO;

    @Mock
    private ApiPacketUtil apiPacketUtil;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProxyOrderServiceImpl proxyOrderService;

    @BeforeEach
    void setUp() {
        // 注入配置值
        ReflectionTestUtils.setField(proxyOrderService, "env", "sandbox");
        ReflectionTestUtils.setField(proxyOrderService, "sandboxUrl", "https://sandbox.ipipv.com");
    }

    @Test
    @DisplayName("测试 processResponse 方法 - 验证 JSON 到实体的完整映射")
    void testProcessResponse_MappingSuccess() throws Exception {
        // 1. 准备 API 返回的原始加密包裹字符串
        String mockApiResponse = "{\"code\":200, \"msg\":\"success\", \"data\":\"ENCRYPTED_DATA_HERE\"}";

        // 2. 准备解密后的完整 JSON（包含文档所有必填与非必填字段）
        String fullDecryptedJson = "{\"orderNo\":\"P202603111200001\",\"appOrderNo\":\"APP_ORD_998877\",\"type\":1,\"status\":3,\"count\":10,\"amount\":\"100.50\",\"refund\":0,\"page\":1,\"pageSize\":10,\"total\":1,\"instances\":[{\"instanceNo\":\"INS_66778899\",\"proxyType\":101,\"protocol\":\"socks5\",\"ip\":\"154.22.33.44\",\"port\":8080,\"regionId\":\"US-West\",\"countryCode\":\"US\",\"cityCode\":\"LAX\",\"useType\":\"static\",\"username\":\"tester_proxy\",\"pwd\":\"password123\",\"userExpired\":1741670400,\"flowTotal\":\"1024.00\",\"flowBalance\":\"512.00\",\"status\":1,\"renew\":null,\"bridges\":[\"1.1.1.1\",\"2.2.2.2\"],\"openAt\":null,\"renewAt\":null,\"releaseAt\":null,\"productNo\":\"PN_STATIC_001\",\"extendIp\":\"\",\"projectId\":\"PROJ_001\"}]}";

        // 3. 打桩：模拟解密工具类的行为
        when(apiPacketUtil.unpack("ENCRYPTED_DATA_HERE")).thenReturn(fullDecryptedJson);

        // 4. 调用私有方法 processResponse
        ProxyOrder order = ReflectionTestUtils.invokeMethod(proxyOrderService, "processResponse", mockApiResponse);

        // 5. 断言验证字段解析是否正确
        assertNotNull(order);
        assertEquals("P202603111200001", order.getOrderNo());
        assertEquals(new BigDecimal("100.50"), order.getAmount());
        assertEquals(1, order.getInstances().size());

        // 验证实例中的字段
        var instance = order.getInstances().get(0);
        assertEquals("INS_66778899", instance.getInstanceNo());
        assertEquals("US", instance.getCountryCode());
        assertEquals(8080, instance.getPort());
        assertNotNull(instance.getBridges());
        assertEquals(2, instance.getBridges().size());
        assertEquals("1.1.1.1", instance.getBridges().get(0));
    }



    @Test
    @DisplayName("测试 API 返回 Code!=200 时的异常处理")
    void testProcessResponse_ApiError() {
        String errorJson = "{\"code\":400, \"msg\":\"Invalid OrderNo\", \"data\":\"\"}";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyOrderService, "processResponse", errorJson);
        });

        assertTrue(exception.getMessage().contains("Invalid OrderNo"));
    }
}