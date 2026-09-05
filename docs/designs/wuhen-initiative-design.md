---
design_type: initiative
created_at: 2025-01-09
---

# 无痕 (WuHen) - 纯本地视频解析保存应用战略设计

## 🎯 愿景与目标

**无痕 (WuHen)** 是一款面向 Android 平台的纯本地视频解析下载工具，通过逆向分析各大视频平台的 API，实现无水印、高清晰度的视频/音频下载。本项目致力于为用户提供简洁、高效、隐私友好的媒体内容获取体验。

### 核心价值主张
- **纯净无扰**：无任何广告、无水印、无多余功能干扰
- **完全本地**：所有解析逻辑运行在设备端，保护用户隐私
- **Modern UI/UX**：采用 Material Design 3 设计规范，提供流畅的视觉体验
- **深度定制**：支持多清晰度选择、元数据写入、智能文件名管理

## 📋 范围与边界

### 纳入范围 (In Scope)

#### 核心功能
- ✅ 主流中文视频平台解析（抖音、快手、B 站、小红书、皮皮虾）
- ✅ 纯客户端逆向解析实现（无需第三方依赖）
- ✅ 多清晰度视频流选择与下载
- ✅ 后台下载队列管理与断点续传
- ✅ 存储空间管理与清理
- ✅ 原格式保存与元数据嵌入
- ✅ 智能文件名规范化

#### 技术栈
- Kotlin 作为唯一编程语言
- Jetpack Compose + Material Design 3
- MVI 架构模式
- Kotlin Coroutines & Flow 异步处理
- Room 数据库 + DataStore 配置存储
- Android App Startup 初始化器

#### 用户体验
- 三 Tab 结构化界面（首页、下载管理、设置）
- 剪贴板监听与分享集成
- 通知栏实时进度展示
- Material You 动态取色

### 排除范围 (Out of Scope)

#### MVP 版本不包含
- ❌ iOS 版本支持
- ❌ 视频播放功能（仅下载管理）
- ❌ 云端同步与备份
- ❌ 多语言国际化（首版仅简体中文）
- ❌ 插件化扩展机制
- ❌ 社区贡献解析器的开放接口

#### 长期规划可能考虑
- ⏳ YouTube、Netflix 等国际平台支持
- ⏳ 批量订阅与自动下载
- ⏳ 视频合并/分割高级编辑
- ⏳ Web 端控制界面

## 👥 干系人与需求

### 主要干系人
1. **最终用户**：追求简洁高效的视频下载工具
   - 需求：快速解析、清晰画质、易用界面
   
2. **开发者**：维护项目持续演进
   - 需求：良好代码结构、易于维护、可测试性
   
3. **合规要求**：遵守法律法规和平台政策
   - 需求：免责声明、合理使用提示、尊重robots.txt

### 用户需求优先级（MoSCoW）
- **Must have**: 
  - 粘贴链接即可解析下载
  - 支持至少 3 个平台无水印下载
  - 下载进度可见
  
- **Should have**:
  - 多清晰度选择
  - 批量下载队列
  - 元数据写入
  
- **Could have**:
  - 剪贴板自动识别
  - 夜间主题
  - 下载完成通知
  
- **Won't have (Phase 1)**:
  - 视频在线预览
  - 云端存储
  - 社交分享功能

## 🏛️ 架构设计概览

### 整体架构图

```
┌─────────────────────────────────────────────┐
│            Android Framework Layer          │
│  Clipboard | Notification | Storage Access │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│              Presentation Layer             │
│  ┌──────────────┐ ┌──────────────┐         │
│  │  Home Screen │ │Download List │         │
│  │ (Compose)    │ │  (Compose)   │         │
│  └──────────────┘ └──────────────┘         │
│  ┌──────────────────────────────┐          │
│  │        Settings Screen       │          │
│  └──────────────────────────────┘          │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│               Domain Layer                  │
│  ┌────────────────────────────────────┐    │
│  │         Use Cases                   │    │
│  │  • ParseVideoUrlUseCase             │    │
│  │  • DownloadVideoUseCase             │    │
│  │  • ManageDownloadsUseCase           │    │
│  └────────────────────────────────────┘    │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│             Data Layer                      │
│  ┌────────────┐ ┌────────────┐ ┌─────────┐│
│  │ Repository │ │   Worker   │ │ Database││
│  └────────────┘ └────────────┘ └─────────┘│
└─────────────────────────────────────────────┘
```

### 核心模块划分

#### 1. Parser Module（解析层）
**职责**: 针对各平台实现 URL 解析和视频信息提取

**子模块**:
- `platform.common`: 通用解析接口和基础类
- `platform.douyin`: 抖音解析器
- `platform.kuaishou`: 快手解析器  
- `platform.bilibili`: B 站解析器
- `platform.xiaohongshu`: 小红书解析器
- `platform.pipixia`: 皮皮虾解析器

**关键接口**:
```kotlin
interface VideoPlatformParser {
    val platformName: String
    fun canParse(url: String): Boolean
    suspend fun parse(videoUrl: String): Result<VideoInfo>
}
```

#### 2. Download Module（下载层）
**职责**: 管理视频下载任务的生命周期

**组件**:
- `DownloadManager`: 全局下载调度器
- `DownloadTaskWorker`: 单个任务的协程工作单元
- `DownloadQueueService`: 前台服务保证后台执行
- `ResumeDownloader`: 断点续传实现

#### 3. Database Module（数据层）
**职责**: 持久化下载记录和元数据

**实体**:
- `DownloadRecord`: 下载记录表
- `VideoFormatOption`: 可选清晰度配置表
- `UserPreferences`: 用户偏好设置表

#### 4. UI Module（表现层）
**职责**: Material Design 3 实现的各屏幕

**Screens**:
- `HomeScreen`: 输入区 + 最近下载
- `DownloadManagementScreen`: 队列管理 + 文件浏览
- `SettingsScreen`: 全局配置

## 📐 技术决策与约束

### 必须遵循的原则
1. **单例模式**: 关键组件（如 DownloadManager）必须是单例
2. **不可变性**: 状态管理采用 StateFlow，避免直接修改
3. **错误优先**: 所有网络操作使用 Result 类型返回
4. **线程隔离**: UI 在主线程，网络在 IO 线程，数据库在并发线程

### 必须使用的库
```kotlin
// HTTP 请求
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// 依赖注入（轻量级）
implementation("io.github.zhuangjw.modern:modern-di:1.0.0")

// JSON 解析
implementation("com.google.code.gson:gson:2.10.1")

// 数据库
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// 偏好设置
implementation("androidx.datastore:datastore-preferences:1.0.0")

// UI 框架
implementation("androidx.compose.material3:material3:1.2.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

### 禁止使用的模式
- ❌ Activity 启动旧版 XML 布局
- ❌ LiveData 替代 StateFlow
- ❌ 阻塞式网络调用
- ❌ 硬编码资源字符串

## 🚀 里程碑规划

### Phase 1: MVP (8-10 周)
- ✅ 基础框架搭建
- ✅ 抖音解析器实现
- ✅ 下载管理器开发
- ✅ UI 三 Tab 骨架
- 🔍 Beta 测试

### Phase 2: 功能完善 (4-6 周)
- ✅ 快手、B 站解析器
- ✅ 多清晰度选择界面
- ✅ 元数据写入功能
- 🔍 Closed Beta

### Phase 3: 体验优化 (3-4 周)
- ✅ 完整 5 平台支持
- ✅ 剪贴板集成
- ✅ 性能调优
- 🔍 Public Release

## ⚠️ 风险与应对策略

### 技术风险
| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| 平台 API 频繁变更 | 高 | 高 | 抽象解析器接口，插件化更新规则 |
| 签名验证机制 | 中 | 高 | 预留 HMAC 签名模拟能力 |
| IP 封禁限制 | 中 | 中 | 实现 User-Agent 轮换池 |

### 法律风险
| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| ToS 违反警告 | 高 | 低 | 明确免责声明，仅限个人使用 |
| 版权诉讼威胁 | 低 | 高 | 移除争议功能，配合 DMCA 请求 |
| 应用商店下架 | 中 | 中 | 准备合规变体版本 |

### 运营风险
- 用户量增长导致的需求膨胀 → 坚持 MVP 原则，严格控制范围
- 维护成本过高 → 建立社区反馈渠道，优先修复高频问题

## 📊 成功指标

### 技术指标
- 解析成功率 ≥ 95%（5 大平台综合）
- 平均解析时间 ≤ 2 秒
- 下载速度达到带宽上限的 80%+
- Crash-Free Rate ≥ 99.5%

### 业务指标
- 首批用户数：10,000+
- 日活跃用户率：≥ 30%
- Google Play 评分：≥ 4.5 星
- 用户留存率（30 天）：≥ 50%

## 🔄 后续迭代方向

### v1.1 规划（MVP 后）
- 支持更多平台（优酷、爱奇艺等）
- 添加视频压缩选项
- 深色模式优化

### v2.0 规划（6 个月后）
- 批量订阅与定时下载
- 跨设备同步（可选）
- 社区插件市场

### 长期愿景
成为 Android 平台上最值得信赖的纯本地视频下载解决方案，始终坚守"隐私优先、功能纯粹、体验优雅"的产品理念。

---

## 附录

### A. 参考项目分析
基于对 [SEAL](https://github.com/JunkFood02/Seal) 项目的研究：
- ✅ 借鉴其 Modern Android 架构经验
- ✅ 学习 Material Design 3 实现方式
- ❌ 避免依赖 yt-dlp 的体积负担
- ✅ 采用更轻量级的自研解析方案

### B. 术语对照表
| 英文 | 中文 | 说明 |
|------|------|------|
| Parser | 解析器 | 负责提取视频 URL |
| Manifest | 清单 | 描述视频元数据 |
| Bitrate | 码率 | 视频质量参数 |
| Codec | 编码器 | 编解码器类型 |
| Container | 容器 | MP4/MKV 格式 |

### C. 设计文档版本历史
- v1.0 (2025-01-09): Initial strategic design for initiative approval