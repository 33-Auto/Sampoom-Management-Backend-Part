package com.sampoom.backend.api.item.repository;

import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ItemResponseDTO> searchAll(String keyword, int offset, int size) {
        String sql = """
            SELECT id, code, name, 'MATERIAL' AS type, NULL AS category_id, NULL AS group_id
            FROM material
            WHERE LOWER(name) LIKE LOWER(?) OR LOWER(code) LIKE LOWER(?)
            UNION ALL
            SELECT id, code, name, 'PART' AS type,
                   category_id AS category_id,
                   group_id AS group_id
            FROM part
            WHERE LOWER(name) LIKE LOWER(?) OR LOWER(code) LIKE LOWER(?)
            ORDER BY code
            LIMIT ? OFFSET ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> ItemResponseDTO.builder()
                        .type(rs.getString("type").equals("MATERIAL") ? "원자재" : "부품")
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .categoryId(rs.getLong("category_id") == 0 ? null : rs.getLong("category_id"))
                        .groupId(rs.getLong("group_id") == 0 ? null : rs.getLong("group_id"))
                        .build(),
                "%" + keyword + "%", "%" + keyword + "%",
                "%" + keyword + "%", "%" + keyword + "%",
                size, offset
        );
    }

    public int countAll(String keyword) {
        String countSql = """
            SELECT COUNT(*) FROM (
                SELECT id FROM material
                WHERE LOWER(name) LIKE LOWER(?) OR LOWER(code) LIKE LOWER(?)
                UNION ALL
                SELECT id FROM part
                WHERE LOWER(name) LIKE LOWER(?) OR LOWER(code) LIKE LOWER(?)
            ) AS total
            """;

        return jdbcTemplate.queryForObject(
                countSql,
                Integer.class,
                "%" + keyword + "%", "%" + keyword + "%",
                "%" + keyword + "%", "%" + keyword + "%"
        );
    }
}
