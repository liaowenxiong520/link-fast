package cn.linkfast.service.impl;

import cn.linkfast.common.PageResult;
import cn.linkfast.dao.ProxyInstanceDAO;
import cn.linkfast.dao.ProxyRegionDAO;
import cn.linkfast.dto.ProxyInstanceQueryDTO;
import cn.linkfast.dto.ProxyInstanceSearchCondition;
import cn.linkfast.entity.ProxyInstance;
import cn.linkfast.entity.ProxyRegion;
import cn.linkfast.service.ProxyInstanceService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.utils.HttpClientUtil;
import cn.linkfast.vo.ProxyInstanceVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代理实例服务实现类
 *
 * @author liaowenxiong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyInstanceServiceImpl implements ProxyInstanceService {

    private final ObjectMapper objectMapper;
    private final ProxyInstanceDAO proxyInstanceDAO;
    private final ProxyRegionDAO proxyRegionDAO;
    private final ApiPacketUtil apiPacketUtil;

    @Value("${api.ipv.env}")
    private String env;

    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;

    @Value("${api.ipv.prod_url}")
    private String prodUrl;

    @Value("${api.ipv.path.instance_query}")
    private String instanceQueryPath;

    private String baseUrl;

    /**
     * 初始化：根据环境开关选择 BaseUrl
     */
    @PostConstruct
    public void init() {
        if ("prod".equalsIgnoreCase(env)) {
            this.baseUrl = prodUrl;
        } else {
            this.baseUrl = sandboxUrl;
        }
    }

    @Override
    public int syncProxyInstance(String instanceNo) throws Exception {
        // 1. 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("instances", Collections.singletonList(instanceNo));

        // 2. 拼接完整的请求 URL
        String fullUrl = baseUrl + instanceQueryPath;

        // 3. 业务参数加密封装
        Map<String, Object> finalRequest = apiPacketUtil.pack(params);

        // 4. 发送 HTTP 请求
        String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

        // 5. 解析响应并持久化
        return processResponse(responseStr);
    }

    @Override
    public PageResult<ProxyInstanceVO> queryProxyInstances(ProxyInstanceQueryDTO queryDto) {
        // 1. DTO 转 SearchCondition（计算 offset）
        ProxyInstanceSearchCondition condition = buildSearchCondition(queryDto);

        // 2. 查询总条数
        int total = proxyInstanceDAO.countByCondition(condition);
        if (total == 0) {
            return new PageResult<>(0, List.of(), queryDto.getPageNum(), queryDto.getPageSize());
        }

        // 3. 执行数据查询
        List<ProxyInstance> entityList = proxyInstanceDAO.selectListByCondition(condition);

        // 4. Entity 转 VO
        List<ProxyInstanceVO> voList = entityList.stream().map(this::convertToVO).collect(Collectors.toList());

        // 5. 封装返回
        return new PageResult<>(total, voList, queryDto.getPageNum(), queryDto.getPageSize());
    }

    private static ProxyInstanceSearchCondition buildSearchCondition(ProxyInstanceQueryDTO queryDto) {
        ProxyInstanceSearchCondition condition = new ProxyInstanceSearchCondition();
        condition.setProxyType(queryDto.getProxyType());
        condition.setStatus(queryDto.getStatus());
        condition.setCountryCode(queryDto.getCountryCode());
        condition.setCityCode(queryDto.getCityCode());
        condition.setIp(queryDto.getIp());

        if (queryDto.getPageNum() != null && queryDto.getPageSize() != null) {
            condition.setLimit(queryDto.getPageSize());
            int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
            condition.setOffset(Math.max(offset, 0));
        }
        return condition;
    }

    private ProxyInstanceVO convertToVO(ProxyInstance entity) {
        ProxyInstanceVO vo = new ProxyInstanceVO();
        BeanUtils.copyProperties(entity, vo);

        // 拼接地域中文名：国家-城市（有值则拼，无则跳过）
        StringBuilder regionName = new StringBuilder();
        if (entity.getCountryCode() != null && !entity.getCountryCode().isEmpty()) {
            ProxyRegion country = proxyRegionDAO.selectByRegionCode(entity.getCountryCode());
            regionName.append(country != null ? country.getRegionName() : entity.getCountryCode());
        }
        if (entity.getCityCode() != null && !entity.getCityCode().isEmpty()) {
            ProxyRegion city = proxyRegionDAO.selectByRegionCode(entity.getCityCode());
            String cityName = city != null ? city.getRegionName() : entity.getCityCode();
            if (!regionName.isEmpty()) {
                regionName.append("-");
            }
            regionName.append(cityName);
        }
        if (!regionName.isEmpty()) {
            vo.setRegionName(regionName.toString());
        }
        return vo;
    }

    /**
     * 解析第三方API响应数据，解密后保存到数据库
     */
    private int processResponse(String responseStr) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) {
                log.warn("实例接口返回 data 为空");
                return 0;
            }

            // 解密响应数据
            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("实例接口返回数据解密成功: {}", decryptedJson);

            // 将解密后的 JSON 转换为 ProxyInstance 列表
            List<ProxyInstance> instanceList = objectMapper.readValue(
                    decryptedJson, new TypeReference<List<ProxyInstance>>() {
                    });

            if (instanceList == null || instanceList.isEmpty()) {
                log.warn("实例接口返回实例列表为空");
                return 0;
            }

            // 保存或更新到数据库
            return proxyInstanceDAO.batchUpdate(instanceList);
        } else {
            throw new RuntimeException("实例API错误: " + root.path("msg").asText());
        }
    }

}

