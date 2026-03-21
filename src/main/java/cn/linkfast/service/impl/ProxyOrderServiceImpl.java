package cn.linkfast.service.impl;

import cn.linkfast.common.PageResult;
import cn.linkfast.dao.ProxyOrderDAO;
import cn.linkfast.dao.ProxyProductDAO;
import cn.linkfast.dto.OrderUpdateResultDTO;
import cn.linkfast.dto.ProxyOrderCreateDTO;
import cn.linkfast.dto.ProxyOrderQueryDTO;
import cn.linkfast.dto.ProxyOrderSearchCondition;
import cn.linkfast.entity.ProxyInstance;
import cn.linkfast.entity.ProxyOrder;
import cn.linkfast.entity.ProxyOrderItem;
import cn.linkfast.entity.ProxyProduct;
import cn.linkfast.exception.BusinessException;
import cn.linkfast.service.PayService;
import cn.linkfast.service.ProxyOrderService;
import cn.linkfast.utils.ApiPacketUtil;
import cn.linkfast.vo.PayPasswordVO;
import cn.linkfast.vo.ProxyOrderCreateVO;
import cn.linkfast.vo.ProxyOrderVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyOrderServiceImpl implements ProxyOrderService {

    private final ProxyOrderDAO proxyOrderDAO;
    private final ObjectMapper objectMapper;
    private final ApiPacketUtil apiPacketUtil;
    private final ProxyProductDAO proxyProductDAO;
    private final PayService payService;
    @Value("${api.ipv.env}")
    private String env;
    @Value("${api.ipv.sandbox_url}")
    private String sandboxUrl;
    @Value("${api.ipv.prod_url}")
    private String prodUrl;
    @Value("${api.ipv.path.order_info}")
    private String orderQueryPath;
    @Value("${api.ipv.path.order_create}")
    private String orderOpenPath;
    private String baseUrl; // 动态确定的基础地址


    private static @NonNull ProxyOrderSearchCondition buildSearchCondition(@NonNull ProxyOrderQueryDTO queryDto) {
        ProxyOrderSearchCondition condition = new ProxyOrderSearchCondition();
        BeanUtils.copyProperties(queryDto, condition);

        // 处理分页逻辑：只有当前端传了分页参数时才计算
        if (queryDto.getPageNum() != null && queryDto.getPageSize() != null) {
            condition.setLimit(queryDto.getPageSize());
            // 公式：offset = (当前页码 - 1) * 每页条数
            int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
            condition.setOffset(Math.max(offset, 0)); // 防止负数
        }
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
    @Transactional(rollbackFor = Exception.class)
    public OrderUpdateResultDTO syncOrderDetails(Map<String, Object> params) throws Exception {


        // 拼接完整的请求 URL
        String fullUrl = baseUrl + orderQueryPath;

        // 业务参数转成最终的请求参数
        Map<String, Object> finalRequest = apiPacketUtil.pack(params);

        // 发送 HTTP 请求，返回的是 HTTP 响应体（Response Body）的全文内容
        log.info("同步订单详情 - 请求URL: {}, 请求参数: {}", fullUrl, objectMapper.writeValueAsString(finalRequest));
        String responseStr = sendPost(fullUrl, finalRequest);

        ProxyOrder order = processResponse(responseStr);
        if (order == null) return new OrderUpdateResultDTO();

        // 将订单的 appOrderNo 赋值给每个实例，确保实例表也关联渠道商订单号
        if (order.getInstances() != null && order.getAppOrderNo() != null) {
            for (ProxyInstance instance : order.getInstances()) {
                if (instance.getAppOrderNo() == null) {
                    instance.setAppOrderNo(order.getAppOrderNo());
                }
            }
        }

        // 通过 appOrderNo 查询本地订单，获取 userId 赋值给每个实例
        if (order.getInstances() != null && !order.getInstances().isEmpty()) {
            ProxyOrder localOrder = proxyOrderDAO.findProxyOrder(order.getAppOrderNo());
            if (localOrder != null && localOrder.getUserId() != null) {
                for (ProxyInstance instance : order.getInstances()) {
                    instance.setUserId(localOrder.getUserId());
                }
            }
        }

        return proxyOrderDAO.updateProxyOrder(order);
    }

    @Override
    public PageResult<ProxyOrderVO> getProxyOrders(ProxyOrderQueryDTO dto) {
        // 1. DTO转换为DAO查询条件
        ProxyOrderSearchCondition condition = buildSearchCondition(dto);

        // 2. 调用DAO查询数据（列表+总数）
        List<ProxyOrder> orderList = proxyOrderDAO.findProxyOrderList(condition);
        int total = proxyOrderDAO.countProxyOrder(condition);

        // 3. 实体转换为VO（包含instanceTotal）
        // 4. 将 Entity 转换为 VO (数据脱敏/格式转换)
        List<ProxyOrderVO> voList = orderList.stream().map(this::convertToVO).collect(Collectors.toList());

        // 4. 封装分页结果返回
        return new PageResult<>(total, voList, dto.getPageNum(), dto.getPageSize());

    }

    /**
     * 将数据库实体转换为展示对象
     */
    private ProxyOrderVO convertToVO(ProxyOrder entity) {
        ProxyOrderVO vo = new ProxyOrderVO();
        // 使用 Spring 的 BeanUtils 快速拷贝同名属性
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public String sendPost(String url, Map<String, Object> body) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
            return client.execute(post, response -> EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        }
    }

    private ProxyOrder processResponse(String responseStr) throws Exception {
        log.info("第三方API原始响应: {}", responseStr);
        JsonNode root = objectMapper.readTree(responseStr);
        if (root.path("code").asInt() == 200) {
            String encryptedData = root.path("data").asText();
            if (encryptedData == null || encryptedData.isEmpty()) return null;

            // 解密响应数据
            String decryptedJson = apiPacketUtil.unpack(encryptedData);
            log.info("接口返回数据解密成功: {}", decryptedJson);
            // 将解密后的 JSON 转换为 ProxyOrder
            return objectMapper.readValue(decryptedJson, new TypeReference<>() {
            });
        } else {
            log.error("第三方API返回错误, code={}, msg={}, 完整响应: {}", root.path("code").asInt(), root.path("msg").asText(), responseStr);
            throw new RuntimeException("API错误: " + root.path("msg").asText());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProxyOrderCreateVO createProxyOrder(ProxyOrderCreateDTO dto) {
        // 1. 校验支付密码
        PayPasswordVO payResult = payService.verifyPayPassword(dto.getPayPassword());
        if (!payResult.getPassed()) {
            throw new BusinessException(400, payResult.getMessage());
        }

        /*
          userId 目前没有传入参数，暂时写死一个值，后续可以通过鉴权上下文获取当前用户ID，或者在DTO中添加userId字段由调用方传入
         */
        Long userId = 2032958739262217115L;
        String appOrderNo = userId + "" + System.currentTimeMillis();
        ProxyOrder order = new ProxyOrder();
        order.setAppOrderNo(appOrderNo);
        order.setUserId(userId);
        order.setOrderType(dto.getOrderType());
        order.setTotalQuantity(dto.getTotalQuantity());
        order.setStatus(1); // 待处理
        List<ProxyOrderItem> items = dto.getParams().stream().map(itemDto -> {
            ProxyOrderItem item = new ProxyOrderItem();
            item.setAppOrderNo(appOrderNo);
            item.setProductNo(itemDto.getProductNo());
            item.setProxyType(itemDto.getProxyType());
            item.setCountryCode(itemDto.getCountryCode());
            item.setStateCode(itemDto.getStateCode());
            item.setCityCode(itemDto.getCityCode());
            item.setUnit(itemDto.getUnit());
            item.setDuration(itemDto.getDuration());
            item.setCount(itemDto.getCount());
            item.setCycleTimes(itemDto.getCycleTimes());
            item.setFlow(0);
            item.setUseBridge(1);
//            item.setProjectId(itemDto.getProjectId());

            // 查库补全其它属性
            if (itemDto.getProductNo() != null) {
                ProxyProduct product = proxyProductDAO.findProxyProduct(itemDto.getProductNo());
                if (product == null) {
                    throw new BusinessException(400, "产品不存在: " + itemDto.getProductNo());
                }
                item.setUseType(product.getUseType());
                item.setProtocol(product.getProtocol());
                item.setUseLimit(product.getUseLimit());
                item.setAreaCode(product.getAreaCode());
                item.setDetail(product.getDetail());
                item.setCostPrice(product.getCostPrice());
                item.setRetailPrice(product.getRetailPrice());
                item.setIpType(product.getIpType());
                item.setIspType(product.getIspType());
                item.setNetType(product.getNetType());
                item.setBandWidth(product.getBandWidth());
                item.setBandWidthPrice(product.getBandWidthPrice());
                item.setMaxBandWidth(product.getMaxBandWidth());
                item.setFlow(product.getFlow());
                item.setCpu(product.getCpu());
                item.setMemory(product.getMemory());
                item.setSupplierCode(product.getSupplierCode());
                item.setIpCount(product.getIpCount());
                item.setIpDuration(product.getIpDuration());
                item.setParentNo(product.getParentNo());
                item.setProxyEverytimeChange(product.getProxyEverytimeChange());
                item.setProxyGlobalRandom(product.getProxyGlobalRandom());
                if (product.getProjectList() != null && !product.getProjectList().isEmpty()) {
                    item.setProjectId(product.getProjectList().get(0).getCode());
                }
            }
            return item;
        }).collect(Collectors.toList());
        order.setItems(items);
        proxyOrderDAO.saveProxyOrder(order);
        String orderNo = null;
        java.math.BigDecimal amount = null;
        try {
            Map<String, Object> bizParams = new java.util.HashMap<>();
            bizParams.put("appOrderNo", appOrderNo);
            List<Map<String, Object>> paramList = new java.util.ArrayList<>();
            for (ProxyOrderItem item : items) {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("productNo", item.getProductNo());
                m.put("proxyType", item.getProxyType());
                m.put("countryCode", item.getCountryCode());
                m.put("stateCode", item.getStateCode());
                m.put("cityCode", item.getCityCode());
                m.put("unit", item.getUnit());
                m.put("duration", item.getDuration());
                m.put("count", item.getCount());
                m.put("cycleTimes", item.getCycleTimes());
                m.put("flow", item.getFlow());
                m.put("useBridge", item.getUseBridge());
                if (item.getProjectId() != null) {
                    m.put("projectId", item.getProjectId());
                }
                paramList.add(m);
            }
            bizParams.put("params", paramList);
            Map<String, Object> req = apiPacketUtil.pack(bizParams);
            String url = baseUrl + orderOpenPath;
            String resp = sendPost(url, req);
            ProxyOrder respOrder = processResponse(resp);
            if (respOrder != null) {
                orderNo = respOrder.getOrderNo();
                amount = respOrder.getAmount();
                OrderUpdateResultDTO updateResult = proxyOrderDAO.updateProxyOrder(appOrderNo, orderNo, amount);
                // 校验主订单更新行数，预期更新 1 条
                if (updateResult.getProxyOrderUpdatedRows() != 1) {
                    throw new BusinessException("回写订单失败，主订单预期更新1条，实际更新" + updateResult.getProxyOrderUpdatedRows() + "条，appOrderNo=" + appOrderNo);
                }
                // 校验子订单更新行数
                int expectSubCount = order.getItems().size();
                int actualSubCount = updateResult.getProxyOrderItemUpdatedRows();
                if (actualSubCount != expectSubCount) {
                    throw new BusinessException("子订单更新异常！预期更新" + expectSubCount + "条，实际更新" + actualSubCount + "条，触发事务回滚");
                }
            }
        } catch (Exception e) {
            throw new BusinessException("订单创建失败，请稍后重试", e);
        }
        ProxyOrderCreateVO vo = new ProxyOrderCreateVO();
        vo.setAppOrderNo(appOrderNo);
        vo.setStatus(1); // 待处理
        vo.setOrderNo(orderNo);
        vo.setAmount(amount);
        return vo;
    }

    @Override
    public ProxyOrderVO getProxyOrder(String appOrderNo) {
        ProxyOrder order = proxyOrderDAO.findProxyOrder(appOrderNo);
        if (order == null) {
            return null;
        }
        return convertToVO(order);
    }

}


