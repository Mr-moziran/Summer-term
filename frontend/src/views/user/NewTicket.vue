<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="flex-between">
          <h2>提交工单</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 800px"
      >
        <el-form-item label="工单标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请简要描述您的问题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="请详细描述您遇到的问题，包括问题现象、发生时间、尝试过的解决方法等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提交工单
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTicket } from '@/api/ticket'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  title: '',
  description: ''
})

const rules = {
  title: [
    { required: true, message: '请输入工单标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度为5-200个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度为10-2000个字符', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await createTicket({ ...form, submitterId: userStore.userId })
      ElMessage.success('工单提交成功')
      router.push('/my-tickets')
    } catch (error) {
      ElMessage.error(error.message || '工单提交失败')
    } finally {
      loading.value = false
    }
  })
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}
</style>
