package com.sql_test.test_heavy_write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineerRepository extends JpaRepository<EngineerEntity,Integer>,BatchUpdateEngineerRepository {
    @Query(value = """
        SELECT * FROM engineer_sync
        WHERE sync_status = 0
        AND id%1000=:redundant
        LIMIT 1000
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<EngineerEntity> fetchRedundantBatch(@Param("redundant")int redundant);

    @Query(value = """
        SELECT * FROM engineer_sync
        WHERE sync_status = 0
        LIMIT 1000
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<EngineerEntity> fetchSimpleBatch();

    @Query(value = "SELECT * FROM engineer_sync " +
            "WHERE sync_status = 0 " +
            "AND id > :lastIdx " +
            "ORDER BY id " +
            "LIMIT :batch " +
            "FOR UPDATE SKIP LOCKED"
    ,nativeQuery = true)
    List<EngineerEntity> cursorFetchBatch(@Param("lastIdx")Integer lastIdx,@Param("batch")Integer batchSize);


    @Query(value = """
            SELECT * FROM engineer_sync 
            WHERE sync_status = 0 
            AND id BETWEEN :firstIdx AND :lastIdx 
            LIMIT :batch 
            FOR UPDATE SKIP LOCKED
            """,nativeQuery = true)
    List<EngineerEntity> fetchBatchInARange(@Param("firstIdx") Integer firstIdx, @Param("lastIdx") Integer lastIdx, @Param("batch") Integer batchSize);
}
