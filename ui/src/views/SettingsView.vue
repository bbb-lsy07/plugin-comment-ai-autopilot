<template>
  <div class="settings-page">
    <VPageHeader title="插件设置">
      <template #icon>
        <IconPlug class="mr-2 self-center" />
      </template>
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

    <div class="m-4">
      <VLoading v-if="loading" />

      <div v-if="!loading">
        <!-- Tab Navigation -->
        <div class="flex overflow-x-auto gap-2 p-1.5 bg-white border border-gray-200 rounded-xl mb-6 shadow-sm">
          <button v-for="tab in tabItems" :key="tab.value" 
            @click="activeTab = tab.value"
            class="flex-1 min-w-[80px] py-2 px-3 text-sm font-medium rounded-lg transition-colors whitespace-nowrap text-center"
            :class="activeTab === tab.value ? 'bg-blue-600 text-white shadow' : 'text-gray-600 hover:bg-gray-100'">
            {{ tab.label }}
          </button>
        </div>

        <!-- Main Grid Layout -->
        <div class="flex flex-col lg:flex-row gap-6 items-start">
          
          <!-- Left: Settings Sections -->
          <div class="flex-1 w-full space-y-6">
            
            <!-- ========== Basic Settings ========== -->
            <section v-if="activeTab === 'basic'" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div class="px-5 py-4 border-b border-gray-100 bg-blue-50/50 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold text-gray-800">基本设置</h3>
                  <p class="text-xs text-gray-500 mt-0.5">控制AI回复的基本行为</p>
                </div>
              </div>
              <div class="p-5 space-y-5">
                <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div class="text-sm font-medium text-gray-800">自动回复</div>
                    <div class="text-xs text-gray-500 mt-1">启用后，AI将自动回复新评论</div>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer shrink-0">
                    <input type="checkbox" v-model="settings.basic.autoReply" class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>
                
                <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div class="text-sm font-medium text-gray-800">自动发布</div>
                    <div class="text-xs text-gray-500 mt-1">关闭后AI回复将存为草稿，需手动审核发布</div>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer shrink-0">
                    <input type="checkbox" v-model="settings.basic.autoPublish" class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label class="text-sm font-medium text-gray-800">最大对话轮次</label>
                    <span class="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">{{ settings.basic.maxConversationRounds }}</span>
                  </div>
                  <p class="text-xs text-gray-500 mb-2">同一评论线程中AI最多自动回复的轮次</p>
                  <input type="range" v-model.number="settings.basic.maxConversationRounds" min="1" max="100" class="w-full accent-blue-600" />
                  <div class="flex justify-between text-xs text-gray-400 mt-1"><span>1</span><span>50</span><span>100</span></div>
                </div>

                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label class="text-sm font-medium text-gray-800">最大重试次数</label>
                    <span class="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">{{ settings.basic.maxRetryCount }}</span>
                  </div>
                  <p class="text-xs text-gray-500 mb-2">AI生成失败时的最大重试次数，采用指数退避策略</p>
                  <input type="range" v-model.number="settings.basic.maxRetryCount" min="1" max="10" class="w-full accent-blue-600" />
                  <div class="flex justify-between text-xs text-gray-400 mt-1"><span>1</span><span>5</span><span>10</span></div>
                </div>

                <div>
                  <label class="text-sm font-medium text-gray-800 block mb-1">速率限制</label>
                  <p class="text-xs text-gray-500 mb-2">每分钟最大AI回复数量</p>
                  <input type="number" v-model.number="settings.basic.rateLimitPerMinute" min="1" max="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="10" />
                </div>

                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label class="text-sm font-medium text-gray-800">评论者黑名单</label>
                    <button class="text-xs text-blue-600 hover:underline flex items-center gap-1" @click="openCommenterDialog">
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>
                      添加评论者
                    </button>
                  </div>
                  <p class="text-xs text-gray-500 mb-2">支持名称、邮箱和正则表达式。正则以 regex: 开头，如 regex:^spam.*</p>
                  <textarea v-model="settings.basic.blockedCommenters" rows="2" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="例如：张三, spam@example.com, 李四"></textarea>
                </div>
              </div>
            </section>

            <!-- ========== AI Persona Settings ========== -->
            <section v-if="activeTab === 'persona'" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div class="px-5 py-4 border-b border-gray-100 bg-purple-50/50 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold text-gray-800">AI角色设置</h3>
                  <p class="text-xs text-gray-500 mt-0.5">定义AI虚拟评论者的身份和风格</p>
                </div>
              </div>
              <div class="p-5 space-y-5">
                <VLoading v-if="personasLoading" />
                <div v-else-if="personas.length === 0" class="flex flex-col items-center gap-2 py-8 text-gray-400">
                  <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                  <span class="text-sm">请添加至少一个AI角色</span>
                </div>
                <div v-else class="space-y-2.5">
                  <div v-for="p in personas" :key="p.metadata.name" class="flex items-center gap-3.5 p-3.5 bg-gray-50 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-gray-100 transition">
                    <div class="w-11 h-11 shrink-0 relative">
                      <img v-if="getPersonaAvatar(p)" :src="getPersonaAvatar(p)" alt="头像" class="w-11 h-11 rounded-full object-cover border-2 border-purple-200" />
                      <div v-else class="w-11 h-11 rounded-full bg-gradient-to-br from-purple-500 to-purple-700 flex items-center justify-center text-white font-bold text-sm">{{ (p.spec.displayName || '?').charAt(0) }}</div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-1.5 flex-wrap">
                        <span class="text-sm font-semibold text-gray-800">{{ p.spec.displayName || '未命名' }}</span>
                        <span v-if="p.spec.gender === 'female'" class="px-1.5 py-0.5 text-[11px] font-medium bg-pink-100 text-pink-700 rounded">女</span>
                        <span v-else-if="p.spec.gender === 'male'" class="px-1.5 py-0.5 text-[11px] font-medium bg-blue-100 text-blue-700 rounded">男</span>
                        <span v-if="p.spec.neutralVoice" class="px-1.5 py-0.5 text-[11px] font-medium bg-gray-100 text-gray-600 rounded">中性语气</span>
                        <span v-if="p.spec.wakeWord" class="px-1.5 py-0.5 text-[11px] font-medium bg-yellow-100 text-yellow-700 rounded">唤醒: {{ p.spec.wakeWord }}</span>
                        <span v-if="p.spec.isDefault" class="px-1.5 py-0.5 text-[11px] font-medium bg-blue-100 text-blue-700 rounded">默认</span>
                      </div>
                      <div class="text-xs text-gray-500 mt-0.5 truncate max-w-[400px]">{{ p.spec.prompt || '暂无提示词' }}</div>
                    </div>
                    <div class="flex gap-1 shrink-0">
                      <button class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-500 hover:bg-gray-200 hover:text-gray-700 transition" title="编辑" @click="openPersonaDialog(p)">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                      </button>
                      <button v-if="!p.spec?.isDefault" class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-500 hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition" title="删除" @click="deletePersona(p)">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                      </button>
                      <button v-if="!p.spec?.isDefault" class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-500 hover:bg-yellow-50 hover:text-yellow-600 hover:border-yellow-200 transition" title="设为默认" @click="setDefaultPersona(p)">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <VButton type="secondary" @click="openPersonaDialog(null)">
                  <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                  添加角色
                </VButton>
              </div>
            </section>

            <!-- ========== Model Settings ========== -->
            <section v-if="activeTab === 'model'" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div class="px-5 py-4 border-b border-gray-100 bg-emerald-50/50 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold text-gray-800">模型设置</h3>
                  <p class="text-xs text-gray-500 mt-0.5">配置AI Foundation提供的模型</p>
                </div>
              </div>
              <div class="p-5 space-y-5">
                <div>
                  <label class="text-sm font-medium text-gray-800 block mb-1">AI模型名称</label>
                  <p class="text-xs text-gray-500 mb-2">留空使用AI Foundation默认模型，填写AiModel资源名称可指定模型</p>
                  <input type="text" v-model="settings.model.modelName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="留空使用默认模型" />
                </div>
              </div>
            </section>

            <!-- ========== Prompt Settings ========== -->
            <section v-if="activeTab === 'prompt'" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div class="px-5 py-4 border-b border-gray-100 bg-amber-50/50 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-amber-100 text-amber-600 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold text-gray-800">Prompt设置</h3>
                  <p class="text-xs text-gray-500 mt-0.5">自定义AI回复的提示词模板</p>
                </div>
              </div>
              <div class="p-5 space-y-5">
                <div>
                  <label class="text-sm font-medium text-gray-800 block mb-1">Prompt预设</label>
                  <p class="text-xs text-gray-500 mb-2">选择预设风格，可多选</p>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    <label v-for="p in promptPresets" :key="p.key" class="flex items-start gap-2.5 p-3 bg-gray-50 border border-gray-200 rounded-lg cursor-pointer hover:border-gray-300 hover:bg-gray-100 transition" :class="{ 'border-blue-500 bg-blue-50': isPresetEnabled(p.key) }">
                      <input type="checkbox" :checked="isPresetEnabled(p.key)" @change="togglePreset(p.key)" class="w-[18px] h-[18px] mt-0.5 accent-blue-600 cursor-pointer shrink-0" />
                      <div class="min-w-0">
                        <span class="text-sm font-medium text-gray-800 block">{{ p.label }}</span>
                        <span class="text-xs text-gray-500">{{ p.desc }}</span>
                      </div>
                    </label>
                  </div>
                </div>
                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label class="text-sm font-medium text-gray-800">自定义Prompt模板</label>
                    <span class="text-xs text-gray-400">留空使用默认模板</span>
                  </div>
                  <div class="flex flex-wrap gap-1.5 mb-2">
                    <span v-for="v in promptVariables" :key="v.name" class="inline-flex items-center gap-1 px-2.5 py-1 bg-gray-100 rounded-md text-xs text-gray-600">
                      <code class="font-mono text-[11px] bg-gray-200 px-1 rounded text-purple-700">{{ v.name }}</code>
                      {{ v.desc }}
                    </span>
                  </div>
                  <textarea v-model="settings.prompt.customPromptTemplate" rows="10" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition font-mono text-[13px] leading-relaxed" placeholder="自定义Prompt模板"></textarea>
                </div>
              </div>
            </section>

            <!-- ========== Cleanup Settings ========== -->
            <section v-if="activeTab === 'cleanup'" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div class="px-5 py-4 border-b border-gray-100 bg-red-50/50 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-red-100 text-red-600 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold text-gray-800">数据清理</h3>
                  <p class="text-xs text-gray-500 mt-0.5">自动清理过期的AI回复记录</p>
                </div>
              </div>
              <div class="p-5 space-y-5">
                <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div class="text-sm font-medium text-gray-800">启用自动清理</div>
                    <div class="text-xs text-gray-500 mt-1">每天自动清理超过保留天数的记录</div>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer shrink-0">
                    <input type="checkbox" v-model="settings.cleanup.cleanupEnabled" class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label class="text-sm font-medium text-gray-800">保留天数</label>
                    <span class="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">{{ settings.cleanup.retentionDays }} 天</span>
                  </div>
                  <p class="text-xs text-gray-500 mb-2">超过此天数的AI回复记录将被自动清理</p>
                  <input type="range" v-model.number="settings.cleanup.retentionDays" min="1" max="365" class="w-full accent-blue-600" />
                  <div class="flex justify-between text-xs text-gray-400 mt-1"><span>1天</span><span>180天</span><span>365天</span></div>
                </div>

                <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg border-t border-gray-200">
                  <div>
                    <div class="text-sm font-medium text-gray-800">手动清理</div>
                    <div class="text-xs text-gray-500 mt-1">立即执行一次清理操作</div>
                  </div>
                  <VButton size="sm" type="secondary" @click="performCleanup" :disabled="cleanupLoading">
                    {{ cleanupLoading ? '清理中...' : '立即清理' }}
                  </VButton>
                </div>

                <div v-if="cleanupResult !== null" class="inline-flex items-center gap-1.5 px-3.5 py-2 bg-green-50 text-green-700 rounded-lg text-sm font-medium">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                  清理完成，共删除 {{ cleanupResult }} 条记录
                </div>
              </div>
            </section>
          </div>

          <!-- Right: Sticky Sidebar -->
          <div class="w-full lg:w-72 shrink-0 lg:sticky lg:top-24 space-y-4">
            <div class="bg-blue-50 border border-blue-100 rounded-xl p-5 shadow-sm">
              <div class="flex items-center justify-between mb-4 pb-2.5 border-b border-blue-100">
                <h4 class="font-semibold text-gray-800 text-sm">操作</h4>
                <span v-if="hasUnsavedChanges" class="px-2 py-0.5 text-xs font-medium bg-orange-100 text-orange-700 rounded">未保存</span>
              </div>
              <div class="space-y-2.5">
                <VButton block type="primary" @click="saveSettings" :disabled="saving">
                  <template v-if="!saving">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                  </template>
                  {{ saving ? '保存中...' : '保存设置' }}
                </VButton>
                <VButton block @click="fetchSettings" :disabled="saving">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                  重置为当前值
                </VButton>
              </div>
            </div>

            <div v-if="activeTab === 'prompt'" class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
              <h4 class="font-semibold text-gray-800 mb-3 text-sm pb-2.5 border-b border-gray-100">可用模板变量</h4>
              <div class="space-y-2">
                <div v-for="v in promptVariables" :key="v.name" class="flex flex-col gap-0.5">
                  <code class="font-mono text-[11px] bg-gray-100 px-1.5 py-0.5 rounded text-purple-700 w-fit">{{ v.name }}</code>
                  <span class="text-xs text-gray-500">{{ v.desc }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Commenter Selection Dialog -->
    <div v-if="showCommenterDialog" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="showCommenterDialog = false">
      <div class="w-full max-w-[480px] bg-white rounded-2xl shadow-xl overflow-hidden mx-4">
        <div class="flex items-center justify-between px-6 pt-5 pb-0">
          <h3 class="text-base font-semibold text-gray-900">选择评论者</h3>
          <button class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition" @click="showCommenterDialog = false">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="px-6 py-3 relative">
          <svg class="absolute left-9 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
          <input v-model="commenterSearch" type="text" class="w-full pl-9 pr-3 py-2.5 text-sm border border-gray-200 rounded-lg bg-gray-50 outline-none focus:bg-white focus:border-blue-500 transition" placeholder="搜索评论者名称或邮箱..." />
        </div>
        <div class="max-h-80 overflow-y-auto px-4 pb-4">
          <VLoading v-if="commenterLoading" />
          <div v-else-if="filteredCommenters.length === 0" class="flex flex-col items-center gap-2 py-10 text-gray-400">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            <span class="text-sm">暂无评论者数据</span>
          </div>
          <div v-else class="flex flex-col gap-0.5">
            <button v-for="c in filteredCommenters" :key="c.displayName + c.email" class="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg hover:bg-blue-50 transition text-left" @click="addCommenter(c)">
              <div class="w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center text-xs font-semibold text-gray-500 shrink-0 overflow-hidden">
                <img v-if="c.avatarUrl" :src="c.avatarUrl" alt="" class="w-full h-full object-cover" />
                <span v-else>{{ c.displayName?.charAt(0) || '?' }}</span>
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-gray-800">{{ c.displayName }}</div>
                <div v-if="c.email" class="text-xs text-gray-400">{{ c.email }}</div>
              </div>
              <svg class="w-4.5 h-4.5 text-blue-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Persona Edit Dialog -->
    <div v-if="showPersonaDialog" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="showPersonaDialog = false">
      <div class="w-full max-w-[520px] bg-white rounded-2xl shadow-xl overflow-hidden mx-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between px-6 pt-5 pb-0">
          <h3 class="text-base font-semibold text-gray-900">{{ personaEditing ? '编辑角色' : '添加角色' }}</h3>
          <button class="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition" @click="showPersonaDialog = false">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="p-6 flex flex-col gap-4">
          <!-- Avatar Preview -->
          <div class="flex items-center gap-3 p-3 bg-purple-50/50 border border-purple-100 rounded-xl">
            <div class="w-12 h-12 shrink-0">
              <img v-if="personaDialogAvatar" :src="personaDialogAvatar" alt="Gravatar头像" class="w-12 h-12 rounded-full object-cover border-2 border-purple-200" />
              <div v-else class="w-12 h-12 rounded-full bg-gradient-to-br from-purple-500 to-purple-700 flex items-center justify-center text-white font-bold text-lg">{{ (personaForm.displayName || '?').charAt(0) }}</div>
            </div>
            <div class="text-xs text-gray-500">{{ personaDialogAvatar ? 'Gravatar头像预览' : '未设置邮箱，将使用默认头像' }}</div>
          </div>
          <!-- Nickname -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-gray-800">昵称</label>
            <input type="text" v-model="personaForm.displayName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="小回" />
          </div>
          <!-- Email -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-gray-800">邮箱</label>
            <p class="text-xs text-gray-500">用于Gravatar头像服务，留空使用默认头像</p>
            <input type="email" v-model="personaForm.email" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="ai@example.com" />
          </div>
          <!-- Gender & Neutral Voice -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-gray-800">性别与语气</label>
            <div class="flex items-center gap-4">
              <div class="flex gap-2">
                <label class="flex-1 flex items-center justify-center px-4 py-2 border rounded-lg cursor-pointer transition" :class="personaForm.gender === 'female' ? 'border-blue-500 bg-blue-50 text-blue-700' : 'border-gray-200 bg-gray-50 text-gray-700 hover:bg-gray-100'">
                  <input type="radio" v-model="personaForm.gender" value="female" class="hidden" />
                  <span class="text-sm font-medium">女</span>
                </label>
                <label class="flex-1 flex items-center justify-center px-4 py-2 border rounded-lg cursor-pointer transition" :class="personaForm.gender === 'male' ? 'border-blue-500 bg-blue-50 text-blue-700' : 'border-gray-200 bg-gray-50 text-gray-700 hover:bg-gray-100'">
                  <input type="radio" v-model="personaForm.gender" value="male" class="hidden" />
                  <span class="text-sm font-medium">男</span>
                </label>
              </div>
              <label class="flex items-center gap-2 cursor-pointer shrink-0">
                <input type="checkbox" v-model="personaForm.neutralVoice" class="w-4 h-4 accent-blue-600" id="neutralVoice" />
                <span class="text-sm text-gray-600">中性语气</span>
              </label>
            </div>
            <p class="text-xs text-gray-500">勾选中性语气则使用中性语气，取消勾选则跟随性别语气</p>
          </div>
          <!-- Wake Word -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-gray-800">唤醒词</label>
            <p class="text-xs text-gray-500">评论以此词开头则唤醒该角色回复，留空不启用。</p>
            <input type="text" v-model="personaForm.wakeWord" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="如：小回小回" />
          </div>
          <!-- Prompt -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-gray-800">人格提示词</label>
            <p class="text-xs text-gray-500">定义AI角色的性格、语气和回复风格</p>
            <textarea v-model="personaForm.prompt" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-500 bg-gray-50 focus:bg-white transition" placeholder="例如：你是「小回」，一个友善的评论者。你的回复简洁自然，像朋友聊天一样。"></textarea>
          </div>
          <!-- Is Default -->
          <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <div class="text-sm font-medium text-gray-800">设为默认角色</div>
              <div class="text-xs text-gray-500 mt-1">默认角色将用于没有指定角色的场景</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer shrink-0">
              <input type="checkbox" v-model="personaForm.isDefault" class="sr-only peer" />
              <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
          <!-- Actions -->
          <div class="flex justify-end gap-2 pt-1">
            <VButton @click="showPersonaDialog = false">取消</VButton>
            <VButton type="primary" @click="savePersona" :disabled="personaSaving">
              {{ personaSaving ? '保存中...' : '保存' }}
            </VButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Import Confirm Dialog -->
    <VModal v-model:visible="showImportConfirm" title="确认导入配置">
      <div class="space-y-3">
        <p class="text-sm text-gray-600">导入将覆盖当前配置，此操作不可撤销。确定要继续吗？</p>
        <div v-if="importFileData" class="text-xs text-gray-500">
          <p v-if="importFileData.configMap">- 包含插件设置</p>
          <p v-if="importFileData.personas">- 包含 {{ importFileData.personas.length }} 个AI角色</p>
        </div>
      </div>
      <template #footer>
        <VSpace>
          <VButton @click="showImportConfirm = false">取消</VButton>
          <VButton type="primary" :loading="importLoading" @click="confirmImport">确认导入</VButton>
        </VSpace>
      </template>
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
  { name: '{{persona_prompt}}', desc: '人格提示词' },
  { name: '{{safety_prompt}}', desc: '安全规范' },
  { name: '{{post_title}}', desc: '文章标题' },
  { name: '{{post_date}}', desc: '发布日期' },
  { name: '{{comment_count}}', desc: '评论数' },
  { name: '{{article}}', desc: '文章内容' },
  { name: '{{comment}}', desc: '评论内容' },
]

const promptPresets = [
  { key: 'friendly', label: '友好型', desc: '热情友好，像朋友聊天' },
  { key: 'professional', label: '专业型', desc: '严谨正式，有逻辑性' },
  { key: 'humorous', label: '幽默型', desc: '轻松诙谐，适当幽默' },
  { key: 'concise', label: '简洁型', desc: '一两句话，简洁明了' },
]

const enabledPresetKeys = computed({
  get: () => {
    const val: string | string[] = settings.prompt.enabledPresets as string | string[]
    return Array.isArray(val) ? val : (val || '').split(',').map((s: string) => s.trim()).filter(Boolean)
  },
  set: (keys: string[]) => {
    settings.prompt.enabledPresets = keys
  }
})

const togglePreset = (key: string) => {
  const keys = enabledPresetKeys.value.slice()
  const idx = keys.indexOf(key)
  if (idx >= 0) {
    keys.splice(idx, 1)
  } else {
    keys.push(key)
  }
  enabledPresetKeys.value = keys
}

const isPresetEnabled = (key: string) => enabledPresetKeys.value.includes(key)

const loading = ref(false)
const saving = ref(false)
const showCommenterDialog = ref(false)
const commenterList = ref<{ displayName: string; email: string; avatarUrl: string }[]>([])
const commenterSearch = ref("")
const commenterLoading = ref(false)
const cleanupLoading = ref(false)
const cleanupResult = ref<number | null>(null)

// Persona management
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const personas = ref<any[]>([])
const personasLoading = ref(false)
const showPersonaDialog = ref(false)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const personaEditing = ref<any | null>(null)
const personaSaving = ref(false)
const personaForm = reactive({
  displayName: '',
  email: '',
  gender: 'female',
  neutralVoice: false,
  wakeWord: '',
  prompt: '',
  isDefault: false,
})
const personaDialogAvatar = ref('')

const settings = reactive({
  basic: { autoReply: true, autoPublish: true, maxRetryCount: 3, blockedCommenters: "", maxConversationRounds: 8, rateLimitPerMinute: 10 },
  model: { modelName: "" },
  prompt: { customPromptTemplate: "", enabledPresets: [] as string[] },
  cleanup: { cleanupEnabled: true, retentionDays: 30 },
})

// Track unsaved changes: snapshot of settings after last fetch/save
const lastSavedSnapshot = ref("")
const hasUnsavedChanges = computed(() => {
  return JSON.stringify(settings) !== lastSavedSnapshot.value
})

const configMapName = "comment-ai-autopilot-configmap"
const apiBase = "/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1"

// ===== Export / Import =====
const exportConfig = async () => {
  try {
    const { data } = await axiosInstance.get(`${apiBase}/export`)
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `comment-ai-autopilot-config-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
    Toast.success('配置已导出')
  } catch (e) {
    console.error('Failed to export config', e)
    Toast.error('导出配置失败')
  }
}

const importLoading = ref(false)
const showImportConfirm = ref(false)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const importFileData = ref<any>(null)

const handleImportFile = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  const file = input.files[0]
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = JSON.parse(e.target?.result as string)
      if (!data.configMap && !data.personas) {
        Toast.error('无效的配置文件格式')
        return
      }
      importFileData.value = data
      showImportConfirm.value = true
    } catch {
      Toast.error('无法解析配置文件')
    }
  }
  reader.readAsText(file)
  // Reset input to allow re-selecting same file
  input.value = ''
}

const confirmImport = async () => {
  if (!importFileData.value) return
  importLoading.value = true
  try {
    await axiosInstance.post(`${apiBase}/import`, importFileData.value)
    Toast.success('配置已导入')
    showImportConfirm.value = false
    importFileData.value = null
    // Refresh data
    await fetchPersonas()
    await computePersonaAvatars()
  } catch (e) {
    console.error('Failed to import config', e)
    Toast.error('导入配置失败')
  } finally {
    importLoading.value = false
  }
}

const openCommenterDialog = async () => {
  showCommenterDialog.value = true
  commenterSearch.value = ""
  commenterLoading.value = true
  try {
    const { data } = await axiosInstance.get("/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/commenters")
    commenterList.value = Array.isArray(data) ? data : (data.items || [])
  } catch (e) {
    console.error("Failed to fetch commenters", e)
    Toast.error("获取评论者列表失败")
    commenterList.value = []
  } finally {
    commenterLoading.value = false
  }
}

const filteredCommenters = computed(() => {
  const kw = commenterSearch.value.trim().toLowerCase()
  if (!kw) return commenterList.value
  return commenterList.value.filter(c => c.displayName.toLowerCase().includes(kw) || (c.email && c.email.toLowerCase().includes(kw)))
})

const addCommenter = (commenter: { displayName: string; email: string }) => {
  const value = commenter.email || commenter.displayName
  if (!value) return
  const current = settings.basic.blockedCommenters.split(",").map(s => s.trim()).filter(Boolean)
  if (current.includes(value)) { Toast.info("该评论者已在黑名单中"); return }
  current.push(value)
  settings.basic.blockedCommenters = current.join(",")
  Toast.success("已添加到黑名单")
  showCommenterDialog.value = false
}

const computeGravatarHash = async (email: string): Promise<string> => {
  const normalized = email.trim().toLowerCase()
  const encoder = new TextEncoder()
  const data = encoder.encode(normalized)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

const performCleanup = async () => {
  cleanupLoading.value = true
  cleanupResult.value = null
  try {
    const { data } = await axiosInstance.post("/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/cleanup")
    cleanupResult.value = data.deletedCount ?? data ?? 0
    Toast.success(`清理完成，共删除 ${cleanupResult.value} 条记录`)
  } catch { Toast.error("清理失败") }
  finally { cleanupLoading.value = false }
}

// ===== Persona CRUD =====
const personasApiBase = "/apis/console.api.comment-ai-autopilot.nxxy335.top/v1alpha1/personas"

const fetchPersonas = async () => {
  personasLoading.value = true
  try {
    const { data } = await axiosInstance.get(personasApiBase)
    const list = Array.isArray(data) ? data : (data.items || [])
    // Default persona first
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    personas.value = list.sort((a: any, b: any) => {
      if (a.spec?.isDefault && !b.spec?.isDefault) return -1
      if (!a.spec?.isDefault && b.spec?.isDefault) return 1
      return 0
    })
  } catch (e) {
    console.error("Failed to fetch personas", e)
    personas.value = []
  } finally {
    personasLoading.value = false
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const getPersonaAvatar = (persona: any) => {
  if (persona.spec?.email) {
    return persona._avatarUrl || ''
  }
  return ''
}

// Compute avatar for each persona on list load
const computePersonaAvatars = async () => {
  for (const p of personas.value) {
    if (p.spec?.email) {
      try {
        const hash = await computeGravatarHash(p.spec.email)
        p._avatarUrl = `https://cn.cravatar.com/avatar/${hash}`
      } catch { p._avatarUrl = '' }
    } else {
      p._avatarUrl = ''
    }
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const openPersonaDialog = async (persona: any | null) => {
  personaEditing.value = persona
  if (persona) {
    personaForm.displayName = persona.spec.displayName || ''
    personaForm.email = persona.spec.email || ''
    personaForm.gender = persona.spec.gender || 'female'
    personaForm.neutralVoice = persona.spec.neutralVoice || false
    personaForm.wakeWord = persona.spec.wakeWord || ''
    personaForm.prompt = persona.spec.prompt || ''
    personaForm.isDefault = persona.spec.isDefault || false
  } else {
    personaForm.displayName = ''
    personaForm.email = ''
    personaForm.gender = 'female'
    personaForm.neutralVoice = false
    personaForm.wakeWord = ''
    personaForm.prompt = ''
    personaForm.isDefault = false
  }
  personaDialogAvatar.value = ''
  if (personaForm.email) {
    try {
      const hash = await computeGravatarHash(personaForm.email)
      personaDialogAvatar.value = `https://cn.cravatar.com/avatar/${hash}`
    } catch { personaDialogAvatar.value = '' }
  }
  showPersonaDialog.value = true
}

const savePersona = async () => {
  if (!personaForm.displayName.trim()) {
    Toast.warning('请输入角色昵称')
    return
  }
  // Prevent removing default flag from the only default persona
  if (!personaForm.isDefault && personaEditing.value?.spec?.isDefault) {
    Toast.warning('必须保留至少一个默认角色，请先将其他角色设为默认')
    return
  }
  personaSaving.value = true
  try {
    // If setting as default, first unset current default
    if (personaForm.isDefault) {
      for (const p of personas.value) {
        if (p.spec?.isDefault) {
          // Skip self when editing
          if (personaEditing.value && p.metadata.name === personaEditing.value.metadata.name) continue
          const { data: latest } = await axiosInstance.get(`${personasApiBase}/${p.metadata.name}`)
          latest.spec.isDefault = false
          await axiosInstance.put(`${personasApiBase}/${p.metadata.name}`, latest)
        }
      }
    }
    const payload = {
      spec: {
        displayName: personaForm.displayName,
        email: personaForm.email,
        gender: personaForm.gender,
        neutralVoice: personaForm.neutralVoice,
        wakeWord: personaForm.wakeWord,
        prompt: personaForm.prompt,
        isDefault: personaForm.isDefault,
      },
      apiVersion: 'comment-ai-autopilot.nxxy335.top/v1alpha1',
      kind: 'AiPersona',
      metadata: personaEditing.value
        ? { name: personaEditing.value.metadata.name }
        : { generateName: 'persona-' },
    }
    if (personaEditing.value) {
      // Update
      const { data: existing } = await axiosInstance.get(`${personasApiBase}/${personaEditing.value.metadata.name}`)
      const updated = { ...existing, spec: { ...existing.spec, ...payload.spec } }
      await axiosInstance.put(`${personasApiBase}/${personaEditing.value.metadata.name}`, updated)
      Toast.success('角色已更新')
    } else {
      // Create
      await axiosInstance.post(personasApiBase, payload)
      Toast.success('角色已添加')
    }
    showPersonaDialog.value = false
    await fetchPersonas()
    await computePersonaAvatars()
  } catch (e) {
    console.error('Failed to save persona', e)
    Toast.error('保存角色失败')
  } finally {
    personaSaving.value = false
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const deletePersona = async (persona: any) => {
  if (persona.spec?.isDefault) {
    Toast.warning('默认角色不可删除，请先将其他角色设为默认')
    return
  }
  if (!confirm(`确定要删除角色「${persona.spec?.displayName || '未命名'}」吗？`)) return
  try {
    await axiosInstance.delete(`${personasApiBase}/${persona.metadata.name}`)
    Toast.success('角色已删除')
    await fetchPersonas()
    await computePersonaAvatars()
  } catch (e) {
    console.error('Failed to delete persona', e)
    Toast.error('删除角色失败')
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const setDefaultPersona = async (persona: any) => {
  try {
    // First unset current default
    for (const p of personas.value) {
      if (p.spec?.isDefault && p.metadata.name !== persona.metadata.name) {
        const { data: latest } = await axiosInstance.get(`${personasApiBase}/${p.metadata.name}`)
        latest.spec.isDefault = false
        await axiosInstance.put(`${personasApiBase}/${p.metadata.name}`, latest)
      }
    }
    // Set new default
    const { data: latestTarget } = await axiosInstance.get(`${personasApiBase}/${persona.metadata.name}`)
    latestTarget.spec.isDefault = true
    await axiosInstance.put(`${personasApiBase}/${persona.metadata.name}`, latestTarget)
    Toast.success('已设为默认角色')
    await fetchPersonas()
  } catch (e) {
    console.error('Failed to set default persona', e)
    Toast.error('设置默认角色失败')
  }
}

// Watch persona dialog email for Gravatar preview
let emailDebounceTimer: ReturnType<typeof setTimeout> | null = null
watch(() => personaForm.email, (newEmail) => {
  if (emailDebounceTimer) clearTimeout(emailDebounceTimer)
  if (!newEmail || !newEmail.trim()) { personaDialogAvatar.value = ''; return }
  emailDebounceTimer = setTimeout(async () => {
    try {
      const hash = await computeGravatarHash(newEmail)
      personaDialogAvatar.value = `https://cn.cravatar.com/avatar/${hash}`
    } catch { personaDialogAvatar.value = '' }
  }, 500)
})

const parseConfigSection = (data: Record<string, unknown>, key: string): Record<string, unknown> => {
  const val = data[key]
  if (!val) return {}
  if (typeof val === 'string') {
    try { return JSON.parse(val) } catch { return {} }
  }
  return val as Record<string, unknown>
}

const fetchSettings = async () => {
  loading.value = true
  try {
    const { data } = await coreApiClient.configMap.getConfigMap({ name: configMapName })
    if (data.data) {
      const d = data.data as Record<string, unknown>
      const basic = parseConfigSection(d, 'basic') as Record<string, unknown>
      const model = parseConfigSection(d, 'model') as Record<string, unknown>
      const prompt = parseConfigSection(d, 'prompt') as Record<string, unknown>
      const cleanup = parseConfigSection(d, 'cleanup') as Record<string, unknown>
      if (Object.keys(basic).length) { settings.basic.autoReply = basic.autoReply !== false; settings.basic.autoPublish = basic.autoPublish !== false; settings.basic.maxRetryCount = (basic.maxRetryCount as number) || 3; settings.basic.blockedCommenters = (basic.blockedCommenters as string) || ""; settings.basic.maxConversationRounds = (basic.maxConversationRounds as number) || 8; settings.basic.rateLimitPerMinute = (basic.rateLimitPerMinute as number) || 10 }
      if (Object.keys(model).length) { settings.model.modelName = (model.modelName as string) || "" }
      if (Object.keys(prompt).length) { settings.prompt.customPromptTemplate = (prompt.customPromptTemplate as string) || ""; const ep = prompt.enabledPresets; settings.prompt.enabledPresets = Array.isArray(ep) ? ep : (typeof ep === 'string' ? (ep as string).split(",").map((s: string) => s.trim()).filter(Boolean) : []) }
      if (Object.keys(cleanup).length) { settings.cleanup.cleanupEnabled = cleanup.cleanupEnabled !== false; settings.cleanup.retentionDays = (cleanup.retentionDays as number) || 30 }
    }
  } catch (e) {
    console.error("Failed to fetch settings", e)
    Toast.error("加载设置失败，请刷新页面重试")
  }
  finally {
    loading.value = false
    // Update snapshot after fetch to reset unsaved indicator
    lastSavedSnapshot.value = JSON.stringify(settings)
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    const { data: latest } = await coreApiClient.configMap.getConfigMap({ name: configMapName })
    const updated = { ...latest }
    updated.data = {
      ...updated.data,
      basic: JSON.stringify(settings.basic),
      model: JSON.stringify(settings.model),
      prompt: JSON.stringify(settings.prompt),
      cleanup: JSON.stringify(settings.cleanup),
    }
    await coreApiClient.configMap.updateConfigMap({ name: configMapName, configMap: updated })
    Toast.success("设置已保存")
    // Update snapshot after save to reset unsaved indicator
    lastSavedSnapshot.value = JSON.stringify(settings)
  } catch (e) { console.error("Failed to save settings", e); Toast.error("保存设置失败") }
  finally { saving.value = false }
}

onMounted(async () => {
  await fetchSettings()
  await fetchPersonas()
  await computePersonaAvatars()
})
</script>

<style scoped>
/* Only keep line-clamp and peer-checked utilities that Tailwind may not cover in this scope */
</style>
