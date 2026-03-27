package cn.linkfast.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 代理实例表
 */
@Data
public class ProxyInstance {
    private Long id;
    private Long orderId;         // 代理购买订单的id
    private String orderNo;       // 平台订单号
    private String appOrderNo;    // 渠道商订单号
    private String instanceNo;    // 平台实例编号，非空
    private Integer proxyType;    // 代理类型
    private String protocol;      // 协议类型
    private String ip;            // 代理地址
    private Integer port;         // 代理端口
    private String regionId;      // 区域地址
    private String countryCode;   // 国家代码
    private String cityCode;      // 城市代码
    private String useType;       // 使用方式
    private String username;      // 使用代理实例的账户名
    private String pwd;           // 使用代理实例的密码
    private Long userId;          // 渠道商系统的买家id
    private Long userExpired;     // 到期时间（时间戳，秒）
    private BigDecimal flowTotal; // 总流量(MB)/IP数
    private BigDecimal flowBalance;// 剩余流量(MB)
    private Integer status;       // 实例状态
    private Integer renew;        // 是否自动续费


    private List<String> bridges;       // 桥地址列表（JSON存储）

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+8")
    private Date openAt;          // 实例开通时间
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+8")
    private Date renewAt;         // 最后成功续费时间
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+8")
    private Date releaseAt;       // 实例释放成功时间
    private String productNo;     // 产品编号
    private String extendIp;      // 扩展地址
    private String projectId;     // 项目code
    private String remark;
    private Date createTime;      // 创建时间，非空
    private Date updateTime;      // 更新时间，非空


}

