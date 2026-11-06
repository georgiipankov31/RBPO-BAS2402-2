package ru.mtuci.rbpo.lab2.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.lab2.dto.AccountInfo;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UserDetails findUserDetails(String email) {
        List<UserDetails> values = jdbc.query("""
                SELECT email,
                       password_hash,
                       role,
                       enabled
                  FROM application_users
                 WHERE email = ?
                """, this::readUserDetails, email);
        return values.isEmpty() ? null : values.get(0);
    }

    public AccountInfo findByEmail(String email) {
        List<AccountInfo> values = jdbc.query("""
                SELECT id,
                       user_id,
                       email,
                       role,
                       enabled
                  FROM application_users
                 WHERE email = ?
                """, this::readAccount, email);
        return values.isEmpty() ? null : values.get(0);
    }

    public AccountInfo create(long userId, String email, String passwordHash) {
        // Новая учетная запись всегда USER; хеш в ответ не включаем.
        return jdbc.queryForObject("""
                INSERT INTO application_users (user_id, email, password_hash, role)
                     VALUES (?, ?, ?, 'USER')
                  RETURNING id,
                            user_id,
                            email,
                            role,
                            enabled
                """, this::readAccount, userId, email, passwordHash);
    }

    private UserDetails readUserDetails(ResultSet row, int rowNumber) throws SQLException {
        // Роль читаем из БД: roles("ADMIN") добавляет право ROLE_ADMIN.
        return User.withUsername(row.getString("email"))
                .password(row.getString("password_hash"))
                .roles(row.getString("role"))
                .disabled(!row.getBoolean("enabled"))
                .build();
    }

    private AccountInfo readAccount(ResultSet row, int rowNumber) throws SQLException {
        return new AccountInfo(row.getLong("id"), row.getLong("user_id"),
                row.getString("email"), row.getString("role"), row.getBoolean("enabled"));
    }
}
