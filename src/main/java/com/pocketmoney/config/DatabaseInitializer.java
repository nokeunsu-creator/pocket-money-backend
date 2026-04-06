package com.pocketmoney.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // entries 테이블에 deleted 컬럼이 없으면 추가
            jdbcTemplate.execute(
                "ALTER TABLE entries ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT false NOT NULL"
            );
            jdbcTemplate.execute(
                "ALTER TABLE entries ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP"
            );
            jdbcTemplate.update("UPDATE entries SET deleted = false WHERE deleted IS NULL");

            // bank_entries 테이블에 deleted 컬럼이 없으면 추가
            jdbcTemplate.execute(
                "ALTER TABLE bank_entries ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT false NOT NULL"
            );
            jdbcTemplate.execute(
                "ALTER TABLE bank_entries ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP"
            );
            jdbcTemplate.update("UPDATE bank_entries SET deleted = false WHERE deleted IS NULL");

            System.out.println("[DB Init] deleted 컬럼 확인/추가 완료");
        } catch (Exception e) {
            System.out.println("[DB Init] 참고: " + e.getMessage());
        }
    }
}
