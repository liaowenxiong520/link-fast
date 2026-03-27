package cn.linkfast.service.impl;

import cn.linkfast.common.PageResult;
import cn.linkfast.dao.ProxyProductDAO;
import cn.linkfast.dto.ProxyProductQueryDTO;
import cn.linkfast.dto.ProxyProductSearchCondition;
import cn.linkfast.entity.ProxyProduct;
import cn.linkfast.service.ProxyProductService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.utils.HttpClientUtil;
import cn.linkfast.vo.ProxyProductVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProxyProductServiceImpl implements ProxyProductService {
    /*static {
        // 关键防御代码：在代码层强制限制 JSON 嵌套深度
        // 这样即便黑客利用漏洞发送恶意 JSON，Jackson 也会在 1000 层时直接报错，不会导致服务器崩溃
        StreamReadConstraints constraints = StreamReadConstraints.builder().maxNestingDepth(1000).build();
        objectMapper.getFactory().setStreamReadConstraints(constraints);
    }*/

    private final ObjectMapper objectMapper;
    private final ProxyProductDAO proxyProductDAO;
    private final ApiPacketUtil apiPacketUtil;
    // --- 注入配置 ---
    @Value("${api.ipv.env}")
    private String env;

    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;

    @Value("${api.ipv.prod_url}")
    private String prodUrl;

    @Value("${api.ipv.path.product_query}")
    private String productQueryPath;

    private String baseUrl; // 动态确定的基础地址

    private static @NonNull ProxyProductSearchCondition buildSearchCondition(@NonNull ProxyProductQueryDTO queryDto) {
        ProxyProductSearchCondition condition = new ProxyProductSearchCondition();
        if (queryDto.getCountryCode() != null && !queryDto.getCountryCode().isEmpty()) {
            condition.setCountryCode(queryDto.getCountryCode());
        }
        if (queryDto.getCityCode() != null && !queryDto.getCityCode().isEmpty()) {
            condition.setCityCode(queryDto.getCityCode());
        }
        if (queryDto.getProxyType() != null && !queryDto.getProxyType().isEmpty()) {
            condition.setProxyType(queryDto.getProxyType());
        }
        condition.setLimit(queryDto.getPageSize());
        int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
        condition.setOffset(Math.max(offset, 0));
        return condition;
    }

    /**
     * 初始化：根据环境开关选择 BaseUrl，并准备 AES 的 IV
     */
    @PostConstruct
    public void init() {
        // 1. 确定环境地址
        if ("prod".equalsIgnoreCase(env)) {
            this.baseUrl = prodUrl;
        } else {
            this.baseUrl = sandboxUrl;
        }
    }


    @Override
    public List<ProxyProduct> getProxyProducts(Map<String, Object> params) throws Exception {

        // 拼接完整的请求 URL
        String fullUrl = baseUrl + productQueryPath;

        // 业务参数转成最终的请求参数
        Map<String, Object> finalRequest = apiPacketUtil.pack(params);

        // 发送 HTTP 请求并获取响应字符串
        String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

        // 解析响应并解密，得到 ProxyProduct 列表
        return processResponse(responseStr);
    }

    @Override
    public int syncProxyProducts(Map<String, Object> params) throws Exception {

        // 拼接完整的请求 URL
        String fullUrl = baseUrl + productQueryPath;

        // 业务参数转成最终的请求参数
        Map<String, Object> finalRequest = apiPacketUtil.pack(params);

        // 发送 HTTP 请求并获取响应字符串
        String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

        // 解析响应并解密，得到 ProxyProduct 列表
        List<ProxyProduct> products = processResponse(responseStr);

        return proxyProductDAO.batchSaveOrUpdate(products);
    }

    @Override
    public PageResult<ProxyProductVO> queryProxyProducts(ProxyProductQueryDTO queryDto) {
        // 1. DTO 转 SearchCondition (计算 offset)
        ProxyProductSearchCondition condition = buildSearchCondition(queryDto);

        // 2. 查询总条数 (用于分页组件显示总页数)
        int total = proxyProductDAO.count(condition);
        if (total == 0) {
            return new PageResult<>(0, List.of(), queryDto.getPageNum(), queryDto.getPageSize());
        }

        // 3. 执行数据查询 (Entity 列表)
        List<ProxyProduct> entityList = proxyProductDAO.selectListByCondition(condition);

        // 4. 将 Entity 转换为 VO (数据脱敏/格式转换)
        List<ProxyProductVO> voList = entityList.stream().map(this::convertToVO).collect(Collectors.toList());

        log.info("返回给前端的产品列表：{}", voList);
        // 5. 封装返回
        return new PageResult<>(total, voList, queryDto.getPageNum(), queryDto.getPageSize());
    }

    /**
     * 将数据库实体转换为展示对象
     */
    private ProxyProductVO convertToVO(ProxyProduct entity) {
        ProxyProductVO vo = new ProxyProductVO();
        // 使用 Spring 的 BeanUtils 快速拷贝同名属性
        BeanUtils.copyProperties(entity, vo);
        // 这里可以做特殊逻辑，比如：
        // if (entity.getProxyType() == 1) vo.setProxyTypeName("静态代理");
        return vo;
    }

    // 解析响应数据
    private List<ProxyProduct> processResponse(String responseStr) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) return List.of();

            // 解密响应数据
            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("接口返回数据解密成功: {}", decryptedJson);
            // 将解密后的 JSON 转换为 ProxyProduct 列表

            return objectMapper.readValue(decryptedJson, new TypeReference<>() {
            });
        } else {
            throw new RuntimeException("API错误: " + root.path("msg").asText());
        }
    }


}