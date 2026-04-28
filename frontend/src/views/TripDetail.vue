<template>
  <div class="trip-detail-page">
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else-if="!trip" class="empty-wrapper">
      <el-empty description="旅程不存在" />
    </div>
    
    <div v-else class="detail-content">
      <div class="trip-header">
        <div class="header-back" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        
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
            <span class="meta-item">
              <el-tag :type="getStatusType(trip.status)" size="small">
                {{ getStatusText(trip.status) }}
              </el-tag>
            </span>
          </div>
          
          <div class="trip-tags" v-if="trip.tags && trip.tags.length > 0">
            <el-tag
              v-for="tag in trip.tags"
              :key="tag.id"
              size="small"
              :style="{ '--tag-color': tag.color || '#3b82f6' }"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
        
        <div class="header-actions">
          <el-button type="primary" size="large" @click="handleAddJournal">
            <el-icon><Edit /></el-icon>
            写日记
          </el-button>
          <el-button size="large" @click="handleEdit">
            <el-icon><Setting /></el-icon>
            编辑
          </el-button>
          <el-button size="large" @click="handleReview">
            <el-icon><Film /></el-icon>
            回顾
          </el-button>
          <el-button size="large" @click="handleShare">
            <el-icon><Share /></el-icon>
            {{ trip.isPublic ? '分享链接' : '生成分享' }}
          </el-button>
        </div>
      </div>
      
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="日记" name="journals">
          <div class="journals-section">
            <div v-if="journalsLoading" class="loading-small">
              <el-icon class="loading-icon"><Loading /></el-icon>
            </div>
            
            <div v-else-if="journals.length === 0" class="empty-journals">
              <el-empty description="暂无日记">
                <el-button type="primary" @click="handleAddJournal">
                  <el-icon><Plus /></el-icon>
                  写第一篇日记
                </el-button>
              </el-empty>
            </div>
            
            <div v-else class="journals-timeline">
              <div
                v-for="journal in journals"
                :key="journal.id"
                class="journal-item"
                @click="handleViewJournal(journal)"
              >
                <div class="journal-date">
                  <span class="date-day">{{ formatDay(journal.date) }}</span>
                  <span class="date-month">{{ formatMonth(journal.date) }}</span>
                </div>
                
                <div class="journal-content">
                  <h3 class="journal-title">{{ journal.title || '无标题' }}</h3>
                  <p v-if="journal.content" class="journal-text">{{ journal.content }}</p>
                  
                  <div class="journal-location" v-if="journal.locationName">
                    <el-icon><Location /></el-icon>
                    {{ journal.locationName }}
                  </div>
                  
                  <div class="journal-photos" v-if="journal.photos && journal.photos.length > 0">
                    <div
                      v-for="(photo, index) in journal.photos.slice(0, 5)"
                      :key="photo.id"
                      class="photo-thumb"
                      :style="{ backgroundImage: `url(${photo.thumbnailUrl || photo.url})` }"
                    >
                      <span v-if="index === 4 && journal.photos.length > 5" class="photo-count">
                        +{{ journal.photos.length - 4 }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="journal-actions" @click.stop>
                    <el-button type="primary" text size="small" @click="handleEditJournal(journal)">
                      编辑
                    </el-button>
                    <el-button type="danger" text size="small" @click="handleDeleteJournal(journal)">
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="地图" name="map">
          <div class="map-section">
            <div class="map-header">
              <h3>行程轨迹</h3>
              <el-button type="primary" size="small" @click="handleAddLocation">
                <el-icon><Plus /></el-icon>
                添加地点
              </el-button>
            </div>
            
            <div v-if="locations.length === 0" class="empty-map">
              <el-empty description="暂无标记地点">
                <el-button type="primary" @click="handleAddLocation">
                  <el-icon><Plus /></el-icon>
                  标记第一个地点
                </el-button>
              </el-empty>
            </div>
            
            <div v-else class="map-container">
              <LMap
                :zoom="mapZoom"
                :center="mapCenter"
                style="width: 100%; height: 100%"
              >
                <LTileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                />
                
                <LPolyline
                  v-if="mapPolylines.length > 0"
                  :lat-lngs="mapPolylines"
                  :color="'#3b82f6'"
                  :weight="4"
                  :opacity="0.8"
                />
                
                <LMarker
                  v-for="(location, index) in locations"
                  :key="location.id"
                  :lat-lng="[location.latitude, location.longitude]"
                >
                  <LPopup>
                    <div class="marker-popup">
                      <h4>{{ index + 1 }}. {{ location.name }}</h4>
                      <p v-if="location.description">{{ location.description }}</p>
                      <p v-if="location.visitDate" class="popup-date">
                        {{ formatDate(location.visitDate) }}
                      </p>
                      <div class="popup-actions">
                        <el-button type="primary" text size="small" @click="handleEditLocation(location)">
                          编辑
                        </el-button>
                        <el-button type="danger" text size="small" @click="handleDeleteLocation(location)">
                          删除
                        </el-button>
                      </div>
                    </div>
                  </LPopup>
                </LMarker>
              </LMap>
            </div>
            
            <div class="locations-list" v-if="locations.length > 0">
              <h4>地点列表</h4>
              <div
                v-for="(location, index) in locations"
                :key="location.id"
                class="location-item"
              >
                <div class="location-order">{{ index + 1 }}</div>
                <div class="location-info">
                  <h5>{{ location.name }}</h5>
                  <p v-if="location.visitDate">{{ formatDate(location.visitDate) }}</p>
                  <p v-if="location.description" class="location-desc">{{ location.description }}</p>
                </div>
                <div class="location-actions">
                  <el-button type="primary" text size="small" @click="handleEditLocation(location)">
                    编辑
                  </el-button>
                  <el-button type="danger" text size="small" @click="handleDeleteLocation(location)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="分享" name="share">
          <div class="share-section">
            <h3>分享设置</h3>
            
            <div class="share-status">
              <span>当前状态：</span>
              <el-tag :type="trip.isPublic ? 'success' : 'info'">
                {{ trip.isPublic ? '已公开分享' : '私密' }}
              </el-tag>
            </div>
            
            <div v-if="trip.isPublic && trip.shareToken" class="share-link">
              <el-input
                :value="shareUrl"
                readonly
                style="margin-bottom: 12px"
              >
                <template #append>
                  <el-button @click="handleCopyLink">复制链接</el-button>
                </template>
              </el-input>
              <el-button type="danger" @click="handleRevokeShare">取消分享</el-button>
            </div>
            
            <div v-else class="share-actions">
              <el-button type="primary" size="large" @click="handleGenerateShare">
                <el-icon><Share /></el-icon>
                生成分享链接
              </el-button>
              <p class="share-hint">生成分享链接后，其他用户可通过该链接查看您的旅程</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { LMap, LTileLayer, LMarker, LPopup, LPolyline } from '@vue-leaflet/vue-leaflet'
import { getTripById, generateShareToken, revokeShareToken } from '@/api/trip'
import { getJournals, deleteJournal } from '@/api/journal'
import { getLocations, deleteLocation } from '@/api/location'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const journalsLoading = ref(true)
const trip = ref(null)
const journals = ref([])
const locations = ref([])
const activeTab = ref('journals')

const mapCenter = ref([39.9042, 116.4074])
const mapZoom = ref(8)

const shareUrl = computed(() => {
  if (trip.value?.shareToken) {
    return `${window.location.origin}/shared/${trip.value.shareToken}`
  }
  return ''
})

const mapPolylines = computed(() => {
  if (locations.value.length < 2) return []
  return locations.value.map(loc => [loc.latitude, loc.longitude])
})

const loadTripData = async () => {
  try {
    const response = await getTripById(route.params.id)
    trip.value = response.data
    
    if (locations.value.length > 0) {
      const firstLoc = locations.value[0]
      mapCenter.value = [firstLoc.latitude, firstLoc.longitude]
    }
  } catch (error) {
    ElMessage.error('加载旅程信息失败')
  } finally {
    loading.value = false
  }
}

const loadJournals = async () => {
  try {
    journalsLoading.value = true
    const response = await getJournals(route.params.id)
    journals.value = response.data || []
  } catch (error) {
    console.error('Failed to load journals:', error)
  } finally {
    journalsLoading.value = false
  }
}

const loadLocations = async () => {
  try {
    const response = await getLocations(route.params.id)
    locations.value = response.data || []
    
    if (locations.value.length > 0) {
      const firstLoc = locations.value[0]
      mapCenter.value = [firstLoc.latitude, firstLoc.longitude]
    }
  } catch (error) {
    console.error('Failed to load locations:', error)
  }
}

const handleBack = () => {
  router.push('/trips')
}

const handleAddJournal = () => {
  router.push(`/trips/${route.params.id}/journals/create`)
}

const handleViewJournal = (journal) => {
  router.push(`/trips/${route.params.id}/journals/${journal.id}/edit`)
}

const handleEditJournal = (journal) => {
  router.push(`/trips/${route.params.id}/journals/${journal.id}/edit`)
}

const handleDeleteJournal = async (journal) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这篇日记吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await deleteJournal(route.params.id, journal.id)
    journals.value = journals.value.filter(j => j.id !== journal.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAddLocation = () => {
  ElMessage.info('地点管理功能开发中')
}

const handleEditLocation = (location) => {
  ElMessage.info('编辑地点功能开发中')
}

const handleDeleteLocation = async (location) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除地点「${location.name}」吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await deleteLocation(route.params.id, location.id)
    locations.value = locations.value.filter(l => l.id !== location.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleEdit = () => {
  router.push(`/trips/${route.params.id}/edit`)
}

const handleReview = () => {
  router.push(`/trips/${route.params.id}/review`)
}

const handleShare = () => {
  activeTab.value = 'share'
}

const handleGenerateShare = async () => {
  try {
    await generateShareToken(route.params.id)
    await loadTripData()
    ElMessage.success('分享链接已生成')
  } catch (error) {
    ElMessage.error('生成分享链接失败')
  }
}

const handleRevokeShare = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消分享吗？取消后其他用户将无法访问此旅程。',
      '确认取消',
      { type: 'warning' }
    )
    
    await revokeShareToken(route.params.id)
    await loadTripData()
    ElMessage.success('已取消分享')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCopyLink = () => {
  navigator.clipboard.writeText(shareUrl.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info(`分享链接: ${shareUrl.value}`)
  })
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

const getStatusType = (status) => {
  const statusMap = {
    PLANNING: 'info',
    ONGOING: 'success',
    COMPLETED: 'primary',
    ARCHIVED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PLANNING: '规划中',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    ARCHIVED: '已归档'
  }
  return statusMap[status] || status
}

watch(activeTab, (newVal) => {
  if (newVal === 'journals') {
    loadJournals()
  } else if (newVal === 'map') {
    loadLocations()
  }
})

onMounted(() => {
  loadTripData()
  loadJournals()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.trip-detail-page {
  width: 100%;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: $text-secondary;
  
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

.empty-wrapper {
  padding: 60px 20px;
}

.loading-small {
  display: flex;
  justify-content: center;
  padding: 40px;
  
  .loading-icon {
    font-size: 24px;
    color: $text-muted;
    animation: spin 1s linear infinite;
  }
}

.detail-content {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 24px;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;
}

.trip-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid $border-light;
}

.header-back {
  display: flex;
  align-items: center;
  gap: 4px;
  color: $text-secondary;
  cursor: pointer;
  margin-bottom: 16px;
  font-size: 14px;
  
  &:hover {
    color: $primary-color;
  }
}

.header-info {
  flex: 1;
}

.trip-title {
  font-size: 28px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 12px;
}

.trip-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 12px;
  
  .meta-item {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: $text-secondary;
    gap: 6px;
  }
}

.trip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .el-tag {
    background-color: var(--tag-color);
    border-color: var(--tag-color);
    color: #fff;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.detail-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
}

.journals-section {
  min-height: 300px;
}

.empty-journals {
  padding: 40px;
}

.journals-timeline {
  position: relative;
  padding-left: 32px;
  
  &::before {
    content: '';
    position: absolute;
    left: 8px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: $border-color;
  }
}

.journal-item {
  position: relative;
  display: flex;
  gap: 20px;
  margin-bottom: 32px;
  cursor: pointer;
  padding: 16px;
  border-radius: $radius-md;
  transition: background 0.2s;
  
  &:hover {
    background: $bg-gray;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: -30px;
    top: 24px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: $primary-color;
    border: 2px solid $bg-white;
    box-shadow: 0 0 0 2px $primary-color;
  }
}

.journal-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
  padding: 12px;
  background: $bg-gray;
  border-radius: $radius-md;
  
  .date-day {
    font-size: 28px;
    font-weight: 700;
    color: $primary-color;
  }
  
  .date-month {
    font-size: 12px;
    color: $text-secondary;
  }
}

.journal-content {
  flex: 1;
}

.journal-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
}

.journal-text {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.journal-location {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: $text-muted;
  gap: 4px;
  margin-bottom: 12px;
}

.journal-photos {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  
  .photo-thumb {
    width: 80px;
    height: 80px;
    border-radius: $radius-sm;
    background-size: cover;
    background-position: center;
    position: relative;
    
    .photo-count {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-weight: 600;
      border-radius: $radius-sm;
    }
  }
}

.journal-actions {
  display: flex;
  gap: 12px;
}

.map-section {
  min-height: 500px;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
}

.empty-map {
  padding: 40px;
}

.map-container {
  height: 500px;
  border-radius: $radius-md;
  overflow: hidden;
  margin-bottom: 24px;
}

.locations-list {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.location-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: $bg-gray;
  border-radius: $radius-md;
  margin-bottom: 12px;
}

.location-order {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-color;
  color: #fff;
  border-radius: 50%;
  font-weight: 600;
  font-size: 14px;
}

.location-info {
  flex: 1;
  
  h5 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  p {
    font-size: 13px;
    color: $text-secondary;
    margin: 0;
  }
  
  .location-desc {
    color: $text-muted;
    margin-top: 4px;
  }
}

.location-actions {
  display: flex;
  gap: 8px;
}

.share-section {
  max-width: 600px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
  }
}

.share-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.share-link {
  margin-bottom: 24px;
}

.share-actions {
  .share-hint {
    margin-top: 12px;
    font-size: 13px;
    color: $text-muted;
  }
}

.marker-popup {
  min-width: 200px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 12px;
    color: $text-secondary;
    margin: 0 0 4px;
  }
  
  .popup-date {
    color: $text-muted;
    font-size: 11px;
  }
  
  .popup-actions {
    display: flex;
    gap: 12px;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid $border-light;
  }
}
</style>
