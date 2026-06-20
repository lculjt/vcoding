package com.vcoding.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("uc_user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String passwordHash;

    private Integer status;

    private Boolean adminFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
