package com.sql_test.test_heavy_write;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@Slf4j
@AllArgsConstructor
public class BatchUpdateEngineerRepositoryImpl implements BatchUpdateEngineerRepository {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertEngineer(List<EngineerEntity> engineerEntities) {
        String sql = "INSERT INTO engineer_sync (id,first_name,last_name,gender,country_id,title,started_date,sync_status) VALUES (?,?,?,?,?,?,?,?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EngineerEntity engineer = engineerEntities.get(i);
                ps.setInt(1,engineer.getId());
                ps.setString(2,engineer.getFirstname());
                ps.setString(3,engineer.getLastname());
                ps.setInt(4,engineer.getGender());
                ps.setInt(5,engineer.getCountryId());
                ps.setString(6,engineer.getTitle());
                ps.setObject(7, engineer.getStartedDate());
                ps.setInt(8,0);
            }

            @Override
            public int getBatchSize() {
                return engineerEntities.size();
            }
        });

        log.info("--> INSERTED SUCCESSFULLY !");
    }

    @Override
    public void batchSyncEngineer(List<EngineerEntity> engineerEntities) {
        String sql = """
        INSERT INTO engineer
        (id, first_name, last_name, gender, country_id, title, started_date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            first_name = EXCLUDED.first_name,
            last_name = EXCLUDED.last_name,
            gender = EXCLUDED.gender,
            country_id = EXCLUDED.country_id,
            title = EXCLUDED.title,
            started_date = EXCLUDED.started_date
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EngineerEntity engineer = engineerEntities.get(i);
                ps.setInt(1,engineer.getId());
                ps.setString(2,engineer.getFirstname());
                ps.setString(3,engineer.getLastname());
                ps.setInt(4,engineer.getGender());
                ps.setInt(5,engineer.getCountryId());
                ps.setString(6,engineer.getTitle());
                ps.setObject(7, engineer.getStartedDate());
            }

            @Override
            public int getBatchSize() {
                return engineerEntities.size();
            }
        });

        String saveSql = """
                UPDATE engineer_sync
                SET sync_status = 1
                WHERE id = ?
                """;
        jdbcTemplate.batchUpdate(saveSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EngineerEntity engineer = engineerEntities.get(i);
                ps.setInt(1,engineer.getId());
            }

            @Override
            public int getBatchSize() {
                return engineerEntities.size();
            }
        });

        log.info("--> UPDATED SUCCESSFULLY !");
    }
}
