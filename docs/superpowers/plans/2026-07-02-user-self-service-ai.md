# User Self-Service AI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a logged-in user self-service AI endpoint that answers from retrieved knowledge when possible and creates a human-support ticket when the user asks for escalation or knowledge is insufficient.

**Architecture:** Add the feature inside `com.project.demo.ai` while reusing `CurrentUserService` and `TicketService`. The service is contract-first: `KnowledgeSearch` retrieves references, `AskAiClient` decides answer versus escalation request, and `AskAiService` owns thresholds and final ticket creation so model output cannot override submitter or status.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Security, Spring Validation, Spring AI `ChatClient`, Spring AI `VectorStore`, JUnit 5, Mockito, MockMvc.

---

### Task 1: Contract and Service Tests

**Files:**
- Create: `demo/src/test/java/com/project/demo/ai/AskAiServiceTests.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiResultType.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiRequest.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiResponse.java`
- Create: `demo/src/main/java/com/project/demo/ai/KnowledgeReferenceResponse.java`
- Create: `demo/src/main/java/com/project/demo/ai/EscalatedTicketResponse.java`
- Create: `demo/src/main/java/com/project/demo/ai/KnowledgeDocument.java`
- Create: `demo/src/main/java/com/project/demo/ai/KnowledgeSearch.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiClient.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiDecision.java`
- Create: `demo/src/main/java/com/project/demo/ai/EscalationRequest.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiProperties.java`
- Create: `demo/src/main/java/com/project/demo/ai/AskAiService.java`

- [x] **Step 1: Write failing service tests**

Create `AskAiServiceTests` with tests for high confidence answer, medium confidence warning answer, low confidence escalation, explicit transfer escalation, and submitter ownership.

- [x] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AskAiServiceTests test`

Expected: compilation fails because `AskAiService` and DTO contracts do not exist.

- [x] **Step 3: Implement minimal contracts and service**

Add DTOs, ports, properties, and `AskAiService`. The service trims the question, checks keyword escalation first, searches knowledge, applies thresholds, calls `AskAiClient`, and creates tickets through `TicketService` only with the current user id.

- [x] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AskAiServiceTests test`

Expected: tests pass.

### Task 2: Local and PgVector Knowledge Implementations

**Files:**
- Create: `demo/src/main/java/com/project/demo/ai/NoopKnowledgeSearch.java`
- Create: `demo/src/main/java/com/project/demo/ai/PgVectorKnowledgeSearch.java`
- Create: `demo/src/main/java/com/project/demo/ai/LocalAskAiClient.java`
- Test: `demo/src/test/java/com/project/demo/ai/LocalAskAiClientTests.java`
- Test: `demo/src/test/java/com/project/demo/ai/PgVectorKnowledgeSearchTests.java`

- [x] **Step 1: Write failing implementation tests**

Add tests proving local client answers from provided references, requests escalation for explicit transfer wording, and PgVector documents map metadata to `KnowledgeDocument`.

- [x] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=LocalAskAiClientTests,PgVectorKnowledgeSearchTests test`

Expected: compilation fails because implementations do not exist.

- [x] **Step 3: Implement minimal local and vector implementations**

`NoopKnowledgeSearch` returns an empty list by default. `PgVectorKnowledgeSearch` uses `VectorStore.similaritySearch` with configurable topK and threshold. `LocalAskAiClient` creates deterministic answers from references and escalation requests from explicit user wording.

- [x] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=LocalAskAiClientTests,PgVectorKnowledgeSearchTests test`

Expected: tests pass.

### Task 3: REST Endpoint and Security

**Files:**
- Create: `demo/src/main/java/com/project/demo/ai/AskAiController.java`
- Modify: `demo/src/main/java/com/project/demo/security/SecurityConfig.java`
- Test: `demo/src/test/java/com/project/demo/ai/AskAiControllerTests.java`

- [x] **Step 1: Write failing controller tests**

Add tests for unauthenticated 401, logged-in user answered response, and explicit transfer creating a visible `PENDING` ticket.

- [x] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AskAiControllerTests test`

Expected: request returns 404 or compilation fails because controller does not exist.

- [x] **Step 3: Implement controller and security rule**

Add `POST /api/ai/ask`. In `SecurityConfig`, place `requestMatchers(HttpMethod.POST, "/api/ai/ask").authenticated()` before `/api/ai/**` agent/admin rule.

- [x] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AskAiControllerTests test`

Expected: tests pass.

### Task 4: DeepSeek Ask Client and Configuration

**Files:**
- Create: `demo/src/main/java/com/project/demo/ai/DeepSeekAskAiClient.java`
- Modify: `demo/src/main/resources/application.yml`

- [x] **Step 1: Add DeepSeek client**

Use `ChatClient` with a strict system prompt and structured response contract. The client maps model output into `AskAiDecision`; actual ticket creation remains in `AskAiService`.

- [x] **Step 2: Add configuration defaults**

Set `app.ai.ask.provider=local`, `app.ai.ask.knowledge.provider=none`, `app.ai.ask.high-threshold=0.82`, `app.ai.ask.medium-threshold=0.70`.

- [x] **Step 3: Run focused AI tests**

Run: `mvn -Dtest=AskAiServiceTests,LocalAskAiClientTests,PgVectorKnowledgeSearchTests test`

Expected: tests pass.

### Task 5: Full Verification and Commit

**Files:**
- Verify all touched files.

- [x] **Step 1: Run full test suite**

Run: `mvn test`

Expected: build success with all tests passing.

- [x] **Step 2: Run repository hygiene checks**

Run:

```powershell
git diff --check
git diff --cached --check
git diff | Select-String -Pattern 'password|secret|api_key|token|DS_API_KEY|EMBEDDING_API_KEY|JWT_SECRET' -CaseSensitive:$false
```

Expected: no whitespace errors; sensitive scan only shows known placeholder/property names if any.

- [x] **Step 3: Commit**

Run:

```powershell
git add docs/superpowers/plans/2026-07-02-user-self-service-ai.md demo/src/main/java demo/src/test/java demo/src/main/resources/application.yml
git commit -m "feat: 增加用户自助问答链路"
```
