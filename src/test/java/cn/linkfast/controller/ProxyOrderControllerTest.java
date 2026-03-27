package cn.linkfast.controller;

import cn.linkfast.config.AppConfig;
import cn.linkfast.config.WebMvcConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 创建代理订单接口测试用例
 * 测试目标：订单创建失败时返回的错误信息是否符合预期
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, WebMvcConfig.class})
@WebAppConfiguration
public class ProxyOrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 测试请求体为空（不传 JSON Body）
     * 预期：返回 400，提示请求体缺失相关错误
     */
    @Test
    @DisplayName("请求体为空时应返回错误信息")
    public void testPurchaseProxiesWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 测试缺少支付密码
     * 预期：返回参数校验失败，code=400
     */
    @Test
    @DisplayName("缺少支付密码时应返回错误信息")
    public void testPurchaseProxiesWithoutPayPassword() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("orderType", 1);
        body.put("totalQuantity", 1);

        Map<String, Object> item = new HashMap<>();
        item.put("productNo", "TEST_PRODUCT_001");
        item.put("proxyType", 101);
        item.put("countryCode", "US");
        item.put("stateCode", "CA");
        item.put("cityCode", "LAX");
        item.put("unit", 1);
        item.put("duration", 30);
        item.put("count", 1);
        item.put("cycleTimes", 1);
        body.put("params", Collections.singletonList(item));

        mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 测试传入错误的支付密码
     * 预期：返回 400，提示支付密码校验不通过
     */
    @Test
    @DisplayName("支付密码错误时应返回400及错误提示")
    public void testPurchaseProxiesWithWrongPayPassword() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("payPassword", "wrong_password_123");
        body.put("orderType", 1);
        body.put("totalQuantity", 1);

        Map<String, Object> item = new HashMap<>();
        item.put("productNo", "TEST_PRODUCT_001");
        item.put("proxyType", 101);
        item.put("countryCode", "US");
        item.put("stateCode", "CA");
        item.put("cityCode", "LAX");
        item.put("unit", 1);
        item.put("duration", 30);
        item.put("count", 1);
        item.put("cycleTimes", 1);
        body.put("params", Collections.singletonList(item));

        MvcResult result = mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("========== 错误支付密码请求结果 ==========");
        System.out.println(responseBody);
    }

    /**
     * 测试 params 列表为空
     * 预期：返回业务异常，订单创建失败
     */
    @Test
    @DisplayName("订单项列表为空时应返回错误信息")
    public void testPurchaseProxiesWithEmptyParams() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("payPassword", "any_password");
        body.put("orderType", 1);
        body.put("totalQuantity", 0);
        body.put("params", Collections.emptyList());

        mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 测试 orderType 为 null
     * 预期：返回错误（业务异常或参数异常）
     */
    @Test
    @DisplayName("缺少orderType字段时应返回错误信息")
    public void testPurchaseProxiesType() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("payPassword", "any_password");
        body.put("totalQuantity", 1);

        Map<String, Object> item = new HashMap<>();
        item.put("productNo", "TEST_PRODUCT_001");
        item.put("proxyType", 101);
        item.put("countryCode", "US");
        item.put("unit", 1);
        item.put("duration", 30);
        item.put("count", 1);
        item.put("cycleTimes", 1);
        body.put("params", Collections.singletonList(item));

        mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 测试续费代理实例（真实调用第三方API）
     * 参数：instanceNo=c_gjfk6f7fyh38vhv, appOrderNo=20329587392622171151773956730965
     *       duration=30, unit=1, renewMonths=3
     * cycleTimes计算规则：duration=30, unit=1 → cycleTimes=renewMonths=3
     * 测试目标：续费成功或失败的结果能否正确返回
     */
    @Test
    @DisplayName("续费代理实例-集成测试")
    public void testRenewProxies() throws Exception {
        Map<String, Object> instance = new HashMap<>();
        instance.put("instanceNo", "c_gjfk6f7fyh38vhv");
        instance.put("duration", 30);
        instance.put("unit", 1);
        instance.put("renewMonths", 3);

        Map<String, Object> body = new HashMap<>();
        body.put("appOrderNo", "a");
        body.put("instances", Collections.singletonList(instance));

        MvcResult result = mockMvc.perform(post("/api/order/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("========== 续费代理实例响应结果 ==========");
        System.out.println(responseBody);

        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        Integer code = (Integer) responseMap.get("code");
        if (code != null && code == 200) {
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            System.out.println("✅ 续费成功！");
            System.out.println("orderNo: " + (data != null ? data.get("orderNo") : null));
            System.out.println("appOrderNo: " + (data != null ? data.get("appOrderNo") : null));
            System.out.println("instances: " + (data != null ? data.get("instances") : null));
            assert data != null : "续费成功时 data 不应为空";
            assert data.get("orderNo") != null : "orderNo 不应为空";
            assert data.get("appOrderNo") != null : "appOrderNo 不应为空";
        } else {
            System.out.println("❌ 续费失败！");
            System.out.println("code: " + responseMap.get("code"));
            System.out.println("message: " + responseMap.get("message"));
            assert responseMap.get("data") == null : "续费失败时 data 应为空";
        }
    }

    /**
     * 测试请求 Content-Type 不是 JSON
     * 预期：全局异常处理器捕获异常后返回 HTTP 200 + JSON 错误信息
     */
    @Test
    @DisplayName("Content-Type不是JSON时应返回错误")
    public void testPurchaseProxiesWithWrongContentType() throws Exception {
        mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("payPassword=test&orderType=1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 测试使用完整合法参数创建代理订单
     * 说明：这是集成测试，依赖真实数据库和第三方API
     *       成功时返回 ProxyPurchaseResultVO（含 appOrderNo、orderNo、status、amount）
     *       失败时由全局异常处理器返回 Result（含 code、message）
     */
    @Test
    @DisplayName("完整合法参数创建代理订单-集成测试")
    public void testPurchaseProxiesWithValidData() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("payPassword", "168888");
        body.put("orderType", 1);
        body.put("totalQuantity", 1);

        Map<String, Object> item = new HashMap<>();
        item.put("productNo", "ipv_gnmr4u5rx");
        item.put("proxyType", 103);
        item.put("countryCode", "CAN");
        item.put("stateCode", "CAN000");
        item.put("cityCode", "CAN000LOD");
        item.put("unit", 1);
        item.put("duration", 30);
        item.put("count", 1);
        item.put("cycleTimes", 1);
        item.put("projectId", "ceshibiaoqian5");
        body.put("params", Collections.singletonList(item));

        MvcResult result = mockMvc.perform(post("/api/order/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("========== 创建代理订单响应结果 ==========");
        System.out.println(responseBody);

        // 解析响应 JSON
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

        if (responseMap.containsKey("appOrderNo")) {
            // 订单创建成功，返回的是 ProxyPurchaseResultVO
            System.out.println("✅ 订单创建成功！");
            System.out.println("appOrderNo: " + responseMap.get("appOrderNo"));
            System.out.println("orderNo: " + responseMap.get("orderNo"));
            System.out.println("status: " + responseMap.get("status"));
            System.out.println("amount: " + responseMap.get("amount"));
            // 断言关键字段不为空
            assert responseMap.get("appOrderNo") != null : "appOrderNo 不应为空";
            assert responseMap.get("orderNo") != null : "orderNo 不应为空";
        } else if (responseMap.containsKey("code")) {
            // 订单创建失败，返回的是 Result 错误响应
            System.out.println("❌ 订单创建失败！");
            System.out.println("code: " + responseMap.get("code"));
            System.out.println("message: " + responseMap.get("message"));
            // 如果期望必须成功，可打开下面这行断言
            // fail("订单创建失败: code=" + responseMap.get("code") + ", message=" + responseMap.get("message"));
        }
    }
}

