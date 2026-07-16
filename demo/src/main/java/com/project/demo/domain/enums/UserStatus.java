package com.project.demo.domain.enums;

/**
 * 用户账号状态枚举。
 *
 * <p>禁用用户即使持有未过期 JWT，也会在认证过滤器中被拒绝访问。</p>
 */
public enum UserStatus {
	ACTIVE,
	DISABLED
}
