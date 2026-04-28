<template>
  <div class="trip-form-page">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑旅程' : '创建旅程' }}</h2>
          <el-button text @click="handleCancel">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      
      <el-form
        ref="tripFormRef"
        :model="tripForm"
        :rules="tripRules"
        label-width="100px"
        class="trip-form"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="旅程名称" prop="title">
              <el-input v-model="tripForm.title" placeholder="请输入旅程名称" size="large" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="目的地" prop="destination">
              <el-input v-model="tripForm.destination" placeholder="请输入目的地" size="large" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="tripForm.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="tripForm.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="tripForm.status" placeholder="选择状态" size="large" style="width: 200px">
            <el-option label="规划中" value="PLANNING" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="tripForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入旅程描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="标签">
          <el-select
            v-model="tripForm.tagNames"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或创建标签"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.name"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div
              v-if="tripForm.coverImage"
              class="cover-preview"
              @click="handlePreviewCover"
            >
              <img :src="tripForm.coverImage" alt="封面图" />
              <div class="cover-overlay">
                <el-icon :size="24"><ZoomIn /></el-icon>
                <span>预览</span>
              </div>
            </div>
            
            <el-upload
              v-else
              class="cover-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :limit="1"
              :on-success="handleCoverUploadSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept="image/*"
            >
              <div class="upload-placeholder">
                <el-icon :size="48"><Plus /></el-icon>
                <span>点击上传封面图</span>
                <span class="upload-hint">支持 JPG、PNG、GIF 格式，最大 50MB</span>
              </div>
            </el-upload>
            
            <el-button
              v-if="tripForm.coverImage"
              type="danger"
              text
              class="remove-cover"
              @click="handleRemoveCover"
            >
              <el-icon><Delete /></el-icon>
              删除封面
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="公开分享">
          <el-switch
            v-model="tripForm.isPublic"
            active-text="公开"
            inactive-text="私密"
          />
          <span class="form-hint">公开后其他用户可通过分享链接查看</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建旅程' }}
          </el-button>
          <el-button size="large" @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-dialog v-model="previewVisible" title="封面预览" width="60%">
      <img :src="tripForm.coverImage" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTripById, createTrip, updateTrip } from '@/api/trip'
import { getTags } from '@/api/tag'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const tripFormRef = ref(null)
const loading = ref(false)
const previewVisible = ref(false)
const availableTags = ref([])

const isEdit = computed(() => !!route.params.id)

const uploadAction = '/api/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const tripForm = reactive({
  title: '',
  destination: '',
  startDate: null,
  endDate: null,
  status: 'PLANNING',
  description: '',
  coverImage: '',
  isPublic: false,
  tagNames: []
})

const validateEndDate = (rule, value, callback) => {
  if (value && tripForm.startDate && value < tripForm.startDate) {
    callback(new Error('结束日期不能早于开始日期'))
  } else {
    callback()
  }
}

const tripRules = {
  title: [
    { required: true, message: '请输入旅程名称', trigger: 'blur' },
    { max: 100, message: '旅程名称不能超过100个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    { validator: validateEndDate, trigger: 'change' }
  ]
}

const loadAvailableTags = async () => {
  try {
    const response = await getTags()
    availableTags.value = response.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

const loadTripData = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const response = await getTripById(route.params.id)
    const trip = response.data
    
    tripForm.title = trip.title || ''
    tripForm.destination = trip.destination || ''
    tripForm.startDate = trip.startDate
    tripForm.endDate = trip.endDate
    tripForm.status = trip.status || 'PLANNING'
    tripForm.description = trip.description || ''
    tripForm.coverImage = trip.coverImage || ''
    tripForm.isPublic = trip.isPublic || false
    tripForm.tagNames = (trip.tags || []).map(tag => tag.name)
  } catch (error) {
    ElMessage.error('加载旅程信息失败')
    router.push('/trips')
  } finally {
    loading.value = false
  }
}

const handleCoverUploadSuccess = (response) => {
  if (response.success) {
    tripForm.coverImage = response.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  console.error('Upload error:', error)
  ElMessage.error('上传失败')
}

const handleRemoveCover = () => {
  tripForm.coverImage = ''
}

const handlePreviewCover = () => {
  previewVisible.value = true
}

const handleSubmit = async () => {
  if (!tripFormRef.value) return
  
  await tripFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        if (isEdit.value) {
          await updateTrip(route.params.id, tripForm)
          ElMessage.success('保存成功')
        } else {
          await createTrip(tripForm)
          ElMessage.success('创建成功')
        }
        router.push('/trips')
      } catch (error) {
        ElMessage.error(isEdit.value ? '保存失败' : '创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleCancel = () => {
  router.push('/trips')
}

onMounted(() => {
  loadAvailableTags()
  loadTripData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.trip-form-page {
  max-width: 900px;
}

.form-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h2 {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
    }
  }
}

.trip-form {
  max-width: 700px;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-preview {
  position: relative;
  width: 300px;
  height: 180px;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid $border-light;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .cover-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    opacity: 0;
    transition: opacity 0.3s;
    gap: 8px;
  }
  
  &:hover .cover-overlay {
    opacity: 1;
  }
}

.cover-uploader {
  :deep(.el-upload) {
    width: 300px;
    height: 180px;
  }
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  border: 2px dashed $border-color;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: $text-secondary;
  
  &:hover {
    border-color: $primary-color;
    background: rgba(59, 130, 246, 0.02);
  }
  
  span {
    margin-top: 12px;
    font-size: 14px;
  }
  
  .upload-hint {
    font-size: 12px;
    color: $text-muted;
    margin-top: 4px;
  }
}

.remove-cover {
  width: fit-content;
}

.form-hint {
  font-size: 12px;
  color: $text-muted;
  margin-left: 12px;
}
</style>
