# Kaggle 工单清洗与映射

## 输入与输出

- 原始文件：`data/raw/customer-support-ticket-dataset/customer_support_tickets.csv`，已被 Git 忽略。
- 候选文件：`data/processed/kaggle-ticket-candidates.csv`，已被 Git 忽略。
- 清洗脚本：`tools/prepare-kaggle-tickets.ps1`。

运行：

```powershell
.\tools\prepare-kaggle-tickets.ps1
```

## 隐私处理

以下原始字段不进入候选文件，也不得进入项目种子数据：客户姓名、邮箱、年龄、性别、购买日期、渠道、首次响应时间和解决耗时。脚本还会对标题、描述和解决方案中的邮箱、网址与手机号做正则脱敏。

## 映射规则

| Kaggle 字段 | 项目字段 | 映射 |
|---|---|---|
| `Technical issue` | `category` | `TECHNICAL` |
| `Refund request`、`Billing inquiry`、`Cancellation request` | `category` | `BILLING` |
| `Product inquiry` | `category` | `OTHER` |
| `Critical` | `priority` | `URGENT` |
| `High` / `Medium` / `Low` | `priority` | 同名值 |
| `Open` | `status` | `PENDING` |
| `Pending Customer Response` | `status` | `PROCESSING` |
| `Closed` | `status` | `CLOSED` |

## 后续人工改写

候选数据中的商品名称、英语表述和原始解决方案不适合直接导入。每条被采用的数据必须：

1. 改写为中文企业 SaaS 场景；
2. 与 `业务规则基准.md` 保持一致；
3. 删除特定商品、客户和交易信息；
4. 仅将有明确解决方案的条目作为“历史解决案例”；
5. 绝不将原始工单直接作为 FAQ 标准答案。

## 已采用的案例

`demo/src/main/resources/db/kaggle-curated-tickets.sql` 已收录 50 条按上述规则重写的中文 SaaS 历史解决案例及对应客服回复，分两批覆盖技术故障、账单处理、权限和产品咨询场景。这些案例只用于工单列表演示和相似工单检索；FAQ 仍以 `knowledge-base.json` 为唯一标准答案来源。
