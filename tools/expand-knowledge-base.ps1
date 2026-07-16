param(
    [string]$Path = "demo/src/main/resources/db/knowledge-base.json"
)

$ErrorActionPreference = 'Stop'
$base = @(Get-Content -LiteralPath $Path -Raw -Encoding utf8 | ConvertFrom-Json)
if ($base.Count -eq 1 -and $base[0] -is [System.Array]) {
    $base = @($base[0])
}
if ($base.Count -eq 300) {
    Write-Output 'Knowledge base is already expanded to 300 entries.'
    return
}
if ($base.Count -ne 60) {
    throw "Expected 60 base FAQ entries, found $($base.Count)."
}

$situations = @{
    ACCOUNT = @('新成员首次登录时', '企业邮箱收信异常时', '管理员刚完成成员操作后', '重复操作后仍未恢复时')
    BILLING = @('订单详情与支付渠道显示不一致时', '套餐刚变更后', '财务核对账单时', '需要人工核验订单信息时')
    TECHNICAL = @('使用最新版 Chrome 时', '使用 Edge 或 Firefox 时', '清理缓存并重新登录后', '多个成员在同一时段遇到时')
    TICKET = @('刚提交工单后', '等待客服回复期间', '在原工单补充信息时', '问题再次出现时')
    COMPLAINT = @('需要补充处理事实时', '希望复核原回复时', '客服处理结果不符合预期时', '需要升级人工处理时')
    SECURITY = @('发现异常操作时', '在共享设备登录后', '收到可疑信息时', '需要保护账号资料时')
    OTHER = @('团队日常协作中', '管理员规划流程时', '需要评估产品能力时', '需要人工确认例外情况时')
}

$expanded = [System.Collections.Generic.List[object]]::new()
$expanded.AddRange($base)
$id = 61
foreach ($entry in $base) {
    foreach ($situation in $situations[$entry.category]) {
        $expanded.Add([pscustomobject]@{
            id = $id
            title = "$($entry.title)（$situation）"
            category = $entry.category
            content = "常见使用情境：$situation。$($entry.content)"
            source = '课程演示知识库情境扩展（参考公开 SaaS 常见支持主题）'
        })
        $id++
    }
}

$expanded | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $Path -Encoding utf8
Write-Output "Expanded knowledge base to $($expanded.Count) entries."
