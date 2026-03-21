package cn.linkfast.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * 地域信息表（大洲-国家-州省-城市四级）
 * 对应 SQL：docs/api/database/region.sql
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Region {
    private Long id;
    private Long parentId;
    private Integer level;

    private String regionCode;
    private String regionName;
    private String regionEnName;

    private Integer sort;
    private String fullCode;
    private String fullName;

    private Integer status;

    private Date createTime;
    private Date updateTime;
}

