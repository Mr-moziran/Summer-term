/**
 * 业务异常与统一异常处理。
 *
 * <p>本包把常见错误转换为统一的 {@code ApiErrorResponse}，使前端能够稳定处理 400、401、403、404、409
 * 等错误场景。业务代码只抛出语义明确的异常，不直接拼 HTTP 响应。</p>
 */
package com.project.demo.exception;
