param(
    [string]$SourcePath = "data/raw/customer-support-ticket-dataset/customer_support_tickets.csv",
    [string]$OutputPath = "data/processed/kaggle-ticket-candidates.csv",
    [int]$SamplesPerType = 100
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $SourcePath)) {
    throw "Source dataset was not found: $SourcePath"
}

$categoryMapping = @{
    'Technical issue' = 'TECHNICAL'
    'Refund request' = 'BILLING'
    'Billing inquiry' = 'BILLING'
    'Cancellation request' = 'BILLING'
    'Product inquiry' = 'OTHER'
}

$priorityMapping = @{
    'Critical' = 'URGENT'
    'High' = 'HIGH'
    'Medium' = 'MEDIUM'
    'Low' = 'LOW'
}

$statusMapping = @{
    'Open' = 'PENDING'
    'Pending Customer Response' = 'PROCESSING'
    'Closed' = 'CLOSED'
}

function ConvertTo-SanitizedText([object]$Value) {
    if ($null -eq $Value) {
        return ''
    }

    $text = [string]$Value
    $text = $text -replace '(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}', '[redacted-email]'
    $text = $text -replace '(?i)\[?email\s*[._-]?\s*protected\]?', '[redacted-email]'
    $text = $text -replace '(?i)https?://\S+', '[redacted-url]'
    $text = $text -replace '(?<!\d)(?:\+?\d[\d\s().-]{7,}\d)(?!\d)', '[redacted-phone]'
    return $text.Trim()
}

$rows = Import-Csv -LiteralPath $SourcePath
$candidates = foreach ($group in $rows | Group-Object 'Ticket Type') {
    $group.Group |
        Sort-Object { [int]$_['Ticket ID'] } |
        Select-Object -First $SamplesPerType |
        ForEach-Object {
            [PSCustomObject]@{
                source_ticket_id = $_.'Ticket ID'
                source_type = $_.'Ticket Type'
                mapped_category = $categoryMapping[$_.'Ticket Type']
                mapped_priority = $priorityMapping[$_.'Ticket Priority']
                mapped_status = $statusMapping[$_.'Ticket Status']
                subject_en = ConvertTo-SanitizedText $_.'Ticket Subject'
                description_en = ConvertTo-SanitizedText $_.'Ticket Description'
                resolution_en = ConvertTo-SanitizedText $_.'Resolution'
                requires_saas_rewrite = $true
                rewrite_note = 'Pattern reference only. Rewrite for the Chinese SaaS scenario before use.'
            }
        }
}

$outputDirectory = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
$candidates | Export-Csv -LiteralPath $OutputPath -NoTypeInformation -Encoding utf8

Write-Output "Generated $($candidates.Count) sanitized ticket candidates: $OutputPath"
