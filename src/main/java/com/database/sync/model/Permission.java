package com.database.sync.model;

import lombok.Data;

@Data
public class Permission {
    private Long id;
    private String permissionName;
    private String permissionCode;
    private String description;
    private Integer status;
    private String url;
    private String method;
    private Long parentId;
    private Integer level;
}