package com.sql_test.test_heavy_write;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineerRepository extends JpaRepository<EngineerEntity,Integer>,BatchUpdateEngineerRepository {

    @Query(value = """
        SELECT * FROM engineer_sync
        WHERE sync_status = 0
        ORDER BY id
        LIMIT 1000
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<EngineerEntity> fetchBatchForProcessing();


    @Query(value = """
    SELECT * FROM engineer_sync
    WHERE sync_status = 0
    AND id >= :startId AND id < :endId
    ORDER BY id
    LIMIT 1000
    FOR UPDATE SKIP LOCKED
""", nativeQuery = true)
    List<EngineerEntity> fetchBatchForProcessingInRange(@Param("startId") int startId, @Param("endId") int endId);

}
