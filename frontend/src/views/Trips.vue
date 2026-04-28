<template>
  <div class="trips-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">我的旅程</h1>
        <p class="page-desc">记录您的每一次美好旅程</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索旅程..."
          prefix-icon="Search"
          clearable
          style="width: 240px; margin-right: 16px"
        />
        <el-button type="primary" size="large" @click="handleCreateTrip">
          <el-icon><Plus /></el-icon>
          创建旅程
        </el-button>
      </div>
    </div>
    
    <div class="filter-section" v-if="tags.length > 0">
      <span class="filter-label">标签筛选：</span>
      <el-tag
        v-for="tag in tags"
        :key="tag.id"
        :type="selectedTags.includes(tag.id) ? '' : 'info'"
        :effect="selectedTags.includes(tag.id) ? 'dark' : 'plain'"
        class="tag-item"
        @click="toggleTag(tag.id)"
      >
        {{ tag.name }}
      </el-tag>
      <el-button v-if="selectedTags.length > 0" type="text" size="small" @click="clearFilter">
        清除筛选
      </el-button>
    </div>
    
    <div class="trips-content">
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="filteredTrips.length === 0" class="empty-wrapper">
        <el-empty description="暂无旅程">
          <el-button type="primary" @click="handleCreateTrip">
            <el-icon><Plus /></el-icon>
            创建第一个旅程
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="trips-grid">
        <div
          v-for="trip in filteredTrips"
          :key="trip.id"
          class="trip-card"
          @click="handleViewTrip(trip.id)"
        >
          <div class="trip-cover">
            <img v-if="trip.coverImage" :src="trip.coverImage" :alt="trip.title" />
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <div class="trip-status">
              <el-tag :type="getStatusType(trip.status)" size="small">
                {{ getStatusText(trip.status) }}
              </el-tag>
            </div>
            <div class="trip-actions" @click.stop>
              <el-dropdown trigger="click" placement="bottom-end">
                <el-button type="text" circle size="small">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEditTrip(trip.id)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleShare(trip)" v-if="trip.isPublic">
                      <el-icon><Share /></el-icon>
                      分享链接
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleGenerateShare(trip)" v-else>
                      <el-icon><Share /></el-icon>
                      生成分享链接
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleDelete(trip)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="trip-info">
            <h3 class="trip-title">{{ trip.title }}</h3>
            <p v-if="trip.description" class="trip-desc">{{ trip.description }}</p>
            
            <div class="trip-meta">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(trip.startDate) }} - {{ formatDate(trip.endDate) }}</span>
              </div>
              <div v-if="trip.destination" class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ trip.destination }}</span>
              </div>
            </div>
            
            <div class="trip-stats" v-if="trip.journalEntryCount > 0 || trip.photoCount > 0">
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                {{ trip.journalEntryCount }} 篇日记
              </span>
              <span class="stat-item">
                <el-icon><Picture /></el-icon>
                {{ trip.photoCount }} 张照片
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTrips } from '@/api/trip'
import { getTags } from '@/api/tag'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const trips = ref([])
const tags = ref([])
const searchKeyword = ref('')
const selectedTags = ref([])

const filteredTrips = computed(() => {
  let result = [...trips.value]
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(trip =>
      trip.title.toLowerCase().includes(keyword) ||
      (trip.description && trip.description.toLowerCase().includes(keyword)) ||
      (trip.destination && trip.destination.toLowerCase().includes(keyword))
    )
  }
  
  if (selectedTags.value.length > 0) {
    result = result.filter(trip =>
      trip.tags && trip.tags.some(tag => selectedTags.value.includes(tag.id))
    )
  }
  
  return result
})

const loadTrips = async () => {
  loading.value = true
  try {
    const response = await getTrips()
    trips.value = response.data || []
  } catch (error) {
    console.error('Failed to load trips:', error)
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const response = await getTags()
    tags.value = response.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

const handleCreateTrip = () => {
  router.push('/trips/create')
}

const handleViewTrip = (id) => {
  router.push(`/trips/${id}`)
}

const handleEditTrip = (id) => {
  router.push(`/trips/${id}/edit`)
}

const handleDelete = async (trip) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除旅程「${trip.title}」吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    trips.value = trips.value.filter(t => t.id !== trip.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleGenerateShare = async (trip) => {
  try {
    ElMessage.success('分享链接已生成')
    await loadTrips()
  } catch (error) {
    ElMessage.error('生成分享链接失败')
  }
}

const handleShare = (trip) => {
  const shareUrl = `${window.location.origin}/shared/${trip.shareToken}`
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info(`分享链接: ${shareUrl}`)
  })
}

const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
}

const clearFilter = () => {
  selectedTags.value = []
  searchKeyword.value = ''
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
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

onMounted(() => {
  loadTrips()
  loadTags()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.trips-page {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-left {
  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 4px;
  }
  
  .page-desc {
    font-size: 14px;
    color: $text-secondary;
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: $bg-white;
  border-radius: $radius-lg;
  border: 1px solid $border-light;
  
  .filter-label {
    font-size: 14px;
    color: $text-secondary;
    margin-right: 12px;
  }
  
  .tag-item {
    cursor: pointer;
    margin-right: 8px;
    transition: all 0.2s;
  }
}

.trips-content {
  min-height: 400px;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
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

.trips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.trip-card {
  background: $bg-white;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
}

.trip-cover {
  position: relative;
  height: 180px;
  background: $bg-gray;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .cover-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: $text-muted;
  }
}

.trip-status {
  position: absolute;
  top: 12px;
  left: 12px;
}

.trip-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  
  .el-button {
    background: rgba(0, 0, 0, 0.3);
    color: #fff;
    
    &:hover {
      background: rgba(0, 0, 0, 0.5);
    }
  }
}

.trip-info {
  padding: 20px;
}

.trip-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trip-desc {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.trip-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
  
  .meta-item {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: $text-muted;
    gap: 6px;
    
    el-icon {
      font-size: 14px;
    }
  }
}

.trip-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding-top: 12px;
  border-top: 1px solid $border-light;
  
  .stat-item {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: $text-secondary;
    gap: 4px;
  }
}

.trip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  
  .el-tag {
    background-color: var(--tag-color);
    border-color: var(--tag-color);
    color: #fff;
  }
}
</style>
