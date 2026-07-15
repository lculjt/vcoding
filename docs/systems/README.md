# 业务系统需求文档索引

本目录用于沉淀平台内各业务系统的产品规划、需求计划、竞品分析和后续阶段方案。

## 文档组织规则

每个业务系统单独占用一个目录，后续二期、三期计划也放在对应系统目录下。

```text
docs/systems/
├── ai-content-platform/
│   ├── README.md
│   ├── phase-1-requirement-plan.md
│   └── competitor-analysis.md
├── global-trend-monitor/
│   ├── README.md
│   ├── phase-1-requirement-analysis.md
│   ├── phase-1-task-plan.md
│   └── technical-design.md
└── ai-product-suite/
    ├── README.md
    └── dual-system-roadmap.md
```

## 当前文档

### AI 产品套件

- [AI 双系统产品规划](ai-product-suite/dual-system-roadmap.md)

该文档是顶层路线图，只描述 Agent 工作台、AI 内容平台和共享 AI 能力层之间的关系、阶段顺序和边界，不维护内容平台的一期详细字段、接口和页面清单。

### AI 内容生产与分发平台

- [一期需求计划书](ai-content-platform/phase-1-requirement-plan.md)
- [竞品分析](ai-content-platform/competitor-analysis.md)

其中，一期需求计划书是 AI 内容平台一期的唯一详细需求源。涉及一期页面、字段、数据表、接口、里程碑和验收标准时，以该文档为准。

### 海外热点聚合观察台

- [一期需求分析文档](global-trend-monitor/phase-1-requirement-analysis.md)
- [一期任务文档](global-trend-monitor/phase-1-task-plan.md)
- [一期技术设计文档](global-trend-monitor/technical-design.md)

该目录用于维护海外热点聚合观察台的一期需求、任务拆分、技术设计、数据源建议、平台接入边界和验收标准。

## 维护原则

- 顶层路线图只写系统关系、阶段路线和边界。
- 子系统需求文档写具体页面、字段、表结构、接口和验收标准。
- 竞品分析只沉淀市场、竞品、差异化和产品策略，不承担实施计划。
- 如果不同文档内容冲突，以更具体的子系统阶段需求文档为准。
