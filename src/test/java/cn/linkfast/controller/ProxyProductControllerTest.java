package cn.linkfast.controller;

import cn.linkfast.config.AppConfig;
import cn.linkfast.config.WebMvcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, WebMvcConfig.class})
@WebAppConfiguration
public class ProxyProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 测试不携带任何参数请求 /api/proxy-product/list
     * 预期：返回 400，参数校验失败
     */
    @Test
    public void testGetProxyProductListWithoutParams() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/proxy-product/list"))
                .andDo(print())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("========== 不带参数请求结果 ==========");
        System.out.println(responseBody);
    }
}

