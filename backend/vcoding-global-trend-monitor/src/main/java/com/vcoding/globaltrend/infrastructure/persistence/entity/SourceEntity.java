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
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String platformType;
    private String contentType;
    private Boolean enabled;
    private String status;
    private String region;
    private String language;
    private String configJson;
    private LocalDateTime lastSuccessAt;
    private LocalDateTime lastFailureAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
