/**
 * 核心业务服务层。
 *
 * <p>这里承载事务边界、权限所有权校验、领域对象编排、通知触发、统计计算和 AI 建议流程。
 * Controller 不直接访问仓储，避免把业务规则分散到接口层。</p>
 */
package com.project.demo.service;
