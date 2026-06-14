# 安装与更新

## 前置要求

- Halo 2.23+
- AI Foundation 插件（必须） — 本插件依赖 AI Foundation 提供的AI模型能力，请先安装并配置 AI Foundation

## 安装

### 方式一：从 Release 下载

1. 前往 [GitHub Releases](https://github.com/nxxy335/plugin-comment-ai-autopilot/releases) 下载最新的 `.jar` 文件
2. 登录 Halo 管理后台
3. 进入 **插件** → **已安装** → 点击右上角 **安装** 按钮
4. 选择下载的 `.jar` 文件上传
5. 安装完成后启用插件

### 方式二：从源码构建

```bash
# 克隆仓库
git clone https://github.com/nxxy335/plugin-comment-ai-autopilot.git
cd plugin-comment-ai-autopilot

# 构建
./gradlew build -x test

# 构建产物位于 build/libs/ 目录
```

然后将生成的 `.jar` 文件通过 Halo 后台安装。

## 更新

1. 下载新版本的 `.jar` 文件
2. 在 Halo 后台 **插件** 页面找到「AI回评」
3. 点击插件卡片右上角的 **更多** → **更新**
4. 选择新的 `.jar` 文件上传

## 卸载

1. 在 Halo 后台 **插件** 页面找到「AI回评」
2. 先停用插件
3. 点击 **更多** → **卸载**
