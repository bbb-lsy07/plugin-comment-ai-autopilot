<template>
  <div class="comment-ai-autopilot-settings">
    <VPageHeader title="插件设置">
      <template #icon>
        <IconPlug class="mr-2 self-center" />
      </template>
      <template #actions>
        <VButton @click="$router.push({ name: 'CommentAiAutopilot' })"> 返回概览 </VButton>
      </template>
    </VPageHeader>

    <div class="m-4">
      <VCard :body-class="['!p-0']">
        <!-- Tab Navigation -->
        <div class="flex border-b">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="px-6 py-3 text-sm font-medium border-b-2 transition-colors"
            :class="activeTab === tab.key
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="p-6">
          <VLoading v-if="loading" />

          <!-- Basic Settings -->
          <div v-if="activeTab === 'basic' && !loading" class="space-y-6">
            <div class="flex items-center justify-between">
              <div>
                <div class="font-medium">自动回复</div>
                <div class="text-sm text-gray-500">启用后，AI将自动回复新评论</div>
              </div>
              <label class="relative inline-flex cursor-pointer items-center">
                <input type="checkbox" v-model="settings.basic.autoReply" class="peer sr-only" />
                <div class="peer h-6 w-11 rounded-full bg-gray-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all peer-checked:bg-blue-600 peer-checked:after:translate-x-full peer-checked:after:border-white"></div>
              </label>
            </div>
            <div class="flex items-center justify-between">
              <div>
                <div class="font-medium">自动发布</div>
                <div class="text-sm text-gray-500">审核通过后自动发布AI回复</div>
              </div>
              <label class="relative inline-flex cursor-pointer items-center">
                <input type="checkbox" v-model="settings.basic.autoPublish" class="peer sr-only" />
                <div class="peer h-6 w-11 rounded-full bg-gray-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all peer-checked:bg-blue-600 peer-checked:after:translate-x-full peer-checked:after:border-white"></div>
              </label>
            </div>
            <div>
              <label class="font-medium">最大重试次数</label>
              <input
                type="number"
                v-model.number="settings.basic.maxRetryCount"
                min="1"
                max="10"
                class="mt-1 block w-full max-w-xs rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div>
              <label class="font-medium">评论者黑名单</label>
              <div class="mt-1 text-sm text-gray-500">输入评论者显示名称，多个用逗号分隔。这些评论者的评论不会触发AI回复</div>
              <textarea
                v-model="settings.basic.blockedCommenters"
                rows="3"
                class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="例如: 张三,李四"
              ></textarea>
            </div>
          </div>

          <!-- Persona Settings -->
          <div v-if="activeTab === 'persona' && !loading" class="space-y-6">
            <div>
              <label class="font-medium">AI角色昵称</label>
              <input
                type="text"
                v-model="settings.persona.personaName"
                class="mt-1 block w-full max-w-md rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="小回"
              />
            </div>
            <div>
              <label class="font-medium">AI角色邮箱</label>
              <div class="mt-1 text-sm text-gray-500">用于Gravatar头像服务展示头像，留空则使用默认头像</div>
              <input
                type="email"
                v-model="settings.persona.personaEmail"
                class="mt-1 block w-full max-w-md rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="ai@example.com"
              />
            </div>
            <div>
              <label class="font-medium">AI角色人格提示词</label>
              <textarea
                v-model="settings.persona.personaPrompt"
                rows="4"
                class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="定义AI角色的性格和回复风格"
              ></textarea>
            </div>
          </div>

          <!-- Model Settings -->
          <div v-if="activeTab === 'model' && !loading" class="space-y-6">
            <div>
              <label class="font-medium">AI模型名称</label>
              <div class="mt-1 text-sm text-gray-500">留空使用AI Foundation默认模型，填写AiModel资源名称可指定模型</div>
              <input
                type="text"
                v-model="settings.model.modelName"
                class="mt-2 block w-full max-w-md rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="留空使用默认模型"
              />
            </div>
          </div>

          <!-- Prompt Settings -->
          <div v-if="activeTab === 'prompt' && !loading" class="space-y-6">
            <div>
              <label class="font-medium">自定义Prompt模板</label>
              <div class="mt-1 text-sm text-gray-500">
                可用变量: <code class="text-xs bg-gray-100 px-1">{'{{'}persona_prompt{{'}}'}}</code>,
                <code class="text-xs bg-gray-100 px-1">{'{{'}safety_prompt{{'}}'}}</code>,
                <code class="text-xs bg-gray-100 px-1">{'{{'}article{{'}}'}}</code>,
                <code class="text-xs bg-gray-100 px-1">{'{{'}comment{{'}}'}}</code>
              </div>
              <textarea
                v-model="settings.prompt.customPromptTemplate"
                rows="10"
                class="mt-2 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm font-mono focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="自定义Prompt模板"
              ></textarea>
            </div>
          </div>

          <!-- Save Button -->
          <div v-if="!loading" class="mt-6 flex justify-end">
            <VButton type="primary" @click="saveSettings" :disabled="saving">
              {{ saving ? '保存中...' : '保存设置' }}
            </VButton>
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { axiosInstance } from "@halo-dev/api-client"
import { VPageHeader, VButton, VCard, VLoading, Toast } from "@halo-dev/components"
import { IconPlug } from "@halo-dev/components"

const tabs = [
  { key: "basic", label: "基本设置" },
  { key: "persona", label: "AI角色设置" },
  { key: "model", label: "模型设置" },
  { key: "prompt", label: "Prompt设置" },
]

const activeTab = ref("basic")
const loading = ref(false)
const saving = ref(false)

const settings = reactive({
  basic: {
    autoReply: true,
    autoPublish: true,
    maxRetryCount: 3,
    blockedCommenters: "",
  },
  persona: {
    personaName: "小回",
    personaPrompt: "",
    personaEmail: "",
  },
  model: {
    modelName: "",
  },
  prompt: {
    customPromptTemplate: "",
  },
})

const configMapName = "comment-ai-autopilot-configmap"

const fetchSettings = async () => {
  loading.value = true
  try {
    const { data } = await axiosInstance.get(
      `/api/v1alpha1/configmaps/${configMapName}`,
    )
    if (data.data) {
      const d = data.data
      if (d.basic) {
        settings.basic.autoReply = d.basic.autoReply !== false
        settings.basic.autoPublish = d.basic.autoPublish !== false
        settings.basic.maxRetryCount = d.basic.maxRetryCount || 3
        settings.basic.blockedCommenters = d.basic.blockedCommenters || ""
      }
      if (d.persona) {
        settings.persona.personaName = d.persona.personaName || "小回"
        settings.persona.personaPrompt = d.persona.personaPrompt || ""
        settings.persona.personaEmail = d.persona.personaEmail || ""
      }
      if (d.model) {
        settings.model.modelName = d.model.modelName || ""
      }
      if (d.prompt) {
        settings.prompt.customPromptTemplate = d.prompt.customPromptTemplate || ""
      }

    }
  } catch (e) {
    console.error("Failed to fetch settings", e)
  } finally {
    loading.value = false
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    // Fetch latest version first
    const { data: latest } = await axiosInstance.get(
      `/api/v1alpha1/configmaps/${configMapName}`,
    )

    const updated = { ...latest }
    updated.data = {
      ...updated.data,
      basic: {
        autoReply: settings.basic.autoReply,
        autoPublish: settings.basic.autoPublish,
        maxRetryCount: settings.basic.maxRetryCount,
        blockedCommenters: settings.basic.blockedCommenters,
      },
      persona: {
        personaName: settings.persona.personaName,
        personaPrompt: settings.persona.personaPrompt,
        personaEmail: settings.persona.personaEmail,
      },
      model: {
        modelName: settings.model.modelName,
      },
      prompt: {
        customPromptTemplate: settings.prompt.customPromptTemplate,
      },

    }

    await axiosInstance.put(
      `/api/v1alpha1/configmaps/${configMapName}`,
      updated,
    )
    Toast.success("设置已保存")
  } catch (e) {
    console.error("Failed to save settings", e)
    Toast.error("保存设置失败")
  } finally {
    saving.value = false
  }
}

onMounted(fetchSettings)
</script>
