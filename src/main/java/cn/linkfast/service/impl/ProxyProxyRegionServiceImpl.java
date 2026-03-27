package cn.linkfast.service.impl;

import cn.linkfast.dao.ProxyRegionDAO;
import cn.linkfast.entity.ProxyRegion;
import cn.linkfast.exception.BusinessException;
import cn.linkfast.service.ProxyRegionService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.utils.HttpClientUtil;
import cn.linkfast.dto.AreaDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地域同步实现类：解析树形结构并落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyProxyRegionServiceImpl implements ProxyRegionService {

    private final ObjectMapper objectMapper;
    private final ApiPacketUtil apiPacketUtil;
    private final ProxyRegionDAO proxyRegionDAO;

    @Value("${api.ipv.env}")
    private String env;

    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;

    @Value("${api.ipv.prod_url}")
    private String prodUrl;

    @Value("${api.ipv.path.area_list}")
    private String areaListPath;

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
    public List<AreaDTO> queryRegionTree(List<String> codes) {
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
            String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

            // 4. 解析响应并解密
            return processResponse(responseStr);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("获取地域列表失败，请稍后重试", e);
        }
    }

    @Override
    public int syncRegionTreeToDb(List<String> codes) throws Exception {
        // 1. 请求第三方「获取地域信息接口」— 放在事务外，避免 HTTP 耗时占用数据库连接
        Map<String, Object> params = new HashMap<>();
        if (codes != null && !codes.isEmpty()) {
            params.put("codes", codes);
        }
        Map<String, Object> finalRequest = apiPacketUtil.pack(params);
        String fullUrl = baseUrl + areaListPath;
        System.out.println("向第三方API发送请求：" + fullUrl);
        String responseStr = HttpClientUtil.sendPost(fullUrl, finalRequest, objectMapper);

        // 2. 处理响应：解密 data -> 反序列化为树形 AreaDTO
        List<AreaDTO> tree = processResponse(responseStr);
        if (tree.isEmpty()) {
            log.warn("地域同步：第三方返回为空，codes={}", codes);
            return 0;
        }

        // 3. 扁平化为 ProxyRegion 集合，并记录 parentCode 以便后续回填 parent_id
        List<ProxyRegion> proxyRegionList = new ArrayList<>();
        Map<String, String> regionCodeToParentCode = new HashMap<>();
        Set<String> seenCodes = new LinkedHashSet<>();
        flattenTree(tree, null, 1, new ArrayList<>(), new ArrayList<>(), proxyRegionList, regionCodeToParentCode, seenCodes);
        log.info("地域列表中的元素总数 {}", proxyRegionList.size());
        if (proxyRegionList.isEmpty()) {
            return 0;
        }

        // 4. 将数据库操作委托给独立的事务方法（短事务），避免长事务占用连接
        return doSyncToDb(proxyRegionList, regionCodeToParentCode);
    }

    /**
     * 真正执行数据库 upsert 的事务方法。
     * 拆分出来是为了将事务范围缩小到纯 DB 操作，不包含 HTTP 请求等耗时逻辑。
     */
    @Transactional(rollbackFor = Exception.class)
    public int doSyncToDb(List<ProxyRegion> proxyRegionList, Map<String, String> regionCodeToParentCode) {
        // 4.1 第一次 upsert：region_code 唯一，先把节点写入（parent_id 暂时为 0）
        int batchModifiedCount = proxyRegionDAO.batchSaveOrUpdate(proxyRegionList);

        // 4.2 查询写入后的 id，回填 parent_id
        List<String> regionCodes = proxyRegionList.stream()
                .map(ProxyRegion::getRegionCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Long> idMap = proxyRegionDAO.selectIdMapByRegionCodes(regionCodes);
        for (ProxyRegion r : proxyRegionList) {
            String parentCode = regionCodeToParentCode.get(r.getRegionCode());
            if (parentCode == null) {
                r.setParentId(0L);
                continue;
            }
            Long parentId = idMap.get(parentCode);
            if (parentId == null) {
                log.warn("地域同步：找不到 parentId，regionCode={}, parentCode={}", r.getRegionCode(), parentCode);
                r.setParentId(0L);
            } else {
                r.setParentId(parentId);
            }
        }

        // 4.3 第二次 upsert：更新 parent_id
        proxyRegionDAO.batchSaveOrUpdate(proxyRegionList);
        log.info("地域信息批量修改或插入完成，总共处理的地域记录数为 {} 条，第三方获取到的地域条数为 {}", batchModifiedCount, proxyRegionList.size());
        return batchModifiedCount;
    }

    /**
     * 解析第三方API响应，解密后转为 AreaDTO 列表
     */
    private List<AreaDTO> processResponse(String responseStr) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) {
                log.warn("地域接口返回 data 为空");
                return Collections.emptyList();
            }

            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("地域接口返回数据解密成功：{}", decryptedJson);

            List<AreaDTO> areaList = objectMapper.readValue(decryptedJson, new TypeReference<List<AreaDTO>>() {
            });
            return areaList != null ? areaList : Collections.emptyList();
        } else {
            throw new BusinessException("地域API错误: " + root.path("msg").asText());
        }
    }

    private void flattenTree(List<AreaDTO> nodes, String parentCode, int level, List<String> codePath,
                             List<String> cnamePath, List<ProxyRegion> out, Map<String, String> regionCodeToParentCode,
                             Set<String> seenCodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            AreaDTO node = nodes.get(i);
            if (node == null || node.getCode() == null) {
                continue;
            }

            String regionCode = node.getCode();
            String cname = node.getCname();
            String nameEn = node.getName();

            // 检测重复 regionCode，重复时打印到控制台
            if (!seenCodes.add(regionCode)) {
                System.out.println("[重复regionCode] code=" + regionCode
                        + ", cname=" + cname
                        + ", parentCode=" + parentCode
                        + ", level=" + level);
            }

            // 构建全路径（full_code/full_name）
            List<String> nextCodePath = new ArrayList<>(codePath);
            List<String> nextCnamePath = new ArrayList<>(cnamePath);
            nextCodePath.add(regionCode);
            nextCnamePath.add(cname);

            String fullCode = String.join("-", nextCodePath);
            String fullName = nextCnamePath.stream().filter(Objects::nonNull).collect(Collectors.joining("-"));

            ProxyRegion proxyRegion = new ProxyRegion();
            proxyRegion.setParentId(0L); // 后续在 upsert 后回填
            proxyRegion.setLevel(level);
            proxyRegion.setRegionCode(regionCode);
            proxyRegion.setRegionName(cname);
            proxyRegion.setRegionEnName(nameEn == null ? "" : nameEn);
            proxyRegion.setSort(i);
            proxyRegion.setFullCode(fullCode);
            proxyRegion.setFullName(fullName);
            proxyRegion.setStatus(1);
            out.add(proxyRegion);

            regionCodeToParentCode.put(regionCode, parentCode);

            // 递归子节点
            flattenTree(node.getChildren(), regionCode, level + 1, nextCodePath, nextCnamePath, out,
                    regionCodeToParentCode, seenCodes);
        }
    }
}
