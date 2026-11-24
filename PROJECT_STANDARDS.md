# CSC207 (Fall 2026) 团队项目开发与评分规范 (`PROJECT_STANDARDS.md`)

> 本文档依据 UofT Quercus 官方发布的 **CSC207H1F (Fall 2026) Team Project** 全部要求、里程碑、最新评分细则（`Project Rubric 2026 [Nov 15].pdf`）、Lab 指引及官方模板整理而成，作为本仓库开发、测试、Code Review、文档维护与最终答辩的统一项目规范。
> 完整官方原文归档见：[docs/quercus-project-requirements/INDEX.md](./docs/quercus-project-requirements/INDEX.md)。

---

## 目录

1. [评分构成与个人系数红线](#1-评分构成与个人系数红线)
2. [官方 100 分制评分标准细则](#2-官方-100-分制评分标准细则)
3. [项目里程碑与关键截止时间](#3-项目里程碑与关键截止时间)
4. [架构与技术实现硬性规范](#4-架构与技术实现硬性规范)
5. [测试覆盖率与代码质量规范](#5-测试覆盖率与代码质量规范)
6. [团队分工、Git 分支与 PR Code Review 规范](#6-团队分工git-分支与-pr-code-review-规范)
7. [README 与项目蓝图（MVP Blueprint）规范](#7-readme-与项目蓝图mvp-blueprint规范)
8. [最终答辩（Final Presentation）与 Demo 规范](#8-最终答辩final-presentation与-demo-规范)
9. [官方原文与模板索引](#9-官方原文与模板索引)

---

## 1. 评分构成与个人系数红线

### 1.1 总评占比与计算公式
- 团队项目占课程总成绩 **20%**。
- **个人最终项目成绩** = `团队答辩总分（满分 100 分）` × `个人调节系数（Individual Scale Factor，0.00 – 1.00）`。
- 正常履责的团队成员预期系数为 **`1.00`**（即获得完整团队分数）。

### 1.2 个人调节系数扣分规则（`Individual Scale Factor`）
个人系数初始为 `1.00`。最多叠加 **1 项团队协作扣分（`-0.05` 或 `-0.10`）** 与 **1 项代码贡献扣分（`-0.20` 或 `-1.00`）**，最低扣至 `0.00`：

| 扣分幅度 | 触发原因类别 | 官方典型示例（红线警示） |
| :---: | :--- | :--- |
| **`-0.05`** | 轻微协作问题（Minor Group Issue） | 多次无故缺席 Lab；Lab 期间参与度低；缺乏 PR Code Review；答辩中个人负责部分明显准备不足。 |
| **`-0.10`** | 严重协作问题（Major Group Issue） | 未按时完成约定节点；反复违反 `TeamContract.md`；长期不参与项目讨论、组会或 Lab。 |
| **`-0.20`** | 代码贡献异常（过少或包办） | **贡献不足**：合并的 PR 未实现本人负责 Use Case 的实质核心代码（仅改格式/小修小补/只写文档）；<br>**过度包办**：代替队友实现其负责的 Use Case，或未经沟通直接重写队友代码，破坏协作体验。 |
| **`-1.00`** | 无有效代码贡献（直接归零） | GitHub 仓库中无本人已合并的 PR，或对所负责的 Use Case 无任何实质性推进（项目计 **0 分**）。 |

### 1.3 期末个人必交项（截止：**12 月 8 日周二晚**）
1. **个人贡献反思（Individual Reflection，MarkUs 提交）**：最多 1 页，说明本学期个人在团队中的具体贡献（若存在贡献量偏少/偏多等特殊情况，需附上 GitHub PR/Commit 链接解释说明）。
2. **组内互评（Peer Evaluations）**：结合 [TeamContract.md](./TeamContract.md) 如实完成互评与自评。
3. **Clean Architecture Reflection Quiz（Quercus 在线测验）**：已延期至 **12 月 8 日（周二）** 截止。

---

## 2. 官方 100 分制评分标准细则

> 依据 11 月 15 日更新版正式评分表 `Project Rubric 2026 [Nov 15].pdf`（详见 [02_Project_Rubrics_2026.md](./docs/quercus-project-requirements/02_Project_Rubrics_2026.md)）。

| 评分项 | 分值 | 4/5 (Excellent) 核心达标线 | 5/5 (Exceptional) 满分冲刺标准 |
| :--- | :---: | :--- | :--- |
| **1. Presentation Materials**<br>（PPT 与视觉图表） | **15** | 包含全部必选要素，排版整洁无明显错误；全程有效运用架构图、UML 图、UI 截图等视觉元素。 | PPT 专业度极高；图表能把复杂架构与设计思想直观讲透。 |
| **2. Presentation Delivery**<br>（表达、排练与控时） | **15** | 节奏清晰流畅，讲解而非照念文字；**硬性门槛：总时长 <= 15 分钟，且每位组员必须发言**（否则最高 3/5）。 | 全组配合自信流畅，成员切换自然无缝，明显经过充分合练。 |
| **3. Demo**<br>（功能演示） | **10** | 测试数据与环境提前备好，端到端顺畅跑通团队 Use Cases，边操作边清晰解说每一步。 | 演示无缝衔接，清晰展现每位成员的 User Story，并与前文讲到的架构设计与 Use Cases 紧密呼应。 |
| **4. Question Answering**<br>（问答环节） | **5** | 准确回答评委（本组 TA + 另一位讲师/TA）提出的全部问题，无明显错误。 | 回答事实准确、简明扼要，展现出对项目代码细节与 CSC207 课程概念的深刻理解。 |
| **5. Specification**<br>（MVP / User Stories / Use Cases） | **10** | 清晰易懂；**严格聚焦软件“做什么（WHAT）”，绝不混入“怎么实现（HOW）”的技术细节**。 | 极其清晰直观，非程序员或完全不了解项目的人也能一眼看懂软件用途与交互目标。 |
| **6. Project Scope**<br>（项目规模与分工边界） | **5** | 规模适度且有挑战性；**每位成员至少有 1 个重要（Significant）且互不重叠的 User Story**。 | 项目完整度与深度显著超出课程预期，每位成员的 User Story 独立鲜明且分量充足。 |
| **7. API Usage**<br>（外部 API 集成） | **5** | 项目实际调用了 1 个或多个外部 API，并在 PPT 中具体说明 API 在项目中的调用方式与作用。 | 深度且合理地使用外部 API，API 与项目业务需求高度契合，讲解清晰透彻。 |
| **8. Data Persistence**<br>（数据持久化） | **5** | 实现了数据持久化，并在答辩中提供证据。 | 数据持久化清晰可见，且在演示中**同时明确展示数据的保存（Saving）与重新加载（Loading）**。 |
| **9. Clean Architecture**<br>（整洁架构遵守度） | **10** | 几乎完全遵守 CA 与依赖规则（Dependency Rule）；PPT 含恰当架构/UML 图提供强力证据；任何妥协/违规均被明确指出并合理辩护。 | 严格遵守 CA，无违反依赖规则现象；通过高质量架构图充分说服评委；任何极细微例外均有充分理由。 |
| **10. Overall Design**<br>（SOLID 与设计模式） | **10** | 深入讨论 **至少 2 条 SOLID 原则** 在项目中的实例；实现并清晰讲解 **至少 1 个 Starter Code 之外的设计模式**；清晰展示 **1 个可扩展性（Extensibility）设计实例**。 | 在满足 4/5 全部要求基础上，**恰当且深入地使用了至少 1 个课堂未讲授的设计模式**。 |
| **11. Code Quality**<br>（代码质量与工程流程） | **10** | 对合并入 `main` 的代码有严格质量把控；**硬性门槛：必须使用 Checkstyle（或同类静态检查工具）并提供使用证据**（无需清零所有警告，但必须有实际使用证据）。 | 在满足 4/5 基础上，深入展示团队代码质量保障流程，例如**展示一个具有代表性的高质量 Pull Request Review 实例**。 |
| **合计** | **100** | | |

---

## 3. 项目里程碑与关键截止时间

| 阶段 / 里程碑 | 对应 Lab / 日期 | 核心验收标准（Checklist） |
| :--- | :--- | :--- |
| **MarkUs 仓库登记** | Lab 8–9（尽快确认） | 由一名组员在 MarkUs 的 `GroupProject` 作业中提交团队 GitHub 仓库链接。 |
| **Milestone 1**<br>规划与初步开发 | Lab 7（Nov 9/10）<br>截止：**Fri Nov 13** | - GitHub 仓库创建并全员共享；<br>- `README.md` 完成 MVP Blueprint 定稿，明确最小核心 Use Cases 集合及每人的主导分工（每人 >= 1 个 MVP Use Case）；<br>- 核心 Entities 设计并实现；<br>- 各 Use Case 的 UI 与 Interactor 启动初步开发。 |
| **Milestone 2**<br>个人推进与可运行原型 | Lab 8（Nov 16/17）<br>截止：**Fri Nov 20** | - **每位成员**至少提交 **1 个实质性 PR**、至少 Review **1 个队友 PR**，并有可向 TA 演示的负责模块代码；<br>- 跑通外部 API 调用代码；<br>- 具备至少 1 个完整端到端 Use Case 的最小可运行原型。 |
| **Milestone 3**<br>准完整版原型 | Lab 9（Nov 23/24）<br>截止：**Fri Nov 27** | - **每个 `Interactor` 至少编写 1 个单元测试**；<br>- 全部 MVP 功能开发完成并集成进主程序；<br>- 项目达到准完整状态，向 TA 进行最终反馈前的 Demo（如时间冲突需在周三前填报答辩协调表）。 |
| **Milestone 4**<br>测试全覆盖与答辩打磨 | Lab 10（Nov 30/Dec 1）<br>截止：**Fri Dec 4** | - **`entity` 层与 `use_case` 层单元测试达到 100% 行覆盖率（100% Line Coverage）**；<br>- 修复遗留 Bug，程序达到答辩就绪状态；<br>- 完成答辩 PPT 制作与全组计时彩排。 |
| **Final Presentations**<br>最终现场答辩 | **Dec 7 / Dec 8 Labs** | - Lab 时间内现场答辩（~15 分钟讲解与 MVP 演示 + ~5 分钟问答），全员必须上台发言。 |
| **个人反思、互评与问卷** | 截止：**Tue Dec 8 23:59** | - MarkUs 提交个人贡献反思（<= 1 页）与 Peer Evaluations；<br>- Quercus 完成 Clean Architecture Reflection Quiz。 |

---

## 4. 架构与技术实现硬性规范

### 4.1 技术栈约束
- **GUI 框架**：必须使用 **Java Swing** 开发桌面图形界面（严禁做成 Web 应用；未经课程组特批不得改用 JavaFX）。
- **构建与测试**：使用 Maven 管理依赖与构建（当前仓库配置为 Java 11、JUnit 5、JaCoCo、SQLite JDBC、OkHttp、`org.json`）。

### 4.2 Clean Architecture 标准目录与包结构（兼容官方 CAVE 检查工具）
为严格遵守依赖规则（Dependency Rule）并兼容课程官方 **CAVE（Clean Architecture Visualizer for Education）** 静态检查工具（`cave verify` / `cave start`），`src/main/java` 必须保持以下顶层包划分，且**每个 Use Case 必须拥有独立的子包**：

```text
src/
├── main/
│   └── java/
│       ├── app/                  # 应用组装层（Main, AppBuilder, DataAccessBundle 等组装与依赖注入）
│       ├── entity/               # 核心业务实体与领域策略（最内层，零外层依赖）
│       ├── use_case/             # 用例业务逻辑层（按用例分包！）
│       │   ├── <use_case_1>/     #   包含 Interactor, InputBoundary, OutputBoundary,
│       │   ├── <use_case_2>/     #   InputData, OutputData, DataAccessInterface (DAI)
│       │   └── ...
│       ├── interface_adapter/    # 接口适配层（Controller, Presenter, ViewModel, State）
│       ├── data_access/          # 数据访问与外部 API 实现层（SQLite DAO, In-Memory DAO, HTTP Gateway）
│       └── view/                 # Swing 视图层（JPanel/JFrame UI 组件，监听用户事件与 ViewModel 变更）
└── test/
    └── java/                     # 与 main 镜像对应的单元测试、覆盖率测试与架构依赖检查测试
```

### 4.3 依赖方向铁律（Dependency Rule）
1. **`entity` 层**：只包含纯粹的领域对象、业务不变量与领域规则接口，**严禁 `import` 任何 `use_case`、`interface_adapter`、`data_access`、`view`、`app` 或第三方持久化/UI 库**。
2. **`use_case` 层**：`Interactor` 只能依赖 `entity` 以及同层定义的接口（`InputBoundary`、`OutputBoundary`、`*DataAccessInterface`）和纯数据载体（`InputData`、`OutputData`），**严禁直接引用具体的 `Controller`、`Presenter`、`ViewModel`、`DAO` 实现类或 Swing 组件**。
3. **`interface_adapter` 层**：`Controller` 负责组装 `InputData` 调用 `InputBoundary.execute(...)`；`Presenter` 实现 `OutputBoundary`，将 `OutputData` 转换为 `ViewModel` / `State` 更新并触发属性变更通知（`PropertyChangeSupport`），**不直接操作 Swing 控件**。
4. **`view` 层与 `data_access` 层**：处于最外层细节层。`View` 监听 `ViewModel` 变化刷新界面并将用户输入交给 `Controller`；`DAO` / `Gateway` 实现 `use_case` 层定义的 `*DataAccessInterface`（DAI），封装 SQLite、本地文件或外部 HTTP API 调用细节。
5. **`app` 层（Main Component）**：唯一允许知晓所有具体实现类并完成构造函数注入与界面组装的“脏”组装层（如 `AppBuilder`）。

> **本地自检命令**：
> - 运行仓库内置架构依赖测试：`mvn test -Dtest=CleanArchitectureDependencyTest`
> - （可选）使用官方 CAVE 工具检查：`npm install -g clean-architecture-visualizer@latest` 后在项目根目录执行 `cave verify` 或 `cave start`。

### 4.4 单个 Use Case 标准开发 17 步清单
每次新增或完善一个 MVP Use Case 时，按以下官方推荐清单逐项核对：
- [ ] 1. 完成 Use Case 文本规约（明确主流程与异常分支）
- [ ] 2. 绘制执行前后的 UI 草图（Before / After View Sketch）
- [ ] 3. 创建或更新所需的 `Entity` 类及其领域校验规则
- [ ] 4. 定义 `InputData` 类（封装用户输入参数）
- [ ] 5. 定义 `OutputData` 类（封装用例输出结果）
- [ ] 6. 梳理所需的数据读写/外部服务操作
- [ ] 7. 在 `use_case/<name>/` 下定义 `*DataAccessInterface`（DAI）
- [ ] 8. 编写轻量级 `InMemory` DAO / Mock 以便脱离外部环境独立测试
- [ ] 9. 编写 `Interactor` 单元测试（先定行为规约）
- [ ] 10. 定义 `InputBoundary`（统一推荐使用 `execute` 方法名）与 `OutputBoundary`，实现 `Interactor`
- [ ] 11. 补全失败分支、空值与边界条件的单元测试，确保覆盖全部路径
- [ ] 12. 实现或更新 `Controller`
- [ ] 13. 在 `View` 中绑定触发控件（按钮/表单）并调用 `Controller`
- [ ] 14. 实现 `Presenter`（实现 `OutputBoundary`）
- [ ] 15. 定义或更新对应的 `State` 与 `ViewModel`
- [ ] 16. 更新 `View` 监听 `ViewModel` 属性变更以刷新界面或弹出提示
- [ ] 17. 在 `AppBuilder` 中完成装配并端到端联调验证

### 4.5 SOLID、设计模式、API 与持久化硬性要求
1. **SOLID 原则**：代码中需有至少 **2 个清晰的 SOLID 原则落地实例**（如 DIP 依赖倒置隔离 DAO/Gateway、SRP 单一职责拆分 Interactor、OCP 开闭原则支持策略扩展、ISP 按用例细分 DAI 接口等），并在答辩中重点展示。
2. **设计模式（Design Patterns）**：除了课程 Starter Code 自带的 Observer / MVC / Builder / Simple Factory 外，**必须主动引入至少 1 个贴合业务场景的设计模式**（例如 Strategy 评分/推荐策略、Adapter 外部 API 适配、Facade、Decorator、Command 等；若引入**课堂未讲授的设计模式**且运用得当，可冲击 Overall Design 满分 5/5）。
3. **外部 API 集成**：必须真实调用至少 1 个外部 API，注意处理网络超时、限流（Rate Limit）及 API Key 安全（敏感 Key 放入本地环境变量或 `.env`，严禁硬编码提交至 GitHub）。
4. **数据持久化**：必须支持跨会话持久化（如 SQLite 数据库），且程序启动加载与操作保存必须在 Demo 中可直观验证。

---

## 5. 测试覆盖率与代码质量规范

### 5.1 单元测试与 100% 覆盖率目标
- **硬性指标**：Milestone 4（12 月 4 日前）**`entity` 包与 `use_case` 包必须达到 100% 行覆盖率（Line Coverage）**。
- **测试原则**：
  - `Interactor` 单元测试必须独立于真实 Swing UI、真实 SQLite 文件及外部网络 API（使用 `InMemory` DAO 或测试替身 / Fake Presenter 验证成功视图、失败视图及状态变更）。
  - 同时覆盖正常主流程（Happy Path）与全部异常/校验失败分支（Fail View）。
- **验证方式**：运行 `mvn clean test`，查看 `target/site/jacoco/index.html` 中 `entity` 与 `use_case` 各包的覆盖率统计。

### 5.2 Checkstyle 静态检查与代码规范（拿 80%+ 质量分的硬门槛）
- **硬性指标**：官方评分表规定，Code Quality 想要获得 **4/5 或 5/5**，**必须使用 Checkstyle（或同类静态检查工具）并在答辩中提供使用证据**。
- **执行规范**：
  - 在 Maven 或 IDE（IntelliJ Checkstyle-IDEA 插件）中启用课程 Checkstyle 规则进行检查，保留运行截图或 CI/Maven 检查记录作为答辩素材。
  - 遵循 Java 命名规范（类名 `UpperCamelCase`，方法/变量 `lowerCamelCase`，常量 `UPPER_SNAKE_CASE`，包名小写下划线 `use_case` / `interface_adapter` / `data_access`）。
  - 为公共类、接口及核心方法编写清晰的 Javadoc 注释（说明职责、参数、返回值与前置/后置条件）。
  - 严禁将 `.db`、`.ics`、`.env`、`target/`、IDE 个人配置等运行产物提交到 Git 仓库（已在 [.gitignore](./.gitignore) 中配置忽略）。

---

## 6. 团队分工、Git 分支与 PR Code Review 规范

### 6.1 个人主导分工要求（"Take the Lead"）
每位团队成员必须在 MVP 中**主导（Take the Lead）至少 1 个完整的 Use Case**（对应至少 1 个独立、不重叠的 User Story），即亲自编写该用例绝大部分的：
1. `Use Case Interactor` 及其关联的 `InputBoundary` / `OutputBoundary` / `InputData` / `OutputData` / `DAI`；
2. 该用例专属的 `Controller` 与 `Presenter`；
3. 触发该用例的 `View` 交互入口及执行后的界面状态更新；
4. 该用例对应的单元测试。

### 6.2 Git 分支与 Pull Request 工作流
1. **分支开发**：功能开发、重构与 Bug 修复均应在语义化特性分支上进行（例如 `feat/<use-case-name>`、`fix/<issue>`、`test/<module>`、`docs/<topic>`），避免多人直接在 `main` 上冲突开发。
2. **Pull Request 规范**：
   - 每个 PR 聚焦单一明确的模块或用例变更，附上清晰的 PR 描述（改动摘要、关联的 User Story / Use Case、测试验证结果、如有必要同步更新 `README.md`）。
   - 严禁未经沟通修改或覆盖其他成员负责的核心 Use Case 代码。
3. **Code Review 检查清单（官方要求全员参与）**：
   每位成员都必须在 GitHub 上对队友的 PR 进行有实质内容的 Code Review，重点检查以下 6 项并给出**具体、可执行（Specific & Actionable）**的改进意见：
   - **Coding Style**：是否符合 Checkstyle 代码规范与命名约定；
   - **Correctness**：业务逻辑与边界条件是否正确；
   - **Tests & Coverage**：是否包含充分的单元测试并维持高覆盖率；
   - **Clean Architecture + SOLID**：是否违反依赖规则（如内层反向依赖外层）；
   - **Javadoc**：类与方法文档注释是否清晰完整；
   - **PR Clarity**：PR 说明与相关 `README.md` 更新是否清楚。
   > **满分技巧**：精心打磨并保留 1–2 个包含 Checkstyle 修正讨论与架构改进建议的典型 PR，直接截图放进最终答辩 PPT 的 `Code Quality` 页以冲刺 5/5！

---

## 7. README 与项目蓝图（MVP Blueprint）规范

根目录 [README.md](./README.md) 必须随项目推进持续更新（建议在每次重要功能 PR 中同步维护），并完整包含以下官方规定要素（可参考 [project-readme-template-2026.md](./docs/quercus-project-requirements/project-readme-template-2026.md)）：

1. **项目名称与团队成员名单（Project Name & Team Members）**
2. **项目领域与简介（Project Domain & Specification Summary）**
   - 用 1–2 段清晰文字概括软件解决什么问题、面向什么用户、提供什么核心价值。
   - **注意**：Specification 描述务必聚焦 **WHAT（用户能做什么）**，不要堆砌底层类名、SQL 表结构等 **HOW（实现细节）**。
3. **用户故事列表（User Stories）**
   - 采用标准句式：`As a [role], I want to [action] so that [benefit].`
   - 确保每位成员至少对应 1 个独立、有分量的 User Story。
4. **用例列表与 MVP 负责人映射表（Use Cases & MVP Lead Table）**
   - 明确区分哪些 Use Cases 属于承诺交付的 **MVP**，并列出 `Team Member <-> Lead MVP Use Case(s)` 对应表。
5. **领域实体说明（Entities）**
   - 至少 2 个核心领域实体，列出各 Entity 的名称、核心属性与关键方法（或附上 PlantUML / Mermaid 类图）。
6. **外部 API 说明（API Usage）**
   - 包含 API 名称、官方文档链接、在项目中的具体用途说明，以及是否已调通的状态说明。
7. **设计图表与运行指南（Design Diagrams, Screenshots & How to Run）**
   - 包含核心 UML 类图 / 时序图、当前功能界面截图或动图，以及本地编译启动命令与环境配置说明。

---

## 8. 最终答辩（Final Presentation）与 Demo 规范

- **时间与形式**：12 月 7 日 / 8 日 Lab 时段现场进行；**正片严格控制在 15 分钟以内** + 最多 5 分钟评委提问（Q&A）。
- **全员发言铁律**：**每位团队成员都必须上台发言**，且建议在每页 PPT 上标注主讲人姓名。
- **官方 PPT 模板推荐结构与配时（合计 ~15 分钟）**（详见 [03_Templates_and_Attachments.md](./docs/quercus-project-requirements/03_Templates_and_Attachments.md)）：

| 顺序 | 模块主题 | 建议时长 | 关键得分要点 |
| :---: | :--- | :---: | :--- |
| 1 | **User Stories / MVP 功能概览** | ~1 min | 简明扼要讲清楚软件 **做什么（WHAT）**，切勿大段念字或讲底层实现。 |
| 2 | **API Usage（外部 API）** | ~30 sec | 展示所用外部 API 及其在业务中的具体调用场景与作用。 |
| 3 | **Data Persistence（数据持久化）** | ~30 sec | 说明持久化了哪些数据（并在本页或 Demo 中明确演示保存与重启加载）。 |
| 4 | **Design & Use Case Walkthrough**<br>（架构、SOLID、设计模式与用例走查） | ~6 min | - **SOLID**：精讲项目中 **至少 2 个** 具体原则实例；<br>- **Design Pattern**：精讲团队引入的 **至少 1 个** 非 Starter Code 设计模式及可扩展性设计；<br>- **Use Case Walkthrough**：选 1 个代表性用例展示 **执行前后 UI 对比 -> Clean Architecture UML 类图 -> Interactor 核心代码与控制流**（多用图表少贴大段代码）。 |
| 5 | **Functionality Demo（核心功能演示）** | ~6 min | 提前录入测试数据，聚焦最精彩的核心业务与每位成员的 User Story（少在普通注册/改密码上耗费时间）；可录屏防翻车，但**现场必须备好可随时运行的程序**以应对 Q&A 现场点名演示。 |
| 6 | **Code Quality（代码质量保障）** | ~1 min | 展示 **Checkstyle 实际使用证据** + 团队 PR Code Review 流程与代表性 PR 截图。 |

---

## 9. 官方原文与模板索引

本仓库 [`docs/quercus-project-requirements/`](./docs/quercus-project-requirements/) 目录下完整保存了 Quercus 上的全部原始资料供随时查阅核对：

- [INDEX.md](./docs/quercus-project-requirements/INDEX.md) — 归档资料总目录
- [01_Team_Project_Core_Pages.md](./docs/quercus-project-requirements/01_Team_Project_Core_Pages.md) — 全部 15 个核心项目页面原文（Overview、Expectations、Timeline、Scale Factor 等）
- [02_Project_Rubrics_2026.md](./docs/quercus-project-requirements/02_Project_Rubrics_2026.md) — 11 月 15 日正式版 100 分制评分表、Lab 评分总结 PPT 全文及旧版评分表
- [03_Templates_and_Attachments.md](./docs/quercus-project-requirements/03_Templates_and_Attachments.md) — 官方答辩 PPT 模板（16 页全文 + 示例页截图与代码转录）及 README 模板
- [project-readme-template-2026.md](./docs/quercus-project-requirements/project-readme-template-2026.md) — 官方独立 Markdown README 模板文件
- [04_Clean_Architecture_Use_Case_and_API_Guides.md](./docs/quercus-project-requirements/04_Clean_Architecture_Use_Case_and_API_Guides.md) — CAVE 架构检查指南、Use Case 实现步骤清单、View 设计指南与 API 经验汇总
- [05_Lab_Instructions_and_Weekly_Resources.md](./docs/quercus-project-requirements/05_Lab_Instructions_and_Weekly_Resources.md) — Labs 2–9 项目推进实验说明原文
- [06_Project_Announcements_and_Syllabus.md](./docs/quercus-project-requirements/06_Project_Announcements_and_Syllabus.md) — 全部项目相关课程公告、周计划与课程大纲原文
