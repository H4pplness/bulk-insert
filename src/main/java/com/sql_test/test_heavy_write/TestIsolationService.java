package com.sql_test.test_heavy_write;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestIsolationService {
    private final EngineerRepository engineerRepository;
    private final JdbcTemplate jdbcTemplate;

    public void syncData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i=0;i<1000;i++){
            executor.execute(()->syncBatch());
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Timed out waiting for batch sync tasks");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


//    @Transactional(isolation = Isolation.REPEATABLE_READ,rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
//    public void syncBatch() {
//        List<EngineerEntity> engineerEntities = engineerRepository.fetchSimpleBatch();
//        engineerRepository.batchSyncEngineer(engineerEntities);
//    }



    /**
     * This method is transactional:
     * - Locks the batch
     * - Processes and updates it
     * - Commits or rolls back as one unit
     */
//    @Transactional(isolation = Isolation.SERIALIZABLE,rollbackFor = Exception.class)
//    public void syncBatch() {
//        List<EngineerEntity> batch = engineerRepository.fetchSimpleBatch();
//        if (batch.isEmpty()) {
//            return; // no more rows left
//        }
//
//        // Insert into target table
//        String insertSql = """
//            INSERT INTO engineer
//            (sync_id, first_name, last_name, gender, country_id, title, started_date)
//            VALUES (?, ?, ?, ?, ?, ?, ?)
//        """;
//        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                EngineerEntity e = batch.get(i);
//                ps.setInt(1, e.getId());
//                ps.setString(2, e.getFirstname());
//                ps.setString(3, e.getLastname());
//                ps.setInt(4, e.getGender());
//                ps.setInt(5, e.getCountryId());
//                ps.setString(6, e.getTitle());
//                ps.setObject(7, e.getStartedDate());
//            }
//
//            @Override
//            public int getBatchSize() {
//                return batch.size();
//            }
//        });
//
//        // Mark as synced
//        String updateSql = """
//            UPDATE engineer_sync
//            SET sync_status = 1
//            WHERE id = ?
//        """;
//        jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                ps.setInt(1, batch.get(i).getId());
//            }
//
//            @Override
//            public int getBatchSize() {
//                return batch.size();
//            }
//        });
//    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void syncBatch() {
        // Atomically claim rows by updating status first
        List<EngineerEntity> batch = jdbcTemplate.query("""
            WITH claimed AS (
                SELECT id FROM engineer_sync
                WHERE sync_status = 0
                LIMIT 1000
                FOR UPDATE SKIP LOCKED
            )
            UPDATE engineer_sync
            SET sync_status = 1
            FROM claimed
            WHERE engineer_sync.id = claimed.id
            RETURNING engineer_sync.*
        """, (rs, rowNum) -> {
            EngineerEntity e = new EngineerEntity();
            e.setId(rs.getInt("id"));
            e.setFirstname(rs.getString("first_name"));
            e.setLastname(rs.getString("last_name"));
            e.setGender(rs.getInt("gender"));
            e.setCountryId(rs.getInt("country_id"));
            e.setTitle(rs.getString("title"));
            e.setStartedDate(rs.getObject("started_date", LocalDate.class));
            return e;
        });

        if (batch.isEmpty()) return;

        // Now insert - rows are already marked as synced
        String insertSql = """
        INSERT INTO engineer
        (sync_id, first_name, last_name, gender, country_id, title, started_date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EngineerEntity e = batch.get(i);
                ps.setInt(1, e.getId());
                ps.setString(2, e.getFirstname());
                ps.setString(3, e.getLastname());
                ps.setInt(4, e.getGender());
                ps.setInt(5, e.getCountryId());
                ps.setString(6, e.getTitle());
                ps.setObject(7, e.getStartedDate());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });

        log.info("--> INSERTED SUCCESSFULLY !");
    }
}
