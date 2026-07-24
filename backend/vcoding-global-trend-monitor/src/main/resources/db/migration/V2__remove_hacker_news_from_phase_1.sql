-- 一期范围收敛为 YouTube 和 GitHub，移除已预置的 Hacker News 数据源及关联项目数据。
SET @hacker_news_source_id := (SELECT id FROM gtm_source WHERE code = 'hacker-news' LIMIT 1);

DELETE ua
FROM gtm_user_trend_action ua
         JOIN gtm_trend_item ti ON ti.id = ua.trend_item_id
WHERE @hacker_news_source_id IS NOT NULL
  AND ti.source_id = @hacker_news_source_id;

DELETE ms
FROM gtm_metric_snapshot ms
         JOIN gtm_trend_item ti ON ti.id = ms.trend_item_id
WHERE @hacker_news_source_id IS NOT NULL
  AND ti.source_id = @hacker_news_source_id;

DELETE FROM gtm_trend_daily_aggregate
WHERE @hacker_news_source_id IS NOT NULL
  AND source_id = @hacker_news_source_id;

DELETE FROM gtm_collect_job
WHERE @hacker_news_source_id IS NOT NULL
  AND source_id = @hacker_news_source_id;

DELETE FROM gtm_trend_item
WHERE @hacker_news_source_id IS NOT NULL
  AND source_id = @hacker_news_source_id;

DELETE FROM gtm_source
WHERE code = 'hacker-news';
