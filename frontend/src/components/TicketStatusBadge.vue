<template>
  <div class="ticket-status-badge">
    <el-tag :type="statusConfig.type">
      {{ statusConfig.label }}
    </el-tag>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { TICKET_STATUS } from '@/utils/constants'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusConfig = computed(() => TICKET_STATUS[props.status] || { label: props.status, type: 'info' })
</script>
