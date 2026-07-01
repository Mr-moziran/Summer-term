/**
 * Spring Data JPA 仓储层。
 *
 * <p>仓储接口只描述数据库查询能力，不承载业务权限判断。按用户、工单、回复、通知等聚合拆分，
 * 复杂统计查询由 service 层通过 JdbcTemplate 明确编写 SQL。</p>
 */
package com.project.demo.repository;
