<template>
  <div class="trip-review-page">
    <div class="review-header">
      <div class="header-back" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      
      <div class="header-title">
        <h1>{{ trip?.title }}</h1>
        <p>{{ formatDate(trip?.startDate) }} - {{ formatDate(trip?.endDate) }}</p>
      </div>
      
      <div class="header-actions">
        <el-button type="primary" @click="handleExportPDF">
          <el-icon><Download /></el-icon>
          导出PDF
        </el-button>
        <el-button @click="handleFullscreen">
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </div>
    </div>
    
    <div class="review-content" ref="reviewContentRef">
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else class="timeline-wrapper">
        <div v-if="journals.length === 0" class="empty-journals">
          <el-empty description="暂无日记可回顾" />
        </div>
        
        <div v-else class="journals-timeline">
          <div
            v-for="journal in journals"
            :key="journal.id"
            class="journal-section"
            ref="journalSections"
          >
            <div class="journal-date">
              <span class="date-day">{{ formatDay(journal.date) }}</span>
              <span class="date-month">{{ formatMonth(journal.date) }}</span>
            </div>
            
            <div class="journal-card">
              <h2 v-if="journal.title" class="journal-title">{{ journal.title }}</h2>
              
              <div v-if="journal.photos && journal.photos.length > 0" class="photo-gallery">
                <div
                  v-for="photo in journal.photos"
                  :key="photo.id"
                  class="gallery-item"
                  @click="handleViewPhoto(photo)"
                >
                  <img :src="photo.url" :alt="photo.originalName" />
                  <div class="photo-overlay">
                    <el-icon><ZoomIn /></el-icon>
                  </div>
                </div>
              </div>
              
              <div v-if="journal.content" class="journal-content">
                <p>{{ journal.content }}</p>
              </div>
              
              <div v-if="journal.locationName" class="journal-location">
                <el-icon><Location /></el-icon>
                <span>{{ journal.locationName }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="review-progress" v-if="journals.length > 1">
      <el-progress :percentage="progressPercentage" :stroke-width="2" />
    </div>
    
    <el-dialog v-model="photoViewerVisible" title="查看照片" width="80%" fullscreen>
      <img v-if="currentPhoto" :src="currentPhoto.url" style="width: 100%; object-fit: contain;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTripById } from '@/api/trip'
import { getJournals } from '@/api/journal'
import dayjs from 'dayjs'
import html2pdf from 'html2pdf.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const trip = ref(null)
const journals = ref([])
const reviewContentRef = ref(null)
const journalSections = ref([])
const currentSectionIndex = ref(0)
const photoViewerVisible = ref(false)
const currentPhoto = ref(null)

const progressPercentage = computed(() => {
  if (journals.value.length <= 1) return 100
  return Math.round((currentSectionIndex.value / (journals.value.length - 1)) * 100)
})

const loadData = async () => {
  try {
    loading.value = true
    
    const tripResponse = await getTripById(route.params.id)
    trip.value = tripResponse.data
    
    const journalsResponse = await getJournals(route.params.id)
    journals.value = (journalsResponse.data || []).filter(j => j.photos?.length > 0 || j.content)
    
    document.title = `回顾：${trip.value.title} - 旅行日记`
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push(`/trips/${route.params.id}`)
}

const handleFullscreen = () => {
  const elem = document.documentElement
  if (!document.fullscreenElement) {
    elem.requestFullscreen().catch(err => {
      ElMessage.error('无法进入全屏模式')
    })
  } else {
    document.exitFullscreen()
  }
}

const handleViewPhoto = (photo) => {
  currentPhoto.value = photo
  photoViewerVisible.value = true
}

const handleExportPDF = async () => {
  if (!reviewContentRef.value) return
  
  ElMessage.info('正在生成PDF，请稍候...')
  
  try {
    const opt = {
      margin: 10,
      filename: `${trip.value.title || '旅行日记'}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    }
    
    const content = reviewContentRef.value
    await html2pdf().set(opt).from(content).save()
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF export error:', error)
    ElMessage.error('PDF导出失败')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDay = (date) => {
  if (!date) return ''
  return dayjs(date).format('DD')
}

const formatMonth = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY年MM月')
}

const handleScroll = () => {
  if (!journalSections.value || journalSections.value.length === 0) return
  
  const scrollTop = window.pageYOffset
  const windowHeight = window.innerHeight
  
  for (let i = 0; i < journalSections.value.length; i++) {
    const section = journalSections.value[i]
    const rect = section.getBoundingClientRect()
    
    if (rect.top <= windowHeight / 2 && rect.bottom >= windowHeight / 2) {
      currentSectionIndex.value = i
      break
    }
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.trip-review-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  padding-bottom: 60px;
}

.review-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-back {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
  
  &:hover {
    color: #fff;
  }
}

.header-title {
  text-align: center;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 4px;
  }
  
  p {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
    margin: 0;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.review-content {
  padding-top: 100px;
  max-width: 900px;
  margin: 0 auto;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: rgba(255, 255, 255, 0.7);
  
  .loading-icon {
    font-size: 32px;
    margin-bottom: 12px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-journals {
  padding: 60px 20px;
  
  :deep(.el-empty__description) {
    color: rgba(255, 255, 255, 0.6);
  }
}

.journals-timeline {
  position: relative;
  padding: 0 40px;
  
  &::before {
    content: '';
    position: absolute;
    left: 28px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: linear-gradient(180deg, rgba(59, 130, 246, 0.8) 0%, rgba(59, 130, 246, 0.2) 100%);
  }
}

.journal-section {
  position: relative;
  display: flex;
  gap: 24px;
  margin-bottom: 48px;
  padding-top: 24px;
  
  &::before {
    content: '';
    position: absolute;
    left: -52px;
    top: 36px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: $primary-color;
    border: 3px solid #1a1a2e;
    box-shadow: 0 0 0 2px $primary-color;
  }
}

.journal-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
  padding: 16px 12px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: $radius-lg;
  border: 1px solid rgba(59, 130, 246, 0.2);
  
  .date-day {
    font-size: 32px;
    font-weight: 700;
    color: $primary-color;
    line-height: 1;
  }
  
  .date-month {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 4px;
  }
}

.journal-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border-radius: $radius-lg;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.journal-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #fff;
}

.photo-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.gallery-item {
  position: relative;
  aspect-ratio: 4/3;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }
  
  .photo-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.3s;
    color: #fff;
    font-size: 24px;
  }
  
  &:hover {
    img {
      transform: scale(1.05);
    }
    
    .photo-overlay {
      opacity: 1;
    }
  }
}

.journal-content {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 16px;
  
  p {
    margin: 0;
  }
}

.journal-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.review-progress {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(26, 26, 46, 0.95);
  padding: 12px 32px;
  
  :deep(.el-progress-bar__outer) {
    background-color: rgba(255, 255, 255, 0.1);
  }
}
</style>
