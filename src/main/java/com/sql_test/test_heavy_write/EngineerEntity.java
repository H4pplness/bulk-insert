package com.sql_test.test_heavy_write;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "engineer_sync")
public class EngineerEntity {
    @Id
    private Integer id;

    @Column(name = "first_name")
    private String firstname;

    @Column(name = "last_name")
    private String lastname;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "title")
    private String title;

    @Column(name = "started_date")
    private LocalDate startedDate;

    @Column(name = "sync_status")
    private int syncStatus;
}
