package cn.linkfast.dao.impl;

import cn.linkfast.dao.ProxyInstanceDAO;
import cn.linkfast.dto.ProxyInstanceSearchCondition;
import cn.linkfast.entity.ProxyInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理实例数据访问实现类
 *
 * @author liaowenxiong
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProxyInstanceDaoImpl implements ProxyInstanceDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public int batchSaveOrUpdate(List<ProxyInstance> instances) {
        log.info("批量保存或更新代理实例数据，数量：{}", instances.size());
        if (instances.isEmpty()) {
            return 0;
        }

        String sql = "UPDATE proxy_instance SET " +
                "order_no=?, proxy_type=?, protocol=?, ip=?, port=?, region_id=?, country_code=?, city_code=?, " +
                "use_type=?, username=?, pwd=?, user_expired=?, flow_total=?, flow_balance=?, status=?, renew=?, bridges=?, " +
                "open_at=?, renew_at=?, release_at=?, product_no=?, extend_ip=?, project_id=?, " +
                "update_time=CURRENT_TIMESTAMP " +
                "WHERE instance_no=?";

        List<Object[]> batchArgs = instances.stream().map(i -> new Object[]{
                i.getOrderNo(),
                i.getProxyType(),
                i.getProtocol(),
                i.getIp(),
                i.getPort(),
                i.getRegionId(),
                i.getCountryCode(),
                i.getCityCode(),
                i.getUseType(),
                i.getUsername(),
                i.getPwd(),
                i.getUserExpired(),
                i.getFlowTotal(),
                i.getFlowBalance(),
                i.getStatus(),
                i.getRenew(),
                toJson(i.getBridges()),
                i.getOpenAt(),
                i.getRenewAt(),
                i.getReleaseAt(),
                i.getProductNo(),
                i.getExtendIp(),
                i.getProjectId(),
                i.getInstanceNo()
        }).collect(Collectors.toList());

        int[] results = jdbcTemplate.batchUpdate(sql, batchArgs);

        int successCount = 0;
        for (int r : results) {
            if (r >= 0) {
                successCount++;
            }
        }
        log.info("批量保存或更新代理实例数据，成功数量：{}", successCount);
        return successCount;
    }

    @Override
    public List<ProxyInstance> findProxyInstances(ProxyInstanceSearchCondition condition) {
        StringBuilder sql = new StringBuilder("SELECT * FROM proxy_instance WHERE proxy_type = ? AND status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(condition.getProxyType());
        params.add(condition.getStatus());

        appendOptionalConditions(sql, params, condition);

        sql.append("ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(condition.getLimit());
        params.add(condition.getOffset());

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(ProxyInstance.class), params.toArray());
    }

    @Override
    public int countProxyInstance(ProxyInstanceSearchCondition condition) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM proxy_instance WHERE proxy_type = ? AND status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(condition.getProxyType());
        params.add(condition.getStatus());

        appendOptionalConditions(sql, params, condition);

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    /**
     * 拼接可选查询条件（列表查询和统计查询共用）
     */
    private void appendOptionalConditions(StringBuilder sql, List<Object> params, ProxyInstanceSearchCondition condition) {
        if (condition.getCountryCode() != null && !condition.getCountryCode().isEmpty()) {
            sql.append("AND country_code = ? ");
            params.add(condition.getCountryCode());
        }
        if (condition.getCityCode() != null && !condition.getCityCode().isEmpty()) {
            sql.append("AND city_code = ? ");
            params.add(condition.getCityCode());
        }
        if (condition.getRenew() != null) {
            sql.append("AND renew = ? ");
            params.add(condition.getRenew());
        }
        if (condition.getIp() != null && !condition.getIp().isEmpty()) {
            sql.append("AND ip LIKE ? ");
            params.add("%" + condition.getIp() + "%");
        }
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
}

