package com.sql_test.test_heavy_write;

import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface BatchUpdateEngineerRepository {
    void batchInsertEngineer(List<EngineerEntity> engineerEntities);

    void batchSyncEngineer(List<EngineerEntity> engineerEntities);

//    void batchUpdateEngineer();
}
