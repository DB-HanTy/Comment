package com.hty.comment.dto;

import lombok.Data;

/**
 *用户基本信息
 */
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
