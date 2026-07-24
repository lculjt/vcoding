package com.vcoding.globaltrend.domain.collect;

import java.util.List;

/**
 * 单个平台的正式采集契约。connector 只负责读取和标准化，不负责写库。
 */
public interface SourceConnector {
    String sourceCode();

    List<TrendItemDraft> collect();
}
