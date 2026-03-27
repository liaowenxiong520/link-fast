package cn.linkfast.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 代理实例列表展示VO（前端需要的字段）
 */
@Data
public class ProxyInstanceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String ip;
    private Integer port;
    private String regionId;
    private String regionName;
    private Integer status;
    private String username;
    private String pwd;
    private String instanceNo;
    private Integer renew;
    private String orderNo;
    private String productNo;
    private Long userExpired;
    private String remark;
    private Date createTime;
}
