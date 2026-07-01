/**
 * JPA 领域实体。
 *
 * <p>实体类映射数据库业务表，并封装少量状态变更规则，例如工单分配、状态流转、评分和 AI 分类标记。
 * 复杂授权、通知和 AI 编排不放在实体中，而由 service 层完成。</p>
 */
package com.project.demo.entity;
