package com.sql_test.test_heavy_write;

import java.util.List;

public interface BatchUpdateEngineerRepository {
    void batchInsertEngineer(List<EngineerEntity> engineerEntities);

    void batchSyncEngineer(List<EngineerEntity> engineerEntities);
}
