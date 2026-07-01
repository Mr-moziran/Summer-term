/**
 * HTTP 控制器层。
 *
 * <p>控制器只负责协议适配：读取路径、查询参数和请求体，获取当前登录用户，调用 service，
 * 再把领域对象转换为 DTO。权限入口主要由 Spring Security 和 service 内部所有权校验共同完成。</p>
 */
package com.project.demo.controller;
