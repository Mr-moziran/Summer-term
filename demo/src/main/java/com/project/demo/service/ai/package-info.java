/**
 * AI 能力适配层。
 *
 * <p>通过 {@code TicketAiClient}、{@code SimilarTicketSearch}、{@code ResolvedTicketIndex} 三个端口隔离真实模型、
 * 本地默认实现和 PgVectorStore。默认配置使用 local/none，保证本地开发和测试不依赖外部模型或向量库。</p>
 */
package com.project.demo.service.ai;
