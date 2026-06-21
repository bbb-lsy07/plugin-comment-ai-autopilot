<template>
  <div class="settings-container">
    <VPageHeader title="插件设置">
      <template #icon><IconPlug class="header-icon" /></template>
      <template #actions>
        <VSpace spacing="sm">
          <VButton size="sm" @click="exportConfig">
            <template #icon>
              <svg style="width:14px;height:14px" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
            </template>
            导出
          </VButton>
          <label class="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 cursor-pointer transition-colors">
            <svg style="width:14px;height:14px;margin-right:4px" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
            导入
            <input type="file" accept=".json" class="hidden" @change="handleImportFile" />
          </label>
          <VButton size="sm" @click="$router.push({ name: 'CommentAiAutopilot' })">返回概览</VButton>
        </VSpace>
      </template>
    </VPageHeader>

    <div class="settings-main">
      <VLoading v-if="loading" />

      <div v-else class="settings-layout">
        <!-- 左侧主体 -->
        <div class="settings-content">
          <!-- 标签导航 -->
          <div class="tabs-wrap">
            <button v-for="tab in tabItems" :key="tab.value" 
              class="tab-btn" :class="{ 'active': activeTab === tab.value }"
              @click="activeTab = tab.value">
              {{ tab.label }}
            </button>
          </div>

          <!-- 各个设置面板 -->
          <!-- 1. 基本设置 -->
          <div v-if="activeTab === 'basic'" class="setting-panel">
            <div class="panel-header section-header--blue">
              <div class="section-header__icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              </div>
              <div class="section-header__text"><h3>基本设置</h3><p>控制AI回复的基本行为</p></div>
            </div>
            <div class="panel-body">
              <div class="form-row"><div class="form-row__label"><span class="form-label">自动回复</span><span class="form-hint">启用后，AI将自动回复新评论</span></div><label class="toggle"><input type="checkbox" v-model="settings.basic.autoReply" /><span class="toggle__track"><span class="toggle__thumb"></span></span></label></div>
              <div class="form-row"><div class="form-row__label"><span class="form-label">自动发布</span><span class="form-hint">关闭后AI回复将存为草稿，需手动审核发布</span></div><label class="toggle"><input type="checkbox" v-model="settings.basic.autoPublish" /><span class="toggle__track"><span class="toggle__thumb"></span></span></label></div>
              <div class="form-field"><div class="form-field__header"><span class="form-label">最大对话轮次</span><span class="form-badge">{{ settings.basic.maxConversationRounds }}</span></div><span class="form-hint">同一评论线程中AI最多自动回复的轮次</span><div class="slider"><input type="range" v-model.number="settings.basic.maxConversationRounds" min="1" max="100" class="slider__input" /><div class="slider__marks"><span>1</span><span>50</span><span>100</span></div></div></div>
              <div class="form-field"><label class="form-label">速率限制</label><span class="form-hint">每分钟最大AI回复数量</span><input type="number" v-model.number="settings.basic.rateLimitPerMinute" min="1" max="100" class="form-input" placeholder="10" /></div>
              <div class="form-field"><div class="form-field__header"><span class="form-label">最大重试次数</span><span class="form-badge">{{ settings.basic.maxRetryCount }}</span></div><span class="form-hint">AI生成失败时的最大重试次数</span><div class="slider"><input type="range" v-model.number="settings.basic.maxRetryCount" min="1" max="10" class="slider__input" /><div class="slider__marks"><span>1</span><span>5</span><span>10</span></div></div></div>
              <div class="form-field"><div class="form-field__header"><span class="form-label">评论者黑名单</span><button class="btn-link" @click="openCommenterDialog"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>添加评论者</button></div><span class="form-hint">支持名称、邮箱和正则。以 regex: 开头</span><textarea v-model="settings.basic.blockedCommenters" rows="2" class="form-textarea" placeholder="例如：张三, spam@example.com"></textarea></div>
            </div>
          </div>

          <!-- 2. AI角色设置 -->
          <div v-if="activeTab === 'persona'" class="setting-panel">
            <div class="panel-header section-header--purple">
              <div class="section-header__icon"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg></div>
              <div class="section-header__text"><h3>AI角色设置</h3><p>定义AI虚拟评论者的身份和风格</p></div>
            </div>
            <div class="panel-body">
              <VLoading v-if="personasLoading" />
              <div v-else-if="personas.length === 0" class="persona-empty"><span>请添加至少一个AI角色</span></div>
              <div v-else class="persona-list">
                <div v-for="p in personas" :key="p.metadata.name" class="persona-card">
                  <div class="persona-card__avatar">
                    <img v-if="getPersonaAvatar(p)" :src="getPersonaAvatar(p)" alt="头像" />
                    <span v-else class="persona-card__avatar-fallback">{{ (p.spec.displayName || '?').charAt(0) }}</span>
                  </div>
                  <div class="persona-card__info">
                    <div class="persona-card__name">
                      {{ p.spec.displayName || '未命名' }}
                      <span v-if="p.spec.gender === 'female'" class="persona-card__badge persona-card__badge--female">女</span>
                      <span v-else-if="p.spec.gender === 'male'" class="persona-card__badge persona-card__badge--male">男</span>
                      <span v-if="p.spec.neutralVoice" class="persona-card__badge persona-card__badge--neutral-voice">中性语气</span>
                      <span v-if="p.spec.wakeWord" class="persona-card__badge persona-card__badge--wake-word">唤醒: {{ p.spec.wakeWord }}</span>
                      <span v-if="p.spec.isDefault" class="persona-card__badge">默认</span>
                    </div>
                    <div class="persona-card__prompt">{{ p.spec.prompt || '暂无提示词' }}</div>
                  </div>
                  <div class="persona-card__actions">
                    <button class="btn-icon" title="编辑" @click="openPersonaDialog(p)"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg></button>
                    <button v-if="!p.spec?.isDefault" class="btn-icon btn-icon--danger" title="删除" @click="deletePersona(p)"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg></button>
                    <button v-if="!p.spec?.isDefault" class="btn-icon" title="设为默认" @click="setDefaultPersona(p)"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg></button>
                  </div>
                </div>
              </div>
              <button class="btn-add-persona" @click="openPersonaDialog(null)">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg> 添加角色
              </button>
            </div>
          </div>

          <!-- 3. 模型设置 -->
          <div v-if="activeTab === 'model'" class="setting-panel">
            <div class="panel-header section-header--green">
              <div class="section-header__icon"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg></div>
              <div class="section-header__text"><h3>模型设置</h3><p>配置AI Foundation提供的模型</p></div>
            </div>
            <div class="panel-body">
              <div class="form-field"><label class="form-label">AI模型名称</label><span class="form-hint">留空使用AI Foundation默认模型</span><input type="text" v-model="settings.model.modelName" class="form-input" placeholder="留空使用默认模型" /></div>
            </div>
          </div>

          <!-- 4. Prompt设置 -->
          <div v-if="activeTab === 'prompt'" class="setting-panel">
            <div class="panel-header section-header--amber">
              <div class="section-header__icon"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg></div>
              <div class="section-header__text"><h3>Prompt设置</h3><p>自定义AI回复的提示词模板</p></div>
            </div>
            <div class="panel-body">
              <div class="form-field">
                <label class="form-label">Prompt预设</label><span class="form-hint">选择预设风格，可多选</span>
                <div class="preset-grid">
                  <label v-for="p in promptPresets" :key="p.key" class="preset-item" :class="{ 'preset-item--active': isPresetEnabled(p.key) }">
                    <input type="checkbox" :checked="isPresetEnabled(p.key)" @change="togglePreset(p.key)" class="preset-checkbox" />
                    <div class="preset-item__content"><span class="preset-item__label">{{ p.label }}</span><span class="preset-item__desc">{{ p.desc }}</span></div>
                  </label>
                </div>
              </div>
              <div class="form-field">
                <label class="form-label">自定义Prompt模板</label>
                <textarea v-model="settings.prompt.customPromptTemplate" rows="10" class="form-textarea form-textarea--mono" placeholder="自定义Prompt模板"></textarea>
              </div>
            </div>
          </div>

          <!-- 5. 数据清理 -->
          <div v-if="activeTab === 'cleanup'" class="setting-panel">
            <div class="panel-header section-header--red">
              <div class="section-header__icon"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg></div>
              <div class="section-header__text"><h3>数据清理</h3><p>自动清理过期的AI回复记录</p></div>
            </div>
            <div class="panel-body">
              <div class="form-row"><div class="form-row__label"><span class="form-label">启用自动清理</span><span class="form-hint">每天自动清理</span></div><label class="toggle"><input type="checkbox" v-model="settings.cleanup.cleanupEnabled" /><span class="toggle__track"><span class="toggle__thumb"></span></span></label></div>
              <div class="form-field"><div class="form-field__header"><span class="form-label">保留天数</span><span class="form-badge">{{ settings.cleanup.retentionDays }} 天</span></div><div class="slider"><input type="range" v-model.number="settings.cleanup.retentionDays" min="1" max="365" class="slider__input" /><div class="slider__marks"><span>1天</span><span>180天</span><span>365天</span></div></div></div>
              <div class="form-row form-row--bordered"><div class="form-row__label"><span class="form-label">手动清理</span></div><VButton size="sm" type="secondary" @click="performCleanup" :disabled="cleanupLoading">{{ cleanupLoading ? '清理中...' : '立即清理' }}</VButton></div>
              <div v-if="cleanupResult !== null" class="cleanup-result">清理完成，共删除 {{ cleanupResult }} 条记录</div>
            </div>
          </div>
        </div>

        <!-- 右侧侧边栏 -->
        <div class="settings-sidebar">
          <div class="sidebar-card">
            <div class="sidebar-header"><h4>操作控制</h4><span v-if="hasUnsavedChanges" class="unsaved-badge">未保存</span></div>
            <button class="btn-save" @click="saveSettings" :disabled="saving">{{ saving ? '保存中...' : '保存设置' }}</button>
            <button class="btn-reset" @click="fetchSettings" :disabled="saving">还原当前值</button>
          </div>
          <div v-if="activeTab === 'prompt'" class="sidebar-card info-card">
            <h4 style="font-size:14px;margin-bottom:8px">可用变量</h4>
            <div class="sidebar-var" v-for="v in promptVariables" :key="v.name"><code>{{ v.name }}</code><span>{{ v.desc }}</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗：评论者黑名单 -->
    <div v-if="showCommenterDialog" class="dialog-overlay" @click.self="showCommenterDialog = false">
      <div class="dialog">
        <div class="dialog__header">
          <h3>选择评论者</h3>
          <button class="dialog__close" @click="showCommenterDialog = false"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog__search">
          <input v-model="commenterSearch" type="text" class="form-input" placeholder="搜索评论者名称或邮箱..." />
        </div>
        <div class="dialog__body">
          <VLoading v-if="commenterLoading" />
          <div v-else class="dialog__list">
            <button v-for="c in filteredCommenters" :key="c.displayName + c.email" class="dialog__item" @click="addCommenter(c)">
              <div class="dialog__item-avatar"><img v-if="c.avatarUrl" :src="c.avatarUrl" alt="" /><span v-else>{{ c.displayName?.charAt(0) || '?' }}</span></div>
              <div class="dialog__item-info"><div class="dialog__item-name">{{ c.displayName }}</div><div class="dialog__item-email">{{ c.email }}</div></div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗：AI角色编辑 -->
    <div v-if="showPersonaDialog" class="dialog-overlay" @click.self="showPersonaDialog = false">
      <div class="dialog" style="max-width:520px">
        <div class="dialog__header">
          <h3>{{ personaEditing ? '编辑角色' : '添加角色' }}</h3>
          <button class="dialog__close" @click="showPersonaDialog = false"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog__body" style="padding: 20px;">
          <div class="persona-dialog-preview" style="margin-bottom:16px">
            <div class="persona-card__avatar"><img v-if="personaDialogAvatar" :src="personaDialogAvatar" alt="" /><span v-else class="persona-card__avatar-fallback">{{ (personaForm.displayName || '?').charAt(0) }}</span></div>
            <div style="font-size:13px;color:#6b7280">{{ personaDialogAvatar ? 'Gravatar预览' : '未设置邮箱' }}</div>
          </div>
          <div class="form-field" style="margin-bottom:16px"><label class="form-label">昵称</label><input type="text" v-model="personaForm.displayName" class="form-input" /></div>
          <div class="form-field" style="margin-bottom:16px"><label class="form-label">邮箱</label><input type="email" v-model="personaForm.email" class="form-input" /></div>
          <div class="form-field" style="margin-bottom:16px">
            <label class="form-label">性别</label>
            <select v-model="personaForm.gender" class="form-input">
              <option value="female">女</option>
              <option value="male">男</option>
            </select>
          </div>
          <div class="form-row" style="margin-bottom:16px;border:none;background:transparent;padding:0"><label class="form-label">中性语气</label><label class="toggle"><input type="checkbox" v-model="personaForm.neutralVoice" /><span class="toggle__track"><span class="toggle__thumb"></span></span></label></div>
          <div class="form-field" style="margin-bottom:16px"><label class="form-label">唤醒词</label><input type="text" v-model="personaForm.wakeWord" class="form-input" /></div>
          <div class="form-field" style="margin-bottom:16px"><label class="form-label">提示词</label><textarea v-model="personaForm.prompt" rows="3" class="form-textarea"></textarea></div>
          <div class="form-row" style="border:none;background:transparent;padding:0"><label class="form-label">设为默认</label><label class="toggle"><input type="checkbox" v-model="personaForm.isDefault" /><span class="toggle__track"><span class="toggle__thumb"></span></span></label></div>
          <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:20px">
            <VButton @click="showPersonaDialog = false">取消</VButton>
            <VButton type="primary" @click="savePersona" :disabled="personaSaving">{{ personaSaving ? '保存中...' : '保存' }}</VButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗：导入配置确认 -->
    <VModal v-model:visible="showImportConfirm" title="确认导入配置">
      <p style="font-size:14px;color:#4b5563">导入将覆盖当前配置，此操作不可撤销。确定要继续吗？</p>
      <template #footer><VSpace><VButton @click="showImportConfirm = false">取消</VButton><VButton type="primary" :loading="importLoading" @click="confirmImport">确认</VButton></VSpace></template>
    </VModal>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue"
import { axiosInstance, coreApiClient } from "@halo-dev/api-client"
import { VPageHeader, VButton, VLoading, Toast, VModal, VSpace, IconPlug } from "@halo-dev/components"

const activeTab = ref("basic")
const tabItems = [
  { label: "基本设置", value: "basic" },
  { label: "AI角色", value: "persona" },
  { label: "模型设置", value: "model" },
  { label: "Prompt", value: "prompt" },
  { label: "数据清理", value: "cleanup" },
]

const promptVariables = [
  { name: '{{persona_prompt}}', desc: '角色设定' },
  { name: '{{comment}}', desc: '评论内容' },
  { name: '{{article}}', desc: '文章内容' },
  { name: '{{conversation_history}}', desc: '对话历史' },
]

const promptPresets = [
  { key: 'friendly', label: '友好型', desc: '热情友好，像朋友聊天' },
  { key: 'professional', label: '专业型', desc: '严谨正式，有逻辑性' },
  { key: 'humorous', label: '幽默型', desc: '轻松诙谐，适当幽默' },
  { key: 'concise', label: '简洁型', desc: '一两句话，简洁明了' },
]

const settings = reactive({
  basic: { autoReply: true, autoPublish: true, maxRetryCount: 3, blockedCommenters: "", maxConversationRounds: 8, rateLimitPerMinute: 10 },
  model: { modelName: "" },
  prompt: { customPromptTemplate: "", enabledPresets: [] as string[] },
  cleanup: { cleanupEnabled: true, retentionDays: 30 },
})

const loading = ref(false)
const saving = ref(false)
const lastSavedSnapshot = ref("")
const hasUnsavedChanges = computed(() => JSON.stringify(settings) !== lastSavedSnapshot.value)
const configMapName = "comment-ai-autopilot-configmap"
const apiBase = "/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1"

// Preset Logic
const enabledPresetKeys = computed({
  get: () => Array.isArray(settings.prompt.enabledPresets) ? settings.prompt.enabledPresets : [],
  set: (keys: string[]) => { settings.prompt.enabledPresets = keys }
})
const togglePreset = (key: string) => {
  const keys = [...enabledPresetKeys.value]; const idx = keys.indexOf(key)
  if (idx >= 0) keys.splice(idx, 1); else keys.push(key)
  enabledPresetKeys.value = keys
}
const isPresetEnabled = (key: string) => enabledPresetKeys.value.includes(key)

// Import / Export
const importLoading = ref(false)
const showImportConfirm = ref(false)
const importFileData = ref<any>(null)
const exportConfig = async () => { try { const { data } = await axiosInstance.get(`${apiBase}/export`); const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' }); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = `comment-ai-autopilot-config.json`; a.click(); URL.revokeObjectURL(url); Toast.success('配置已导出') } catch(e) { Toast.error('导出配置失败') } }
const handleImportFile = (event: Event) => { const input = event.target as HTMLInputElement; if (!input.files?.length) return; const reader = new FileReader(); reader.onload = (e) => { try { importFileData.value = JSON.parse(e.target?.result as string); showImportConfirm.value = true } catch { Toast.error('解析失败') } }; reader.readAsText(input.files[0]); input.value = '' }
const confirmImport = async () => { importLoading.value = true; try { await axiosInstance.post(`${apiBase}/import`, importFileData.value); Toast.success('导入成功'); showImportConfirm.value = false; await fetchSettings(); await fetchPersonas(); await computePersonaAvatars() } catch(e) { Toast.error('导入失败') } finally { importLoading.value = false } }

// Commenters & Cleanup
const showCommenterDialog = ref(false); const commenterList = ref<any[]>([]); const commenterSearch = ref(""); const commenterLoading = ref(false)
const filteredCommenters = computed(() => { const kw = commenterSearch.value.trim().toLowerCase(); if(!kw) return commenterList.value; return commenterList.value.filter(c => c.displayName.toLowerCase().includes(kw) || (c.email && c.email.toLowerCase().includes(kw))) })
const openCommenterDialog = async () => { showCommenterDialog.value = true; commenterLoading.value = true; try { const { data } = await axiosInstance.get(`${apiBase}/commenters`); commenterList.value = data.items || data } catch(e) { commenterList.value = [] } finally { commenterLoading.value = false } }
const addCommenter = (c: any) => { const v = c.email || c.displayName; const cur = settings.basic.blockedCommenters.split(",").map(s=>s.trim()).filter(Boolean); if(cur.includes(v)) return; cur.push(v); settings.basic.blockedCommenters = cur.join(","); Toast.success("已添加"); showCommenterDialog.value = false }
const cleanupLoading = ref(false); const cleanupResult = ref<number | null>(null)
const performCleanup = async () => { cleanupLoading.value=true; try { const { data } = await axiosInstance.post(`${apiBase}/cleanup`); cleanupResult.value = data.deletedCount ?? data ?? 0; Toast.success("清理完成") } catch(e){ Toast.error("清理失败") } finally { cleanupLoading.value=false } }

// Persona
const personasApiBase = `${apiBase}/personas`
const personas = ref<any[]>([]); const personasLoading = ref(false); const showPersonaDialog = ref(false); const personaEditing = ref<any>(null); const personaSaving = ref(false); const personaDialogAvatar = ref('')
const personaForm = reactive({ displayName: '', email: '', gender: 'female', neutralVoice: false, wakeWord: '', prompt: '', isDefault: false })
const computeGravatarHash = async (e: string) => { const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(e.trim().toLowerCase())); return Array.from(new Uint8Array(buf)).map(b=>b.toString(16).padStart(2,'0')).join('') }
const fetchPersonas = async () => { personasLoading.value=true; try { const { data } = await axiosInstance.get(personasApiBase); personas.value = (data.items || data).sort((a:any,b:any) => (a.spec?.isDefault ? -1 : 1)) } catch(e){} finally { personasLoading.value=false } }
const computePersonaAvatars = async () => { for (const p of personas.value) { if(p.spec?.email) { try { p._avatarUrl = `https://cn.cravatar.com/avatar/${await computeGravatarHash(p.spec.email)}` } catch{} } else { p._avatarUrl = '' } } }
const getPersonaAvatar = (p:any) => p._avatarUrl || ''
const openPersonaDialog = async (p: any) => { personaEditing.value = p; if(p){ Object.assign(personaForm, p.spec) } else { Object.assign(personaForm, { displayName:'', email:'', gender:'female', neutralVoice:false, wakeWord:'', prompt:'', isDefault:false }) }; if(personaForm.email) personaDialogAvatar.value = `https://cn.cravatar.com/avatar/${await computeGravatarHash(personaForm.email)}`; else personaDialogAvatar.value = ''; showPersonaDialog.value = true }
const savePersona = async () => { personaSaving.value=true; try { if(personaForm.isDefault) { for(const p of personas.value) { if(p.spec?.isDefault && p.metadata.name !== personaEditing.value?.metadata.name) { const {data:l} = await axiosInstance.get(`${personasApiBase}/${p.metadata.name}`); l.spec.isDefault=false; await axiosInstance.put(`${personasApiBase}/${p.metadata.name}`, l) } } } const payload = { spec: { ...personaForm }, apiVersion: 'comment-ai-autopilot.nxxy335.top/v1alpha1', kind: 'AiPersona', metadata: personaEditing.value ? { name: personaEditing.value.metadata.name } : { generateName: 'persona-' } }; if(personaEditing.value) { const {data:ex} = await axiosInstance.get(`${personasApiBase}/${personaEditing.value.metadata.name}`); await axiosInstance.put(`${personasApiBase}/${personaEditing.value.metadata.name}`, {...ex, spec: payload.spec}); Toast.success('已更新') } else { await axiosInstance.post(personasApiBase, payload); Toast.success('已添加') }; showPersonaDialog.value=false; await fetchPersonas(); await computePersonaAvatars() } catch(e){ Toast.error('保存失败') } finally { personaSaving.value=false } }
const deletePersona = async (p:any) => { if(p.spec?.isDefault) return Toast.warning('默认不可删'); if(confirm('确认删除？')) { await axiosInstance.delete(`${personasApiBase}/${p.metadata.name}`); await fetchPersonas(); await computePersonaAvatars() } }
const setDefaultPersona = async (p:any) => { for(const cp of personas.value){ if(cp.spec?.isDefault) { const {data:l} = await axiosInstance.get(`${personasApiBase}/${cp.metadata.name}`); l.spec.isDefault=false; await axiosInstance.put(`${personasApiBase}/${cp.metadata.name}`, l) } }; const {data:t} = await axiosInstance.get(`${personasApiBase}/${p.metadata.name}`); t.spec.isDefault=true; await axiosInstance.put(`${personasApiBase}/${p.metadata.name}`, t); await fetchPersonas() }

let emailDebounce: any; watch(() => personaForm.email, v => { clearTimeout(emailDebounce); if(!v) personaDialogAvatar.value=''; else emailDebounce = setTimeout(async () => { personaDialogAvatar.value = `https://cn.cravatar.com/avatar/${await computeGravatarHash(v)}` }, 500) })

const parseCfg = (d:any, k:string) => { const v = d[k]; if(!v) return {}; if(typeof v === 'string') { try{ return JSON.parse(v) }catch{ return {} } } return v }
const fetchSettings = async () => { loading.value=true; try { const { data } = await coreApiClient.configMap.getConfigMap({ name: configMapName }); if(data.data) { const d:any = data.data; const b = parseCfg(d,'basic'); const m = parseCfg(d,'model'); const p = parseCfg(d,'prompt'); const c = parseCfg(d,'cleanup'); if(b.autoReply !== undefined) Object.assign(settings.basic, b); if(m.modelName !== undefined) settings.model.modelName = m.modelName; if(p.customPromptTemplate !== undefined) { settings.prompt.customPromptTemplate = p.customPromptTemplate; settings.prompt.enabledPresets = Array.isArray(p.enabledPresets) ? p.enabledPresets : (p.enabledPresets||'').split(',').filter(Boolean) }; if(c.retentionDays !== undefined) Object.assign(settings.cleanup, c) } } catch(e){} finally { loading.value=false; lastSavedSnapshot.value = JSON.stringify(settings) } }
const saveSettings = async () => { saving.value=true; try { const { data:l } = await coreApiClient.configMap.getConfigMap({ name: configMapName }); l.data = { ...l.data, basic: JSON.stringify(settings.basic), model: JSON.stringify(settings.model), prompt: JSON.stringify(settings.prompt), cleanup: JSON.stringify(settings.cleanup) }; await coreApiClient.configMap.updateConfigMap({ name: configMapName, configMap: l }); Toast.success("保存成功"); lastSavedSnapshot.value = JSON.stringify(settings) } catch(e){ Toast.error("保存失败") } finally { saving.value=false } }

onMounted(async () => { await fetchSettings(); await fetchPersonas(); await computePersonaAvatars() })
</script>

<style scoped>
.settings-container { padding-bottom: 30px; }
.settings-main { margin: 20px; }
.settings-layout { display: flex; flex-direction: column; gap: 24px; }
@media (min-width: 1024px) { .settings-layout { flex-direction: row; align-items: flex-start; } }
.settings-content { flex: 1; width: 100%; }
.settings-sidebar { width: 100%; display: flex; flex-direction: column; gap: 16px; }
@media (min-width: 1024px) { .settings-sidebar { width: 280px; position: sticky; top: 20px; flex-shrink: 0; } }

/* 标签导航 */
.tabs-wrap { display: flex; gap: 8px; background: #fff; padding: 6px; border-radius: 10px; border: 1px solid #e5e7eb; margin-bottom: 24px; overflow-x: auto; }
.tab-btn { flex: 1; padding: 10px 16px; border: none; background: transparent; border-radius: 6px; cursor: pointer; color: #4b5563; font-weight: bold; font-size: 14px; white-space: nowrap; transition: 0.2s; }
.tab-btn:hover { background: #f3f4f6; }
.tab-btn.active { background: #2563eb; color: #fff; }

/* 面板样式 */
.setting-panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; }
.panel-header { display: flex; align-items: center; gap: 12px; padding: 16px 20px; border-left: 4px solid; border-bottom: 1px solid #e5e7eb; }
.section-header--blue { border-left-color: #3b82f6; background: #eff6ff; }
.section-header--purple { border-left-color: #8b5cf6; background: #f5f3ff; }
.section-header--green { border-left-color: #10b981; background: #ecfdf5; }
.section-header--amber { border-left-color: #f59e0b; background: #fffbeb; }
.section-header--red { border-left-color: #ef4444; background: #fef2f2; }
.section-header__icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; background: #fff; color: inherit; }
.section-header__icon svg { width: 18px; height: 18px; }
.section-header__text h3 { margin: 0 0 4px; font-size: 16px; font-weight: bold; color: #1f2937; }
.section-header__text p { margin: 0; font-size: 12px; color: #6b7280; }
.panel-body { padding: 20px; display: flex; flex-direction: column; gap: 20px; }

/* 表单行 */
.form-row { display: flex; justify-content: space-between; align-items: center; padding: 12px; background: #f9fafb; border-radius: 8px; }
.form-row--bordered { border-top: 1px solid #e5e7eb; border-radius: 0; background: transparent; margin: 0 -20px; padding: 16px 20px; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field__header { display: flex; justify-content: space-between; align-items: center; }
.form-label { font-size: 14px; font-weight: bold; color: #374151; }
.form-hint { font-size: 13px; color: #6b7280; }
.form-badge { background: #eff6ff; color: #2563eb; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }

/* 输入框 */
.form-input, .form-textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; outline: none; }
.form-input:focus, .form-textarea:focus { border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
.form-textarea--mono { font-family: monospace; }

/* 开关和滑块 */
.toggle { position: relative; display: inline-block; cursor: pointer; width: 44px; height: 24px; }
.toggle input { opacity: 0; width: 0; height: 0; }
.toggle__track { position: absolute; inset: 0; background: #d1d5db; border-radius: 12px; transition: 0.2s; }
.toggle__thumb { position: absolute; top: 2px; left: 2px; width: 20px; height: 20px; background: #fff; border-radius: 50%; transition: 0.2s; }
.toggle input:checked + .toggle__track { background: #3b82f6; }
.toggle input:checked + .toggle__track .toggle__thumb { transform: translateX(20px); }

.slider { padding: 5px 0; }
.slider__input { width: 100%; accent-color: #3b82f6; }
.slider__marks { display: flex; justify-content: space-between; font-size: 12px; color: #9ca3af; margin-top: 4px; }

/* 链接按钮 */
.btn-link { background: none; border: none; color: #3b82f6; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 4px; padding: 0; }
.btn-link:hover { color: #2563eb; }
.btn-link svg { width: 14px; height: 14px; }
.btn-add-persona { width: 100%; padding: 12px; background: #f3f4f6; color: #4b5563; border: 1px dashed #d1d5db; border-radius: 8px; cursor: pointer; font-weight: bold; display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 10px; }
.btn-add-persona:hover { background: #e5e7eb; color: #1f2937; }

/* 预设块 */
.preset-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.preset-item { display: flex; gap: 10px; padding: 12px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; cursor: pointer; }
.preset-item--active { border-color: #3b82f6; background: #eff6ff; }
.preset-checkbox { width: 16px; height: 16px; margin-top: 2px; accent-color: #3b82f6; }
.preset-item__label { font-weight: bold; font-size: 14px; color: #1f2937; display: block; }
.preset-item__desc { font-size: 12px; color: #6b7280; }

/* 侧边栏按钮 */
.sidebar-card { background: #f0fdf4; border: 1px solid #bbf7d0; padding: 20px; border-radius: 12px; }
.info-card { background: #fff; border-color: #e5e7eb; }
.sidebar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.sidebar-header h4 { margin: 0; font-size: 15px; font-weight: bold; color: #1f2937; }
.unsaved-badge { background: #fef08a; color: #854d0e; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-weight: bold; }
.btn-save { width: 100%; padding: 12px; background: #16a34a; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; margin-bottom: 10px; font-size: 14px; }
.btn-save:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-reset { width: 100%; padding: 10px; background: #fff; color: #374151; border: 1px solid #d1d5db; border-radius: 8px; cursor: pointer; font-size: 14px; }
.sidebar-var { margin-bottom: 8px; font-size: 13px; color: #4b5563; }
.sidebar-var code { background: #f3f4f6; color: #9333ea; padding: 2px 4px; border-radius: 4px; font-size: 12px; margin-right: 6px; display: inline-block; }

/* 角色卡片 */
.persona-empty { padding: 30px; text-align: center; color: #9ca3af; font-size: 14px; }
.persona-list { display: flex; flex-direction: column; gap: 12px; }
.persona-card { display: flex; align-items: center; gap: 14px; padding: 14px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 10px; }
.persona-card__avatar { width: 40px; height: 40px; border-radius: 50%; overflow: hidden; background: #e5e7eb; flex-shrink: 0; }
.persona-card__avatar img { width: 100%; height: 100%; object-fit: cover; }
.persona-card__avatar-fallback { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background: #8b5cf6; color: #fff; font-weight: bold; font-size: 16px; }
.persona-card__info { flex: 1; min-width: 0; }
.persona-card__name { font-size: 15px; font-weight: bold; color: #1f2937; display: flex; align-items: center; gap: 6px; }
.persona-card__badge { background: #e5e7eb; color: #374151; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold; }
.persona-card__badge--female { background: #fce7f3; color: #be185d; }
.persona-card__badge--male { background: #dbeafe; color: #1d4ed8; }
.persona-card__prompt { font-size: 12px; color: #6b7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 4px; }
.btn-icon { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: #6b7280; cursor: pointer; border-radius: 6px; }
.btn-icon:hover { background: #e5e7eb; color: #1f2937; }
.btn-icon--danger:hover { background: #fee2e2; color: #dc2626; }
.btn-icon svg { width: 16px; height: 16px; }

/* 弹窗通用 */
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.dialog { width: 90%; max-width: 400px; background: #fff; border-radius: 12px; display: flex; flex-direction: column; max-height: 80vh; }
.dialog__header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #e5e7eb; }
.dialog__header h3 { margin: 0; font-size: 16px; font-weight: bold; }
.dialog__close { background: none; border: none; cursor: pointer; color: #9ca3af; }
.dialog__close svg { width: 20px; height: 20px; }
.dialog__search { padding: 16px; border-bottom: 1px solid #e5e7eb; }
.dialog__body { overflow-y: auto; flex: 1; }
.dialog__list { display: flex; flex-direction: column; padding: 8px; }
.dialog__item { display: flex; align-items: center; gap: 12px; padding: 10px; background: none; border: none; cursor: pointer; border-radius: 8px; text-align: left; }
.dialog__item:hover { background: #f3f4f6; }
.dialog__item-avatar { width: 36px; height: 36px; border-radius: 50%; background: #d1d5db; color: #fff; display: flex; justify-content: center; align-items: center; font-weight: bold; overflow: hidden; flex-shrink: 0; }
.dialog__item-avatar img { width: 100%; height: 100%; object-fit: cover; }
.dialog__item-info { flex: 1; min-width: 0; }
.dialog__item-name { font-size: 14px; font-weight: bold; color: #1f2937; }
.dialog__item-email { font-size: 12px; color: #6b7280; }
.persona-dialog-preview { display: flex; align-items: center; gap: 12px; background: #f9fafb; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb; }
.cleanup-result { background: #ecfdf5; color: #059669; padding: 10px 14px; border-radius: 8px; font-size: 14px; font-weight: bold; margin-top: 10px; text-align: center; }
</style>
