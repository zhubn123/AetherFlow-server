package com.berlin.aetherflow.enums;

import lombok.Getter;

@Getter
public enum UserType {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String code;
    private final String description;

    UserType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
