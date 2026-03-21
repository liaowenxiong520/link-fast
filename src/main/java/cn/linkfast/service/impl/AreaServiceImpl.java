package cn.linkfast.service.impl;

import cn.linkfast.exception.BusinessException;
import cn.linkfast.service.AreaService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.vo.AreaVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地域服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final ObjectMapper objectMapper;
    private final ApiPacketUtil apiPacketUtil;

    @Value("${api.ipv.env}")
    private String env;

    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;

    @Value("${api.ipv.prod_url}")
    private String prodUrl;

    @Value("${api.ipv.path.area_list}")
    private String areaListPath;

    private String baseUrl;

    @PostConstruct
    public void init() {
        if ("prod".equalsIgnoreCase(env)) {
            this.baseUrl = prodUrl;
        } else {
            this.baseUrl = sandboxUrl;
        }
    }

    @Override
    public List<AreaVO> queryAreaTree(List<String> codes) {
        try {
            // 1. 构造请求参数
            Map<String, Object> params = new HashMap<>();
            if (codes != null && !codes.isEmpty()) {
                params.put("codes", codes);
            }

            // 2. 加密封装
            Map<String, Object> finalRequest = apiPacketUtil.pack(params);

            // 3. 发送请求
            String fullUrl = baseUrl + areaListPath;
            String responseStr = sendPost(fullUrl, finalRequest);

            // 4. 解析响应并解密
            return processResponse(responseStr);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("获取地域列表失败，请稍后重试", e);
        }
    }

    /**
     * 解析第三方API响应，解密后转为 AreaVO 列表
     */
    private List<AreaVO> processResponse(String responseStr) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) {
                log.warn("地域接口返回 data 为空");
                return Collections.emptyList();
            }

            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("地域接口返回数据解密成功");

            List<AreaVO> areaList = objectMapper.readValue(
                    decryptedJson, new TypeReference<>() {
                    });
            return areaList != null ? areaList : Collections.emptyList();
        } else {
            throw new BusinessException("地域API错误: " + root.path("msg").asText());
        }
    }

    private String sendPost(String url, Map<String, Object> body) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            String json = objectMapper.writeValueAsString(body);
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            return client.execute(post, response ->
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        }
    }
}

