<template>
  <div class="shared-trip-page">
    <div class="page-header">
      <router-link to="/login" v-if="!isAuthenticated" class="login-link">
        <el-icon><User /></el-icon>
        登录
      </router-link>
    </div>
    
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else-if="!trip" class="error-wrapper">
      <el-icon class="error-icon"><Warning /></el-icon>
      <h2>分享链接无效或已过期</h2>
      <p>该分享链接可能已被撤销或不存在</p>
      <router-link to="/login" class="back-link">前往登录</router-link>
    </div>
    
    <div v-else class="content-wrapper">
      <div class="trip-header">
        <div class="header-info">
          <h1 class="trip-title">{{ trip.title }}</h1>
          <div class="trip-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(trip.startDate) }} - {{ formatDate(trip.endDate) }}
            </span>
            <span v-if="trip.destination" class="meta-item">
              <el-icon><Location /></el-icon>
              {{ trip.destination }}
            </span>
          </div>
          
          <p v-if="trip.description" class="trip-description">{{ trip.description }}</p>
          
          <div class="trip-stats">
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              {{ trip.journalEntryCount || 0 }} 篇日记
            </span>
            <span class="stat-item">
              <el-icon><Picture /></el-icon>
              {{ trip.photoCount || 0 }} 张照片
            </span>
          </div>
        </div>
        
        <div v-if="trip.coverImage" class="header-cover">
          <img :src="trip.coverImage" :alt="trip.title" />
        </div>
      </div>
      
      <div class="journals-section" v-if="journals.length > 0">
        <h2 class="section-title">旅行日记</h2>
        
        <div class="journals-list">
          <div
            v-for="journal in journals"
            :key="journal.id"
            class="journal-card"
          >
            <div class="journal-date">
              <span class="date-day">{{ formatDay(journal.date) }}</span>
              <span class="date-month">{{ formatMonth(journal.date) }}</span>
            </div>
            
            <div class="journal-content">
              <h3 v-if="journal.title" class="journal-title">{{ journal.title }}</h3>
              
              <div v-if="journal.photos && journal.photos.length > 0" class="journal-photos">
                <div
                  v-for="photo in journal.photos.slice(0, 5)"
                  :key="photo.id"
                  class="photo-thumb"
                  :style="{ backgroundImage: `url(${photo.thumbnailUrl || photo.url})` }"
                  @click="handleViewPhoto(photo)"
                />
                <div
                  v-if="journal.photos.length > 5"
                  class="photo-thumb more"
                >
                  +{{ journal.photos.length - 5 }}
                </div>
              </div>
              
              <p v-if="journal.content" class="journal-text">{{ journal.content }}</p>
              
              <div v-if="journal.locationName" class="journal-location">
                <el-icon><Location /></el-icon>
                {{ journal.locationName }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-journals">
        <el-empty description="暂无公开的日记" />
      </div>
    </div>
    
    <el-dialog v-model="photoViewerVisible" title="查看照片" width="80%" fullscreen>
      <img v-if="currentPhoto" :src="currentPhoto.url" style="width: 100%; object-fit: contain;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSharedTrip } from '@/api/trip'
import { getJournals } from '@/api/journal'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const trip = ref(null)
const journals = ref([])
const photoViewerVisible = ref(false)
const currentPhoto = ref(null)

const isAuthenticated = computed(() => userStore.isAuthenticated)

const loadData = async () => {
  try {
    loading.value = true
    const token = route.params.token
    
    const tripResponse = await getSharedTrip(token)
    trip.value = tripResponse.data
    
    const journalsResponse = await getJournals(trip.value.id)
    journals.value = (journalsResponse.data || []).filter(j => j.photos?.length > 0 || j.content)
    
    document.title = `${trip.value.title} - 旅行日记分享`
  } catch (error) {
    console.error('Failed to load shared trip:', error)
    trip.value = null
  } finally {
    loading.value = false
  }
}

const handleViewPhoto = (photo) => {
  currentPhoto.value = photo
  photoViewerVisible.value = true
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
  return dayjs(date).format('MM月')
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.shared-trip-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.login-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: $primary-color;
  font-weight: 500;
  font-size: 14px;
  
  &:hover {
    color: $primary-dark;
  }
}

.loading-wrapper,
.error-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: $text-secondary;
  
  .loading-icon,
  .error-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  h2 {
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  p {
    margin-bottom: 24px;
  }
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.back-link {
  color: $primary-color;
  font-weight: 500;
  
  &:hover {
    color: $primary-dark;
  }
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.trip-header {
  display: flex;
  gap: 32px;
  background: $bg-white;
  border-radius: $radius-xl;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: $shadow-md;
}

.header-info {
  flex: 1;
}

.trip-title {
  font-size: 28px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 16px;
}

.trip-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  
  .meta-item {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: $text-secondary;
    gap: 6px;
  }
}

.trip-description {
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 16px;
}

.trip-stats {
  display: flex;
  gap: 24px;
  
  .stat-item {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: $primary-color;
    font-weight: 500;
    gap: 6px;
    padding: 8px 16px;
    background: rgba(59, 130, 246, 0.1);
    border-radius: $radius-full;
  }
}

.header-cover {
  width: 300px;
  height: 200px;
  border-radius: $radius-lg;
  overflow: hidden;
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.journals-section {
  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 4px solid $primary-color;
  }
}

.journals-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.journal-card {
  display: flex;
  gap: 20px;
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 24px;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;
}

.journal-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
  padding: 16px 12px;
  background: $bg-gray;
  border-radius: $radius-md;
  text-align: center;
  
  .date-day {
    font-size: 28px;
    font-weight: 700;
    color: $primary-color;
    line-height: 1;
  }
  
  .date-month {
    font-size: 12px;
    color: $text-secondary;
    margin-top: 4px;
  }
}

.journal-content {
  flex: 1;
}

.journal-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 12px;
}

.journal-photos {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  
  .photo-thumb {
    width: 100px;
    height: 100px;
    border-radius: $radius-sm;
    background-size: cover;
    background-position: center;
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.02);
    }
    
    &.more {
      display: flex;
      align-items: center;
      justify-content: center;
      background: $bg-gray;
      color: $text-secondary;
      font-weight: 600;
      font-size: 16px;
    }
  }
}

.journal-text {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.journal-location {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: $text-muted;
  gap: 4px;
}

.empty-journals {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 40px;
  text-align: center;
  box-shadow: $shadow-sm;
}

@media (max-width: 768px) {
  .trip-header {
    flex-direction: column-reverse;
  }
  
  .header-cover {
    width: 100%;
    height: 200px;
  }
  
  .journal-card {
    flex-direction: column;
  }
  
  .journal-date {
    flex-direction: row;
    justify-content: flex-start;
    gap: 8px;
    min-width: auto;
    padding: 8px 12px;
  }
}
</style>
