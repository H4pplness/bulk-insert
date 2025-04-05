package com.sql_test.test_heavy_write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EngineerRepository extends JpaRepository<EngineerEntity,Integer>,BatchUpdateEngineerRepository {

}
