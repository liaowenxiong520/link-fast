package cn.linkfast.service.impl;

import cn.linkfast.entity.AppAccountInfo;
import cn.linkfast.service.AccountService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.utils.HttpClientUtil;
import cn.linkfast.vo.AccountInfoVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final ApiPacketUtil apiPacketUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${api.ipv.env}")
    private String env;
    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;
    @Value("${api.ipv.prod_url}")
    private String prodUrl;
    @Value("${api.ipv.path.app_info}")
    private String appInfoPath;
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
    public AccountInfoVO getAccountInfo() {
        AccountInfoVO accountInfoVO = new AccountInfoVO();
        try {
            Map<String, Object> finalRequest = apiPacketUtil.pack(null);
            String fullUrl = baseUrl + appInfoPath;
            String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

            AppAccountInfo appAccountInfo = processResponse(responseStr);
            if (appAccountInfo == null) {
                log.error("获取APP账户信息失败，接口返回数据为空~");
                return null;
            }
            accountInfoVO.setCoin(appAccountInfo.getCoin());
            accountInfoVO.setCredit(appAccountInfo.getCredit());
            return accountInfoVO;
        } catch (Exception e) {
            log.error("获取APP账户信息异常", e);
            return null;
        }
    }

    private AppAccountInfo processResponse(String responseStr) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) return null;

            // 解密响应数据
            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("接口返回数据解密成功: {}", decryptedJson);
            // 将解密后的 JSON 转换为实体对象
            return objectMapper.readValue(decryptedJson, AppAccountInfo.class);
        } else {
            throw new RuntimeException("API错误: " + root.path("msg").asText());
        }
    }
}

