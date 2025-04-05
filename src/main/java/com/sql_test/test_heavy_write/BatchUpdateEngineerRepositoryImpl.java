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
        String sql = "INSERT INTO engineer (id,first_name,last_name,gender,country_id,title,started_date) VALUES (?,?,?,?,?,?,?)";

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

        log.info("--> INSERT SUCCESSFULLY !");
    }
}
