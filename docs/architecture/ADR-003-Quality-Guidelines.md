# ADR-003: 质量保障机制

## 状态

已替代 (Superseded by ADR-004) - 2025-10-17

⚠️ **本决策已被废弃**：项目已迁移到 Kotlin Multiplatform，将使用 Kotlin 生态的质量保障工具（Gradle, ktlint, detekt 等）。详见 [ADR-004: Migration to Kotlin Multiplatform](./ADR-004-Migration-to-KMP.md)。

## 日期

2025-01-16

## 背景 (Context)

作为一款移动应用，Audiofy 需要建立完善的质量保障机制，确保：

1. **代码质量**：统一的代码风格、无 Lint 错误、类型安全
2. **功能正确性**：核心功能（翻译、TTS、播放）必须可靠
3. **用户体验**：无崩溃、无明显性能问题
4. **可维护性**：代码清晰、易于调试和扩展

### 质量挑战

- **移动应用调试困难**：不能像 Web 应用那样直接用浏览器 DevTools
- **跨平台差异**：iOS 和 Android 行为可能不一致
- **异步操作多**：网络请求、文件 I/O、音频播放都是异步的，容易出现竞态条件
- **个人项目资源有限**：需要平衡质量和开发速度

## 决策 (Decision)

我们将采用 **实用主义 TDD 原则** + **自动化工具**的质量保障策略，核心是：

1. **代码质量由工具自动保证**（ESLint + Prettier + Git Hooks）
2. **核心功能必须测试**（单元测试 + E2E 测试）
3. **CI/CD 自动运行检查**（GitHub Actions）
4. **覆盖率是底线而非目标**（>70% 足够）

### 代码风格统一

#### 工具选择

- **Prettier**: 自动格式化代码（缩进、引号、分号等）
- **ESLint**: 静态代码分析（捕获潜在 Bug、强制最佳实践）
- **TypeScript**: 类型检查（编译时发现类型错误）

#### 配置文件

**.prettierrc.json**

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "arrowParens": "always"
}
```

**.eslintrc.js**

```javascript
module.exports = {
  root: true,
  env: {
    node: true,
    es2021: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:vue/vue3-recommended',
    'prettier', // 必须放在最后，禁用与 Prettier 冲突的规则
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2021,
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint', 'vue'],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    '@typescript-eslint/no-explicit-any': 'warn',
    'vue/multi-word-component-names': 'off',
  },
}
```

**tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2021",
    "module": "ESNext",
    "moduleResolution": "node",
    "lib": ["ES2021", "DOM"],
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true,
    "skipLibCheck": true,
    "esModuleInterop": true
  },
  "include": ["src/**/*.ts", "src/**/*.vue"],
  "exclude": ["node_modules", "platforms"]
}
```

### 测试策略

#### 实用主义 TDD 原则

**必须测试的场景**：

- ✅ 核心业务逻辑（翻译、TTS、音频存储）
- ✅ 复杂算法（如音频文件路径生成、元数据更新）
- ✅ 边界条件（空输入、网络失败、存储空间不足）
- ✅ 错误处理（API 限流、文件读写失败）

**可以不测试的场景**：

- ❌ 简单的数据模型（Article 接口定义）
- ❌ 纯展示组件（只有模板、无逻辑）
- ❌ 第三方库的功能（假设 nativescript-audio 已测试）

**覆盖率目标**：>70%（核心业务逻辑 >90%）

#### 测试框架

- **单元测试**: Vitest（Vue 官方推荐，比 Jest 快 10 倍）
- **E2E 测试**: Appium（可选，MVP 阶段手动测试优先）

#### 测试示例

**单元测试：TranslateService**

```typescript
// src/services/__tests__/TranslateService.spec.ts
import { describe, it, expect, vi } from 'vitest'
import { TranslateService } from '../TranslateService'

describe('TranslateService', () => {
  it('should translate English to Chinese', async () => {
    // Arrange
    const service = new TranslateService()
    const originalText = 'Hello world'

    // Act
    const translatedText = await service.translate(originalText)

    // Assert
    expect(translatedText).toBe('你好世界')
  })

  it('should handle API errors gracefully', async () => {
    // Arrange
    const service = new TranslateService()
    vi.spyOn(global, 'fetch').mockRejectedValueOnce(new Error('Network error'))

    // Act & Assert
    await expect(service.translate('test')).rejects.toThrow('翻译失败，请检查网络连接')
  })

  it('should handle empty input', async () => {
    // Arrange
    const service = new TranslateService()

    // Act & Assert
    await expect(service.translate('')).rejects.toThrow('输入文本不能为空')
  })
})
```

**单元测试：StorageService**

```typescript
// src/services/__tests__/StorageService.spec.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { StorageService } from '../StorageService'

describe('StorageService', () => {
  let service: StorageService

  beforeEach(() => {
    service = new StorageService()
    service.clear() // 清空测试数据
  })

  it('should save article metadata', async () => {
    // Arrange
    const article = {
      id: 'test-uuid',
      title: 'Test Article',
      originalText: 'Hello',
      translatedText: '你好',
      audioPath: 'audios/test-uuid.wav',
      createdAt: new Date(),
      duration: 10,
      status: 'completed' as const,
    }

    // Act
    await service.saveArticle(article)
    const savedArticle = await service.getArticle('test-uuid')

    // Assert
    expect(savedArticle).toEqual(article)
  })

  it('should return all articles sorted by creation time', async () => {
    // Arrange
    const article1 = { /* ... */ createdAt: new Date('2025-01-15') }
    const article2 = { /* ... */ createdAt: new Date('2025-01-16') }
    await service.saveArticle(article1)
    await service.saveArticle(article2)

    // Act
    const articles = await service.getAllArticles()

    // Assert
    expect(articles[0].createdAt).toEqual(article2.createdAt) // 最新的在前
  })
})
```

### 本地质量门槛（Git Hooks）

#### 工具选择

使用 **Husky** 配置 Git Hooks（NativeScript 项目推荐 Husky，而非 pre-commit 框架）

#### 配置步骤

**安装 Husky**

```bash
npm install --save-dev husky lint-staged
npx husky install
```

**package.json**

```json
{
  "scripts": {
    "prepare": "husky install",
    "lint": "eslint src --ext .ts,.vue --fix",
    "format": "prettier --write \"src/**/*.{ts,vue,json}\"",
    "test": "vitest run",
    "test:watch": "vitest",
    "type-check": "tsc --noEmit"
  },
  "lint-staged": {
    "*.{ts,vue}": ["eslint --fix", "prettier --write", "vitest related --run"],
    "*.{json,md}": ["prettier --write"]
  }
}
```

**.husky/pre-commit**

```bash
#!/usr/bin/env sh
. "$(dirname -- "$0")/_/husky.sh"

npx lint-staged
```

#### Git Hooks 触发时机

- **pre-commit**: 运行 ESLint、Prettier、相关测试
- **pre-push**（可选）: 运行完整测试套件 + 类型检查

### CI/CD 质量门槛（GitHub Actions）

#### 配置文件

**.github/workflows/ci.yml**

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  quality-check:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run ESLint
        run: npm run lint

      - name: Run Prettier check
        run: npx prettier --check "src/**/*.{ts,vue,json}"

      - name: Run TypeScript type check
        run: npm run type-check

      - name: Run tests
        run: npm run test

      - name: Upload coverage report
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/coverage-final.json
          fail_ci_if_error: false

  build-ios:
    runs-on: macos-latest
    needs: quality-check

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install NativeScript CLI
        run: npm install -g @nativescript/cli

      - name: Install dependencies
        run: npm ci

      - name: Build iOS
        run: ns build ios --release --for-device

  build-android:
    runs-on: ubuntu-latest
    needs: quality-check

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Install NativeScript CLI
        run: npm install -g @nativescript/cli

      - name: Install dependencies
        run: npm ci

      - name: Build Android
        run: ns build android --release
```

#### CI 质量门槛

- ✅ ESLint 无错误
- ✅ Prettier 格式检查通过
- ✅ TypeScript 类型检查通过
- ✅ 测试覆盖率 >70%
- ✅ iOS 和 Android 构建成功

### 代码审查规范

#### Pull Request 模板

**.github/pull_request_template.md**

```markdown
## 变更描述

请简要描述此 PR 的变更内容。

## 变更类型

- [ ] 🐛 Bug 修复（fix）
- [ ] ✨ 新功能（feat）
- [ ] 📝 文档更新（docs）
- [ ] 🎨 代码重构（refactor）
- [ ] ✅ 测试相关（test）
- [ ] 🔧 构建/工具/依赖更新（chore）

## 检查清单

- [ ] 代码已通过 ESLint 和 Prettier 检查
- [ ] 已添加/更新相关测试
- [ ] 测试覆盖率未下降
- [ ] 已更新相关文档（如有必要）
- [ ] 在 iOS 和 Android 模拟器上测试通过

## 相关 Issue

Closes #<issue_number>

## 测试截图/视频（可选）

如果是 UI 变更，请提供截图或视频。
```

#### Review 规则

- **必须 Review**: 所有 PR 都必须经过代码审查
- **Approve 人数**: 1 人（个人项目）
- **Review 重点**:
  - 是否符合架构设计（ADR-002）
  - 是否有明显的性能问题
  - 错误处理是否完善
  - 测试是否覆盖核心逻辑

## 备选方案 (Alternatives Considered)

### 备选方案 A: 不使用 Git Hooks，只依赖 CI

- **描述**: 开发者本地不运行检查，所有质量检查都在 CI 中进行
- **优势**:
  - 开发者提交速度快（不需要等待本地检查）
  - 配置简单（只需维护 CI 配置）
- **为何未选择**:
  - 提交后才发现 Lint 错误，需要额外的修复 commit
  - 浪费 CI 资源（每次提交都要运行完整检查）
  - 降低代码质量（开发者容易提交未格式化的代码）

### 备选方案 B: 100% 测试覆盖率

- **描述**: 要求所有代码都必须有测试覆盖
- **优势**:
  - 理论上更安全
  - 重构时信心更足
- **为何未选择**:
  - **过度测试浪费时间**：简单的 getter/setter 不需要测试
  - **覆盖率不等于质量**：100% 覆盖率不代表测试用例设计得好
  - **Linus 原则**："覆盖率是底线，不是目标"
  - 70-80% 覆盖率是业界公认的性价比最高的目标

### 备选方案 C: 使用 Jest 而非 Vitest

- **描述**: 使用更成熟的 Jest 测试框架
- **优势**:
  - 社区最大，文档和教程最丰富
  - 插件生态完善
- **为何未选择**:
  - Vitest 是 Vue 官方推荐（与 Vite 原生集成）
  - Vitest 性能更好（启动速度快 10 倍）
  - Vitest API 与 Jest 兼容，迁移成本低

## 后果 (Consequences)

### 正面影响

1. **代码质量稳定**：自动化工具保证代码风格一致、无低级错误
2. **重构信心足**：测试覆盖核心逻辑，重构时不用担心破坏功能
3. **CI/CD 可靠**：所有 PR 都经过严格检查，main 分支始终可部署
4. **团队协作顺畅**：统一的代码风格和 PR 模板减少沟通成本
5. **Bug 发现早**：Git Hooks 在提交前就发现 Lint 错误，节省时间

### 负面影响与缓解措施

1. **提交速度变慢**
   - **影响**：Git Hooks 运行 Lint + 格式化 + 测试，可能耗时 10-30 秒
   - **缓解**：使用 `lint-staged` 只检查改动的文件；开发时使用 `git commit --no-verify` 跳过（不推荐）

2. **学习成本增加**
   - **影响**：需要学习 Vitest、ESLint 配置、Git Hooks
   - **缓解**：提供完整的配置模板和示例代码；团队培训 1-2 小时

3. **测试维护成本**
   - **影响**：代码变更时需要同步更新测试
   - **缓解**：采用实用主义 TDD，只测试核心逻辑；使用 `vitest --watch` 实时反馈

### 所需资源

- **工具安装**：30 分钟（安装 Husky、ESLint、Prettier、Vitest）
- **CI/CD 配置**：1 小时（配置 GitHub Actions）
- **编写测试**：与开发同步进行（TDD 模式）
- **代码审查时间**：平均每个 PR 10-20 分钟

## 相关决策

- ADR-001: 技术栈选型（NativeScript-Vue + TypeScript）
- ADR-002: 架构设计（三层架构便于单元测试）

## 参考资料

- [Vitest 官方文档](https://vitest.dev/)
- [ESLint 官方文档](https://eslint.org/)
- [Prettier 官方文档](https://prettier.io/)
- [Husky 官方文档](https://typicode.github.io/husky/)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [NativeScript 测试指南](https://docs.nativescript.org/guide/testing)
