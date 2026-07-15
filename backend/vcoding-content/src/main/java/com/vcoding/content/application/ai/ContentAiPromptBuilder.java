package com.vcoding.content.application.ai;

import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 根据选题上下文构建 AI Prompt。Prompt 模板先内置在内容模块，后续可迁移到 Agent 模板中心。
 */
@Component
public class ContentAiPromptBuilder {

    public String build(TopicEntity topic, GenerationTaskType taskType) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是专业的中文内容创作助手。请基于以下选题信息完成任务。\n\n");
        builder.append("【选题标题】").append(topic.getTitle()).append('\n');
        appendLine(builder, "内容方向", topic.getContentDirection());
        appendLine(builder, "目标受众", topic.getTargetAudience());
        appendLine(builder, "核心关键词", topic.getKeywords());
        appendLine(builder, "目标平台", topic.getTargetPlatforms());
        appendLine(builder, "内容类型", topic.getContentType());
        appendLine(builder, "语气风格", topic.getToneStyle());
        if (topic.getExpectedWordCount() != null) {
            builder.append("【期望字数】").append(topic.getExpectedWordCount()).append('\n');
        }
        appendLine(builder, "备注", topic.getRemark());
        builder.append('\n');
        builder.append("【任务类型】").append(taskType.getCode()).append('\n');
        builder.append(buildTaskInstruction(taskType));
        return builder.toString();
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        if (StringUtils.hasText(value)) {
            builder.append('【').append(label).append('】').append(value.trim()).append('\n');
        }
    }

    private String buildTaskInstruction(GenerationTaskType taskType) {
        return switch (taskType) {
            case ARTICLE -> """
                    请生成一篇结构完整的中文文章，输出 JSON，字段如下：
                    title(标题), summary(摘要), body(正文 Markdown), coverPrompt(封面图提示词)
                    只返回 JSON，不要附加解释。
                    """;
            case VIDEO_SCRIPT -> """
                    请生成一份适合短视频口播的中文脚本，输出 JSON，字段如下：
                    title(标题), scriptContent(口播脚本，含开头钩子和分镜提示), coverPrompt(封面图提示词)
                    只返回 JSON，不要附加解释。
                    """;
            case OUTLINE -> """
                    请生成文章大纲，输出 JSON，字段如下：
                    title(标题), outline(分级大纲 Markdown)
                    只返回 JSON，不要附加解释。
                    """;
            case TITLE_CANDIDATES -> """
                    请生成 5 个高点击标题候选，输出 JSON，字段如下：
                    titleCandidates(用换行分隔的 5 个标题)
                    只返回 JSON，不要附加解释。
                    """;
            case SUMMARY -> """
                    请生成内容摘要，输出 JSON，字段如下：
                    summary(150 字以内摘要)
                    只返回 JSON，不要附加解释。
                    """;
            case TAGS -> """
                    请生成适合传播的标签，输出 JSON，字段如下：
                    tags(逗号分隔标签，8 个以内)
                    只返回 JSON，不要附加解释。
                    """;
            case COVER_PROMPT -> """
                    请生成封面图提示词，输出 JSON，字段如下：
                    coverPrompt(中文封面图提示词)
                    只返回 JSON，不要附加解释。
                    """;
        };
    }
}
