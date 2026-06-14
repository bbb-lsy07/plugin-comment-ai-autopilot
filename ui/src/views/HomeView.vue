<template>
  <div class="comment-ai-autopilot-home">
    <VPageHeader title="AI回评">
      <template #icon>
        <IconPlug class="mr-2 self-center" />
      </template>
      <template #actions>
        <VButton type="primary" @click="openSettings"> 插件设置 </VButton>
      </template>
    </VPageHeader>

    <div class="m-4">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <VCard :body-class="['!p-4']">
          <div class="text-sm text-gray-500">总回复数</div>
          <div class="mt-1 text-2xl font-bold">{{ stats?.total || 0 }}</div>
        </VCard>
        <VCard :body-class="['!p-4']">
          <div class="text-sm text-gray-500">通过数</div>
          <div class="mt-1 text-2xl font-bold text-green-600">{{ stats?.passCount || 0 }}</div>
        </VCard>
        <VCard :body-class="['!p-4']">
          <div class="text-sm text-gray-500">失败数</div>
          <div class="mt-1 text-2xl font-bold text-red-600">{{ stats?.failCount || 0 }}</div>
        </VCard>
        <VCard :body-class="['!p-4']">
          <div class="text-sm text-gray-500">待审核</div>
          <div class="mt-1 text-2xl font-bold text-orange-500">{{ stats?.reviewingCount || 0 }}</div>
        </VCard>
        <VCard :body-class="['!p-4']">
          <div class="text-sm text-gray-500">平均评分</div>
          <div class="mt-1 text-2xl font-bold">{{ stats?.avgScore?.toFixed(1) || '0.0' }}</div>
        </VCard>
      </div>

      <!-- Sentiment Distribution -->
      <VCard class="mt-4" :body-class="['!p-4']">
        <h3 class="text-base font-medium mb-3">情感分布</h3>
        <div class="flex items-end gap-6">
          <div class="flex flex-col items-center">
            <div class="w-16 bg-green-100 rounded-t" :style="{ height: getSentimentBarHeight('POSITIVE') + 'px' }"></div>
            <div class="mt-1 text-sm font-medium text-green-600">{{ stats?.sentimentDistribution?.POSITIVE || 0 }}</div>
            <div class="text-xs text-gray-500">正面</div>
          </div>
          <div class="flex flex-col items-center">
            <div class="w-16 bg-gray-200 rounded-t" :style="{ height: getSentimentBarHeight('NEUTRAL') + 'px' }"></div>
            <div class="mt-1 text-sm font-medium text-gray-600">{{ stats?.sentimentDistribution?.NEUTRAL || 0 }}</div>
            <div class="text-xs text-gray-500">中性</div>
          </div>
          <div class="flex flex-col items-center">
            <div class="w-16 bg-red-100 rounded-t" :style="{ height: getSentimentBarHeight('NEGATIVE') + 'px' }"></div>
            <div class="mt-1 text-sm font-medium text-red-600">{{ stats?.sentimentDistribution?.NEGATIVE || 0 }}</div>
            <div class="text-xs text-gray-500">负面</div>
          </div>
        </div>
      </VCard>

      <!-- Daily Trend -->
      <VCard class="mt-4" :body-class="['!p-4']">
        <h3 class="text-base font-medium mb-3">近7日回复趋势</h3>
        <div class="flex items-end gap-2 h-32">
          <div
            v-for="day in (stats?.dailyTrend || [])"
            :key="day.date"
            class="flex-1 flex flex-col items-center justify-end h-full"
          >
            <div class="text-xs text-gray-500 mb-1">{{ day.count }}</div>
            <div
              class="w-full bg-blue-400 rounded-t transition-all"
              :style="{ height: getTrendBarHeight(day.count) + 'px' }"
            ></div>
            <div class="text-[10px] text-gray-400 mt-1">{{ formatTrendDate(day.date) }}</div>
          </div>
        </div>
      </VCard>

      <!-- AI Persona Section -->
      <VCard class="mt-4" :body-class="['!p-4']">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium">AI角色信息</h3>
            <p class="mt-1 text-sm text-gray-500">
              AI虚拟评论者，以独立身份参与评论讨论
            </p>
          </div>
          <VButton @click="openSettings"> 修改配置 </VButton>
        </div>

        <div v-if="persona" class="mt-4 space-y-3">
          <div class="flex items-center gap-3">
            <div class="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center text-lg">
              🤖
            </div>
            <div>
              <div class="font-medium">{{ persona.name }}</div>
              <div class="text-xs text-gray-400">AI虚拟评论者</div>
            </div>
          </div>
          <div v-if="persona.prompt" class="flex items-start gap-2">
            <span class="text-sm text-gray-500 shrink-0 whitespace-nowrap">人格提示词：</span>
            <span class="text-sm text-gray-600 line-clamp-3">{{ persona.prompt }}</span>
          </div>
        </div>
        <div v-else class="mt-4 text-sm text-gray-400">
          加载中...
        </div>
      </VCard>

      <!-- Quick Links -->
      <div class="mt-4 flex gap-4">
        <VButton @click="$router.push({ name: 'CommentAiAutopilotLogs' })">
          查看AI回复日志
        </VButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { axiosInstance } from "@halo-dev/api-client"
import { VPageHeader, VButton, VCard, Toast } from "@halo-dev/components"
import { IconPlug } from "@halo-dev/components"

interface DailyCount {
  date: string
  count: number
}

interface StatsResponse {
  total: number
  passCount: number
  failCount: number
  reviewingCount: number
  avgScore: number
  sentimentDistribution: Record<string, number>
  dailyTrend: DailyCount[]
}

interface PersonaResponse {
  name: string
  prompt: string
  avatar: string
}

const stats = ref<StatsResponse | null>(null)
const persona = ref<PersonaResponse | null>(null)

const fetchStats = async () => {
  try {
    const { data } = await axiosInstance.get(
      "/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/stats",
    )
    stats.value = data
  } catch (e) {
    console.error("Failed to fetch stats", e)
  }
}

const fetchPersona = async () => {
  try {
    const { data } = await axiosInstance.get(
      "/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/persona",
    )
    persona.value = data
  } catch (e) {
    console.error("Failed to fetch persona", e)
  }
}

const openSettings = () => {
  window.location.href = "/console/comment-ai-autopilot/settings"
}

const getSentimentBarHeight = (sentiment: string): number => {
  const dist = stats.value?.sentimentDistribution
  if (!dist) return 0
  const max = Math.max(dist.POSITIVE || 0, dist.NEUTRAL || 0, dist.NEGATIVE || 0, 1)
  const value = dist[sentiment] || 0
  return Math.max((value / max) * 80, value > 0 ? 8 : 0)
}

const getTrendBarHeight = (count: number): number => {
  const trend = stats.value?.dailyTrend
  if (!trend || trend.length === 0) return 0
  const max = Math.max(...trend.map(d => d.count), 1)
  return Math.max((count / max) * 80, count > 0 ? 8 : 0)
}

const formatTrendDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return parts.length >= 3 ? `${parts[1]}/${parts[2]}` : dateStr
}

onMounted(() => {
  fetchStats()
  fetchPersona()
})
</script>
