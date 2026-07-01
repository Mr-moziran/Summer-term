/**
 * API 请求与响应 DTO。
 *
 * <p>DTO 是前后端接口契约的一部分，用于隔离 JPA 实体和外部 JSON 结构。请求 DTO 放置校验注解，
 * 响应 DTO 只暴露页面需要的字段，避免把懒加载实体或内部状态直接序列化给前端。</p>
 */
package com.project.demo.dto;
