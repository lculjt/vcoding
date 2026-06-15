# Codex Git 提交信息规则

Codex 生成 Git commit message 时必须遵守以下规则。

## 语言与格式

- 必须使用简体中文，禁止英文标题或英文正文。
- 格式必须是 `<type>: <中文简述>`。
- 优先使用 1 行，控制在 50 字以内；复杂变更最多 2 行。
- `type` 只能使用：`feat`、`fix`、`chore`、`docs`、`refactor`、`test`、`style`。
- 简述要写清楚“做了什么 + 影响”，不要堆砌文件名，不要用英文动词开头。
- 多个独立改动可以用 `/` 连接。

## type 选用

- `feat`：新功能、用户可感知的能力增强。
- `fix`：修复 bug、回归、权限、编译或交互异常。
- `refactor`：不改变行为的代码重构。
- `chore`：依赖、构建、CI 或配置调整。
- `docs`：仅文档变更。
- `test`：仅测试变更。
- `style`：仅格式或样式调整，不涉及逻辑变化。

## 示例

```text
feat: 新增表单
fix: 修复表单验证问题
chore: 使用 yarn 管理依赖
```

## 禁止

- 禁止使用 `fix: resolve permission issue`。
- 禁止使用 `feat: add xxx component`。
- 禁止使用 `Update files`、`WIP`、`fix bug` 等模糊信息。
- 禁止在冒号后使用英文；`type` 前缀本身可以保留英文。

## 准确性

- 提交信息必须根据 staged diff 生成。
- 必须准确反映实际变更，不得夸大、遗漏主因或描述未提交内容。
