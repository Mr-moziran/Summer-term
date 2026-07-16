<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>知识库管理</h2>
            <p>上传 .txt 或 .md 文档，后台会切分文本并写入向量库，供 AI 自助问答检索。</p>
          </div>
          <el-button @click="goDashboard">返回后台</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="90px" class="upload-form">
        <el-form-item label="文档标题">
          <el-input v-model="form.title" placeholder="可选，不填则使用文件名" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="文档文件">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-remove="handleFileRemove"
            accept=".txt,.md,text/plain,text/markdown"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">仅支持 .txt / .md，单文件不超过 5MB。</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleUpload">上传知识库文档</el-button>
          <el-button @click="resetForm">清空</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="lastResult"
        class="upload-result"
        type="success"
        :closable="false"
        show-icon
      >
        <template #title>
          已上传 {{ lastResult.title }}，生成 {{ lastResult.chunkCount }} 个知识片段。
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadKnowledgeDocument } from '@/api/admin'

const router = useRouter()
const uploadRef = ref()
const uploading = ref(false)
const selectedFile = ref(null)
const lastResult = ref(null)
const form = reactive({
  title: ''
})

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = null
  return true
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的知识库文档')
    return
  }

  uploading.value = true
  try {
    lastResult.value = await uploadKnowledgeDocument(selectedFile.value, form.title.trim())
    ElMessage.success('知识库文档上传成功')
    resetForm(false)
  } catch (error) {
    console.error('上传知识库文档失败:', error)
  } finally {
    uploading.value = false
  }
}

function resetForm(clearResult = true) {
  form.title = ''
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  if (clearResult) {
    lastResult.value = null
  }
}

function goDashboard() {
  router.push('/admin/dashboard')
}
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  h2 {
    margin: 0 0 8px;
  }

  p {
    margin: 0;
    color: #606266;
    line-height: 1.6;
  }
}

.upload-form {
  max-width: 760px;
}

.upload-result {
  margin-top: 20px;
}
</style>
