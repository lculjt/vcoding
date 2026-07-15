package com.vcoding.content.application.ai;

import com.vcoding.content.domain.generation.ContentGenerationResult;
import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地 Mock AI 客户端。不依赖外部模型服务，便于阶段 3 前后端联调。
 */
@Component
@ConditionalOnProperty(name = "vcoding.content.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockContentAiClient implements ContentAiClient {
    private static final String MODEL_NAME = "mock-content-model";

    @Override
    public ContentGenerationResult generate(TopicEntity topic, GenerationTaskType taskType, String prompt) {
        String title = topic.getTitle();
        return switch (taskType) {
            case ARTICLE -> new ContentGenerationResult(
                    title,
                    "围绕「" + title + "」的核心观点摘要，适合目标受众快速了解全文价值。",
                    buildMockArticleBody(topic),
                    null,
                    "一张现代简洁风格的信息图封面，主题：" + title + "，偏专业可信。",
                    null,
                    null,
                    null
            );
            case VIDEO_SCRIPT -> new ContentGenerationResult(
                    title,
                    null,
                    null,
                    buildMockVideoScript(topic),
                    "竖版短视频封面，突出「" + title + "」，高对比标题，真实场景背景。",
                    null,
                    null,
                    null
            );
            case OUTLINE -> new ContentGenerationResult(
                    title,
                    null,
                    null,
                    null,
                    null,
                    null,
                    """
                            ## 一、为什么现在值得关注
                            ## 二、核心方法拆解
                            ## 三、实操步骤
                            ## 四、常见误区
                            ## 五、总结与行动建议
                            """,
                    null
            );
            case TITLE_CANDIDATES -> new ContentGenerationResult(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    """
                            %s：3 个关键动作
                            从 0 到 1 实践：%s
                            为什么 %s 越来越重要？
                            一文看懂 %s 的落地路径
                            %s 实操清单（可直接复用）
                            """.formatted(title, title, title, title, title).trim()
            );
            case SUMMARY -> new ContentGenerationResult(
                    null,
                    "本文围绕「" + title + "」展开，结合目标受众与平台特点，给出可执行的方法、步骤和注意事项。",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            case TAGS -> new ContentGenerationResult(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "AI内容,内容创作,选题策划,增长方法,实操指南",
                    null,
                    null
            );
            case COVER_PROMPT -> new ContentGenerationResult(
                    null,
                    null,
                    null,
                    null,
                    "专业内容封面，主题「" + title + "」，留白充足，适合叠加标题文字。",
                    null,
                    null,
                    null
            );
        };
    }

    @Override
    public String modelName() {
        return MODEL_NAME;
    }

    private String buildMockArticleBody(TopicEntity topic) {
        String direction = topic.getContentDirection() == null ? "内容方向" : topic.getContentDirection();
        return """
                ## 背景

                %s 正在成为内容团队关注的核心主题。对于 %s 来说，理解 %s 的价值，是提升内容产出效率的第一步。

                ## 核心观点

                1. 先明确内容目标，再选择生成方式。
                2. 用结构化 Prompt 约束输出质量。
                3. 生成结果必须进入草稿库，便于后续平台适配。

                ## 实操建议

                - 从选题信息中补齐受众、关键词和平台字段。
                - 生成后先人工校对，再进入多平台改写。
                - 保留每次 AI 运行记录，便于复盘失败原因。

                ## 总结

                围绕「%s」建立标准化内容生产流程，可以显著降低重复劳动，并为后续自动发布打基础。
                """.formatted(
                topic.getTitle(),
                topic.getTargetAudience() == null ? "目标读者" : topic.getTargetAudience(),
                direction,
                topic.getTitle()
        );
    }

    private String buildMockVideoScript(TopicEntity topic) {
        return """
                【开头钩子】
                如果你也在做 %s，这条视频可能能帮你少走 90%% 的弯路。

                【主体口播】
                第一，先明确你的目标受众是谁。
                第二，把选题信息写完整，标题、关键词、平台都要清楚。
                第三，生成脚本后不要直接发布，先改成适合口播的短句。

                【结尾行动】
                想要完整方法，可以收藏这条视频，并在评论区告诉我你最关心的场景。

                【分镜提示】
                0-3 秒：大字标题 + 快切画面
                3-20 秒：口播 + B-roll
                20-30 秒：总结和行动号召
                """.formatted(topic.getTitle());
    }
}
