package com.vcoding.globaltrend.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("gtm_source")
public class SourceEntity {
    /** 数据源主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定数据源编码，例如 youtube、github，用于匹配 connector。 */
    private String code;
    /** 页面展示的数据源名称。 */
    private String name;
    /** 平台类型，例如 VIDEO、CODE，用于图表和管理页分组。 */
    private String platformType;
    /** 内容类型，例如 VIDEO、REPOSITORY，用于热点列表筛选。 */
    private String contentType;
    /** 是否启用采集；停用后手动和定时采集都不应执行。 */
    private Boolean enabled;
    /** 数据源运行状态，例如 ENABLED、DISABLED、ERROR_DEGRADED。 */
    private String status;
    /** 默认采集地区，例如 YouTube 的 US；不分地区的平台可为空。 */
    private String region;
    /** 默认内容语言，例如 en。 */
    private String language;
    /** 非敏感采集配置 JSON；API Key 等密钥不允许放这里。 */
    private String configJson;
    /** 最近一次成功完成采集的时间。 */
    private LocalDateTime lastSuccessAt;
    /** 最近一次采集失败或部分失败的时间。 */
    private LocalDateTime lastFailureAt;
    /** 数据源记录创建时间。 */
    private LocalDateTime createdAt;
    /** 数据源记录最近更新时间。 */
    private LocalDateTime updatedAt;
}
