# InvestLedger - 投资账本

简洁的投资记账安卓应用，用于记录建仓、清仓和统计收益。

## 功能特点

### Phase 1 MVP 核心功能

- ✅ **建仓记录**：买入时记录成本价、数量、投资类型
- ✅ **清仓计算**：卖出时自动计算收益和收益率
- ✅ **持仓列表**：显示当前持有的投资
- ✅ **收益统计**：历史收益汇总、胜率、盈利/亏损统计

### 设计特点

- 🎨 **ChatGPT 简洁风格**：干净、现代、极简
- 📦 **轻量级**：APK < 10MB（启用 ProGuard 和资源压缩）
- 🔒 **本地存储**：Room 数据库，无需联网
- ✍️ **手动输入**：无需实时行情 API

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3
- **架构**：MVVM + Repository
- **数据库**：Room
- **导航**：Compose Navigation

## 项目结构

```
InvestLedger/
├── app/
│   ├── src/main/
│   │   ├── java/com/investledger/
│   │   │   ├── data/              # Room 数据层
│   │   │   │   ├── Position.kt    # 持仓实体
│   │   │   │   ├── Transaction.kt # 交易记录实体
│   │   │   │   ├── PositionDao.kt # 持仓 DAO
│   │   │   │   ├── TransactionDao.kt # 交易 DAO
│   │   │   │   └── AppDatabase.kt # 数据库
│   │   │   ├── ui/                # Compose UI 层
│   │   │   │   ├── theme/         # 主题配置
│   │   │   │   └── screens/       # 各功能屏幕
│   │   │   ├── viewmodel/         # ViewModel 层
│   │   │   └── MainActivity.kt    # 主活动
│   │   └── res/                   # 资源文件
│   └── build.gradle.kts           # 模块构建配置
├── build.gradle.kts               # 项目构建配置
└── settings.gradle.kts            # 项目设置
```

## 构建说明

### 前置要求

- Android Studio Arctic Fox 或更高版本
- JDK 17+
- Android SDK 34

### 构建步骤

1. **导入项目**
   ```bash
   # 在 Android Studio 中打开 InvestLedger 目录
   ```

2. **同步 Gradle**
   ```bash
   ./gradlew build
   ```

3. **构建 APK**
   ```bash
   ./gradlew assembleRelease
   ```
   
   APK 输出位置：`app/build/outputs/apk/release/app-release.apk`

### 开发构建

```bash
# 调试版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 使用指南

### 建仓（买入）

1. 点击右下角的 "+" 按钮
2. 输入：
   - 投资名称/代码
   - 投资类型（股票、基金、加密货币等）
   - 成本价
   - 数量
   - 备注（可选）
3. 点击"确定"

### 清仓（卖出）

1. 在持仓列表中找到要清仓的项目
2. 点击"清仓"按钮
3. 输入卖出价
4. 预览收益和收益率
5. 点击"确定清仓"

### 查看统计

- **持仓页面**：显示当前持仓和总成本
- **记录页面**：显示历史交易记录和累计收益
- **统计页面**：显示胜率、总盈利、总亏损等详细统计

## 未来计划

Phase 2 功能：
- 导出数据（CSV/Excel）
- 数据导入
- 多账户支持
- 图表可视化
- 云端备份

## 许可证

MIT License

---

简洁高效，专注记账。无联网，无广告，无隐私担忧。