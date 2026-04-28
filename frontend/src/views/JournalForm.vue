<template>
  <div class="journal-form-page">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑日记' : '写日记' }}</h2>
          <el-button text @click="handleCancel">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      
      <el-form
        ref="journalFormRef"
        :model="journalForm"
        :rules="journalRules"
        label-width="80px"
        class="journal-form"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="日期" prop="date">
              <el-date-picker
                v-model="journalForm.date"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="journalForm.title" placeholder="请输入标题（可选）" size="large" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="地点" prop="locationName">
          <el-input v-model="journalForm.locationName" placeholder="请输入地点名称（可选）" size="large" />
        </el-form-item>
        
        <el-form-item label="坐标">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-input
                v-model.number="journalForm.latitude"
                type="number"
                placeholder="纬度"
                size="large"
              >
                <template #prefix>Lat</template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <el-input
                v-model.number="journalForm.longitude"
                type="number"
                placeholder="经度"
                size="large"
              >
                <template #prefix>Lng</template>
              </el-input>
            </el-col>
          </el-row>
          <span class="form-hint">可手动输入经纬度，或在地图页面标记地点</span>
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="journalForm.content"
            type="textarea"
            :rows="8"
            placeholder="记录今天的精彩时刻..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="照片">
          <div class="photo-upload-section">
            <div class="photo-grid">
              <div
                v-for="(photo, index) in journalForm.photos"
                :key="photo.id"
                class="photo-item"
              >
                <img :src="photo.thumbnailUrl || photo.url" :alt="photo.originalName" />
                <div class="photo-actions">
                  <el-button type="danger" circle size="small" @click="handleRemovePhoto(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <el-upload
                class="photo-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :multiple="true"
                :on-success="handlePhotoUploadSuccess"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept="image/*"
                :limit="20 - journalForm.photos.length"
              >
                <div class="upload-placeholder">
                  <el-icon :size="32"><Plus /></el-icon>
                  <span>添加照片</span>
                </div>
              </el-upload>
            </div>
            
            <div class="upload-hint">
              <el-icon><InfoFilled /></el-icon>
              支持批量上传，最多20张，单张最大50MB
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '保存日记' }}
          </el-button>
          <el-button size="large" @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJournalById, createJournal, updateJournal } from '@/api/journal'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const journalFormRef = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const uploadAction = '/api/files/upload-multiple'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const journalForm = reactive({
  date: null,
  title: '',
  content: '',
  locationName: '',
  latitude: null,
  longitude: null,
  photos: [],
  photoIds: []
})

const journalRules = {
  date: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  content: [
    { max: 5000, message: '内容不能超过5000个字符', trigger: 'blur' }
  ]
}

const loadJournalData = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const response = await getJournalById(route.params.tripId, route.params.id)
    const journal = response.data
    
    journalForm.date = journal.date
    journalForm.title = journal.title || ''
    journalForm.content = journal.content || ''
    journalForm.locationName = journal.locationName || ''
    journalForm.latitude = journal.latitude
    journalForm.longitude = journal.longitude
    journalForm.photos = journal.photos || []
    journalForm.photoIds = (journal.photos || []).map(p => p.id)
  } catch (error) {
    ElMessage.error('加载日记信息失败')
    router.push(`/trips/${route.params.tripId}`)
  } finally {
    loading.value = false
  }
}

const handlePhotoUploadSuccess = (response) => {
  if (response.success && response.data) {
    const newPhotos = Array.isArray(response.data) ? response.data : [response.data]
    journalForm.photos.push(...newPhotos)
    journalForm.photoIds.push(...newPhotos.map(p => p.id))
    ElMessage.success(`成功上传 ${newPhotos.length} 张照片`)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  console.error('Upload error:', error)
  ElMessage.error('上传失败')
}

const handleRemovePhoto = (index) => {
  journalForm.photos.splice(index, 1)
  journalForm.photoIds.splice(index, 1)
}

const handleSubmit = async () => {
  if (!journalFormRef.value) return
  
  await journalFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const formData = {
          date: journalForm.date,
          title: journalForm.title || undefined,
          content: journalForm.content || undefined,
          locationName: journalForm.locationName || undefined,
          latitude: journalForm.latitude,
          longitude: journalForm.longitude,
          photoIds: journalForm.photoIds.length > 0 ? journalForm.photoIds : undefined
        }
        
        if (isEdit.value) {
          await updateJournal(route.params.tripId, route.params.id, formData)
          ElMessage.success('保存成功')
        } else {
          await createJournal(route.params.tripId, formData)
          ElMessage.success('创建成功')
        }
        router.push(`/trips/${route.params.tripId}`)
      } catch (error) {
        ElMessage.error(isEdit.value ? '保存失败' : '创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleCancel = () => {
  router.push(`/trips/${route.params.tripId}`)
}

onMounted(() => {
  loadJournalData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.journal-form-page {
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

.journal-form {
  max-width: 700px;
}

.form-hint {
  font-size: 12px;
  color: $text-muted;
  margin-top: 8px;
}

.photo-upload-section {
  width: 100%;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: $radius-md;
  overflow: hidden;
  border: 1px solid $border-light;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .photo-actions {
    position: absolute;
    top: 4px;
    right: 4px;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  &:hover .photo-actions {
    opacity: 1;
  }
}

.photo-uploader {
  :deep(.el-upload) {
    width: 100%;
    height: 100%;
  }
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed $border-color;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: $text-secondary;
  gap: 8px;
  
  &:hover {
    border-color: $primary-color;
    background: rgba(59, 130, 246, 0.02);
  }
  
  span {
    font-size: 13px;
  }
}

.upload-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-muted;
}
</style>
