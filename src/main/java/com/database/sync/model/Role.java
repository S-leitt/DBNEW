package com.database.sync.model;

import lombok.Data;

import java.util.List;

@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private List<Permission> permissions;
}