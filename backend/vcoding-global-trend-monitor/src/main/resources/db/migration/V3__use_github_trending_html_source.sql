-- GitHub 一期改为解析官网 Trending HTML，配置记录同步为页面筛选条件。
UPDATE gtm_source
SET config_json = JSON_OBJECT(
        'mode', 'trending-html',
        'since', 'daily',
        'language', NULL,
        'spoken_language_code', NULL
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'github';
