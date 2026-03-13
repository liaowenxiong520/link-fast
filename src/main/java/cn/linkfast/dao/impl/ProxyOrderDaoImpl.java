package cn.linkfast.dao.impl;

import cn.linkfast.dao.ProxyOrderDAO;
import cn.linkfast.dto.OrderUpdateResultDTO;
import cn.linkfast.dto.ProxyOrderSearchCondition;
import cn.linkfast.entity.ProxyOrder;
import cn.linkfast.entity.ProxyOrderInstance;
import cn.linkfast.entity.ProxyOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProxyOrderDaoImpl implements ProxyOrderDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderUpdateResultDTO updateProxyOrder(ProxyOrder order) {
        // 改为插入或更新（主键冲突时更新指定字段）
        String orderSql = "INSERT INTO proxy_order (order_no, app_order_no, user_id, order_type, status, total_quantity, amount, has_refund, instance_total) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE status=VALUES(status), amount=VALUES(amount), has_refund=VALUES(has_refund), instance_total=VALUES(instance_total), order_type=VALUES(order_type), order_no=VALUES(order_no)";
        List<Object> params = new ArrayList<>();
        params.add(order.getOrderNo());
        params.add(order.getAppOrderNo());
        params.add(order.getUserId());
        params.add(order.getOrderType());
        params.add(order.getStatus());
        params.add(order.getTotalQuantity());
        params.add(order.getAmount());
        params.add(order.getHasRefund());
        params.add(order.getInstanceTotal());

        // 1. 插入或更新主表数据
        int proxyOrderUpdatedRows = jdbcTemplate.update(orderSql, params.toArray());
        log.info(">>> 订单已插入或更新，单号: {}，影响行数: {}", order.getOrderNo(), proxyOrderUpdatedRows);

        // 2. 从 order 对象中获取实例列表进行批量处理
        List<ProxyOrderInstance> instances = order.getInstances();
        if (instances == null || instances.isEmpty()) {
            log.warn(">>> 订单 {} 不包含任何实例数据，跳过子表更新", order.getOrderNo());
            return new OrderUpdateResultDTO(proxyOrderUpdatedRows, 0);
        }

        String instSql = "INSERT INTO proxy_order_instance (order_no, instance_no, proxy_type, protocol, ip, port, region_id, country_code, city_code, " + "use_type, username, pwd, user_expired, flow_total, flow_balance, status, renew, bridges, open_at, renew_at, release_at, " + "product_no, extend_ip, project_id) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE status=VALUES(status), flow_balance=VALUES(flow_balance), renew_at=VALUES(renew_at), release_at=VALUES(release_at)";

        List<Object[]> batchArgs = instances.stream().map(i -> new Object[]{i.getOrderNo(), i.getInstanceNo(), i.getProxyType(), i.getProtocol(), i.getIp(), i.getPort(), i.getRegionId(), i.getCountryCode(), i.getCityCode(), i.getUseType(), i.getUsername(), i.getPwd(), i.getUserExpired(), i.getFlowTotal(), i.getFlowBalance(), i.getStatus(), i.getRenew(), toJson(i.getBridges()), i.getOpenAt(), i.getRenewAt(), i.getReleaseAt(), i.getProductNo(), i.getExtendIp(), i.getProjectId()}).collect(Collectors.toList());

        int[] results = jdbcTemplate.batchUpdate(instSql, batchArgs);
        log.info(">>> 订单 {} 的实例已成功持久化，数量: {}", order.getOrderNo(), instances.size());

        // 3. 统计实例表更新的行数
        int proxyInstanceUpdatedRows = 0;
        for (int r : results) {
            // r = 1 (新增), r = 2 (更新), r = 0 (无变化)
            // 只要 r >= 0，都说明这条记录在数据库里处理成功了
            if (r >= 0) {
                proxyInstanceUpdatedRows++;
            }
        }
        log.info(">>> 订单及 {} 个实例已成功持久化，单号: {}", instances.size(), order.getOrderNo());
        return new OrderUpdateResultDTO(proxyOrderUpdatedRows, proxyInstanceUpdatedRows);
    }


    /**
     * 根据条件查询代理订单列表
     *
     * @param condition 查询条件
     * @return 订单列表
     */
    @Override
    public List<ProxyOrder> findProxyOrderList(ProxyOrderSearchCondition condition) {
        // 1. 动态拼接SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM proxy_order WHERE 1=1 ");

        // 存储动态参数（避免SQL注入）
        List<Object> params = new ArrayList<>();

        // 2. 拼接查询条件
        // 必传条件：status
        sql.append("AND status = ? ");
        params.add(condition.getStatus());

        // 非必传条件：orderType
        if (condition.getOrderType() != null && !condition.getOrderType().isEmpty()) {
            sql.append("AND order_type = ? ");
            params.add(condition.getOrderType());
        }

        // 非必传条件：orderNo
        if (condition.getOrderNo() != null && !condition.getOrderNo().isEmpty()) {
            sql.append("AND order_no = ? ");
            params.add(condition.getOrderNo());
        }

        // 3. 拼接分页（MySQL LIMIT）
        sql.append("ORDER BY create_time DESC LIMIT ?, ?");
        params.add(condition.getOffset());
        params.add(condition.getLimit());

        // 4. 执行查询并映射为实体（BeanPropertyRowMapper自动映射驼峰字段）
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(ProxyOrder.class), params.toArray());
    }

    /**
     * 查询订单总数（仅统计符合条件的条数）
     */
    @Override
    public int countProxyOrder(ProxyOrderSearchCondition condition) {
        // 1. 动态拼接统计SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM proxy_order WHERE 1=1 ");

        // 存储动态参数
        List<Object> params = new ArrayList<>();

        // 2. 拼接查询条件（与列表查询保持一致）
        sql.append("AND status = ? ");
        params.add(condition.getStatus());

        if (condition.getOrderType() != null && !condition.getOrderType().isEmpty()) {
            sql.append("AND order_type = ? ");
            params.add(condition.getOrderType());
        }

        if (condition.getOrderNo() != null && !condition.getOrderNo().isEmpty()) {
            sql.append("AND order_no = ? ");
            params.add(condition.getOrderNo());
        }

        // 3. 执行统计查询
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }


    /**
     * 将对象（List）转换为 JSON 字符串存入数据库
     */
    private String toJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON转换失败: ", e);
            return "[]";
        }
    }

    /**
     * 回写第三方返回的 orderNo 和 amount
     */
    public int updateProxyOrder(String appOrderNo, String orderNo, java.math.BigDecimal amount) {
        String sql = "UPDATE proxy_order SET order_no=?, amount=? WHERE app_order_no=?";
        return jdbcTemplate.update(sql, orderNo, amount, appOrderNo);
    }

    /**
     * 保存订单主数据和项目数据到数据库
     * 用于开通代理等新建订单场景
     *
     * @param order 包含主表信息和 items 列表的订单对象
     * @return 保存的订单的 appOrderNo（便于后续业务使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveProxyOrder(ProxyOrder order) {

        // 1. 插入主表数据（纯插入，不做更新）
        String orderSql = "INSERT INTO proxy_order (order_no, app_order_no, user_id, order_type, status, total_quantity, amount, has_refund, instance_total, create_time, update_time) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        List<Object> params = new ArrayList<>();
        params.add(order.getOrderNo());
        params.add(order.getAppOrderNo());
        params.add(order.getUserId());
        params.add(order.getOrderType());
        params.add(order.getStatus());
        params.add(order.getTotalQuantity());
        params.add(order.getAmount());
        params.add(order.getHasRefund());
        params.add(order.getInstanceTotal());

        int proxyOrderInsertedRows = jdbcTemplate.update(orderSql, params.toArray());
        log.info(">>> 订单已新建，渠道商订单号: {}，影响行数: {}", order.getAppOrderNo(), proxyOrderInsertedRows);

        // 2. 获取并保存订单项目数据
        List<ProxyOrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            String itemSql = "INSERT INTO proxy_order_item (app_order_no, product_no, proxy_type, use_type, protocol, use_limit, " + "area_code, country_code, state_code, city_code, detail, cost_price, retail_price, " + "ip_type, isp_type, net_type, duration, unit, band_width, band_width_price, max_band_width, flow, " + "cpu, memory, supplier_code, ip_count, ip_duration, parent_no, proxy_everytime_change, " + "proxy_global_random) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            List<Object[]> batchArgs = items.stream().map(item -> new Object[]{order.getAppOrderNo(), item.getProductNo(), item.getProxyType(), item.getUseType(), item.getProtocol(), item.getUseLimit(), item.getAreaCode(), item.getCountryCode(), item.getStateCode(), item.getCityCode(), item.getDetail(), item.getCostPrice(), item.getRetailPrice(), item.getIpType(), item.getIspType(), item.getNetType(), item.getDuration(), item.getUnit(), item.getBandWidth(), item.getBandWidthPrice(), item.getMaxBandWidth(), item.getFlow(), item.getCpu(), item.getMemory(), item.getSupplierCode(), item.getIpCount(), item.getIpDuration(), item.getParentNo(), item.getProxyEverytimeChange(), item.getProxyGlobalRandom()}).collect(Collectors.toList());

            int[] results = jdbcTemplate.batchUpdate(itemSql, batchArgs);
            log.info(">>> 订单 {} 的项目已成功新建，数量: {}", order.getAppOrderNo(), items.size());
        }

        log.info(">>> 订单保存完成，appOrderNo: {}", order.getAppOrderNo());
        return order.getAppOrderNo();
    }

}