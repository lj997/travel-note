<template>
  <div class="tags-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">标签管理</h1>
        <p class="page-desc">管理您的旅程标签，方便分类检索</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="handleCreateTag">
          <el-icon><Plus /></el-icon>
          新建标签
        </el-button>
      </div>
    </div>
    
    <div class="tags-content">
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="tags.length === 0" class="empty-wrapper">
        <el-empty description="暂无标签">
          <el-button type="primary" @click="handleCreateTag">
            <el-icon><Plus /></el-icon>
            创建第一个标签
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="tags-grid">
        <div
          v-for="tag in tags"
          :key="tag.id"
          class="tag-card"
          :style="{ '--tag-color': tag.color || '#3b82f6' }"
        >
          <div class="tag-color" :style="{ backgroundColor: tag.color || '#3b82f6' }" />
          
          <div class="tag-info">
            <h3 class="tag-name">{{ tag.name }}</h3>
            <p class="tag-stats">
              <el-icon><Collection /></el-icon>
              {{ tag.usageCount || 0 }} 个旅程
            </p>
          </div>
          
          <div class="tag-actions">
            <el-dropdown trigger="click" placement="bottom-end">
              <el-button type="primary" text circle size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEditTag(tag)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleDeleteTag(tag)" v-if="!tag.usageCount">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                  <el-dropdown-item divided disabled v-else>
                    <el-icon><InfoFilled /></el-icon>
                    已使用无法删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '新建标签'"
      width="400px"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagRules"
        label-width="80px"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" size="large" />
        </el-form-item>
        
        <el-form-item label="标签颜色" prop="color">
          <div class="color-picker">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-option"
              :class="{ active: tagForm.color === color }"
              :style="{ backgroundColor: color }"
              @click="tagForm.color = color"
            />
            <el-color-picker
              v-model="tagForm.color"
              :predefine="presetColors"
              size="large"
            />
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmitTag">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTags, createTag, updateTag } from '@/api/tag'

const loading = ref(false)
const tags = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const tagFormRef = ref(null)
const editingTag = ref(null)

const presetColors = [
  '#3b82f6', '#10b981', '#f59e0b', '#ef4444',
  '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
]

const tagForm = reactive({
  name: '',
  color: '#3b82f6'
})

const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 50, message: '标签名称不能超过50个字符', trigger: 'blur' }
  ]
}

const loadTags = async () => {
  try {
    loading.value = true
    const response = await getTags()
    tags.value = response.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreateTag = () => {
  isEdit.value = false
  editingTag.value = null
  tagForm.name = ''
  tagForm.color = '#3b82f6'
  dialogVisible.value = true
}

const handleEditTag = (tag) => {
  isEdit.value = true
  editingTag.value = tag
  tagForm.name = tag.name
  tagForm.color = tag.color || '#3b82f6'
  dialogVisible.value = true
}

const handleDeleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签「${tag.name}」吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    tags.value = tags.value.filter(t => t.id !== tag.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmitTag = async () => {
  if (!tagFormRef.value) return
  
  await tagFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      
      try {
        if (isEdit.value && editingTag.value) {
          await updateTag(editingTag.value.id, {
            name: tagForm.name,
            color: tagForm.color
          })
          ElMessage.success('更新成功')
        } else {
          const response = await createTag({
            name: tagForm.name,
            color: tagForm.color
          })
          tags.value.push(response.data)
          ElMessage.success('创建成功')
        }
        
        dialogVisible.value = false
        await loadTags()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        saving.value = false
      }
    }
  })
}

onMounted(() => {
  loadTags()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.tags-page {
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

.tags-content {
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
  padding: 40px;
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.tag-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 20px;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;
  border-left: 4px solid var(--tag-color);
  transition: all 0.3s;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
}

.tag-color {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tag-info {
  flex: 1;
  
  .tag-name {
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
    margin: 0 0 4px;
  }
  
  .tag-stats {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: $text-secondary;
    margin: 0;
  }
}

.tag-actions {
  flex-shrink: 0;
}

.color-picker {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  
  .color-option {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    
    &:hover {
      transform: scale(1.1);
    }
    
    &.active {
      border-color: $text-primary;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
    }
  }
}
</style>
