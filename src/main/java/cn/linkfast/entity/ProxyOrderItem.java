package cn.linkfast.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 代理订单明细表实体类
 * 对应表：proxy_order_item
 *
 * @author liaowenxiong
 * @version 1.0
 * @since 2026/3/13
 */
@Data
public class ProxyOrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 关联的主订单号（代理订单号）
     */
    private String orderNo;

    /**
     * 关联的渠道商订单号
     */
    private String appOrderNo;

    /**
     * 产品编号
     */
    private String productNo;

    /**
     * 代理类型
     */
    private Integer proxyType;

    /**
     * 使用方式
     */
    private String useType;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 使用限制
     */
    private Integer useLimit;

    /**
     * 区域代码
     */
    private String areaCode;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 州省代码
     */
    private String stateCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 商品描述
     */
    private String detail;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;


    /**
     * ip类型
     */
    private Integer ipType;

    /**
     * isp类型
     */
    private Integer ispType;

    /**
     * 网络类型
     */
    private Integer netType;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * 单位
     */
    private Integer unit;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 购买时长周期数
     */
    private Integer cycleTimes;

    /**
     * 带宽|流量时必要 单位 MB
     */
    private Integer bandWidth;

    /**
     * 额外带宽价格
     */
    private BigDecimal bandWidthPrice;

    /**
     * 可设置最大带宽
     */
    private Integer maxBandWidth;

    /**
     * 动态代理按照流量方式 最小购买和计价流量包 单位MB
     */
    private Integer flow;

    /**
     * 是否使用代理桥接 1-不使用 2-使用
     */
    private Integer useBridge;

    /**
     * cpu数
     */
    private Integer cpu;

    /**
     * 内存容量
     */
    private BigDecimal memory;

    /**
     * 供应商代码 后续该字段逐步废弃，业务不应该依赖该字段
     */
    private String supplierCode;

    /**
     * 动态代理按照ip数方式 最小购买和计价ip数 单位个
     */
    private Integer ipCount;

    /**
     * 动态代理按照ip数方式 时长 单位分钟
     */
    private Integer ipDuration;

    /**
     * 父产品编号
     */
    private String parentNo;

    /**
     * 动态代理账密提取 是否支持每次更换代理
     */
    private Integer proxyEverytimeChange;

    /**
     * 动态代理提取 是否支持全球混播
     */
    private Integer proxyGlobalRandom;

    /**
     * 动态代理Api提取是否支持全球混播
     */
    private Integer apiDrawGlobalRandom;

    /**
     * 动态代理是否支持IP白名单功能
     */
    private Integer ipWhiteList;

    /**
     * 动态代理账密提取是否支持子账号
     */
    private Integer pwdDrawProxyUser;

    /**
     * 动态代理子账号是否支持流量上限管理
     */
    private Integer proxyUserFlowLimit;

    /**
     * 动态代理是否支持流量明细查询
     */
    private Integer flowUseLog;

    /**
     * 动态代理账密流量提取持续时间范围 单位分钟
     */
    private String pwdDrawSessionRange;

    /**
     * 动态代理流量进制转化基准 1000 或者 1024 0表示未知或不支持
     */
    private Integer flowConversionBase;

    /**
     * 产品是共享还是独享
     */
    private Integer productType;
    /**
     * 购买项目code
     */
    private String projectId;
    private Date createTime;
    private Date updateTime;
}