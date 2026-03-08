package com.eduapp.backend.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DBTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void dumpQuestions() throws Exception {
        FileWriter fw = new FileWriter("db_dump.txt");
        List<Map<String, Object>> questions = jdbcTemplate.queryForList(
                "SELECT id, image_url, requires_image_display FROM questions ORDER BY id DESC LIMIT 10");
        for (Map<String, Object> q : questions) {
            String line = "ID: " + q.get("id") + " | IMG: " + q.get("image_url") + " | SHOW: "
                    + q.get("requires_image_display");
            fw.write(line + "\n");
        }
        fw.close();
    }
}
