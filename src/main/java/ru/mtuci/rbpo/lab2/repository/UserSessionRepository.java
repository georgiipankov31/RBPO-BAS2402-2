package ru.mtuci.rbpo.lab2.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.lab2.model.SessionStatus;
import ru.mtuci.rbpo.lab2.model.UserSession;

@Repository
public class UserSessionRepository {
    private final JdbcTemplate jdbc;

    public UserSessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UserSession findByRefreshToken(String refreshToken) {
        // Блокировка строки не дает двум запросам одновременно использовать один refresh.
        List<UserSession> values = jdbc.query("""
                SELECT id,
                       username,
                       device_id,
                       access_token,
                       refresh_token,
                       access_token_expiry,
                       refresh_token_expiry,
                       status
                  FROM user_sessions
                 WHERE refresh_token = ?
                   FOR UPDATE
                """, this::readSession, refreshToken);
        return values.isEmpty() ? null : values.get(0);
    }

    public void save(UserSession session) {
        jdbc.update("""
                INSERT INTO user_sessions (
                    id, username, device_id, access_token, refresh_token,
                    access_token_expiry, refresh_token_expiry, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, session.id(), session.username(), session.deviceId(),
                session.accessToken(), session.refreshToken(),
                Timestamp.from(session.accessTokenExpiry()),
                Timestamp.from(session.refreshTokenExpiry()), session.status().name());
    }

    public void changeStatus(UUID id, SessionStatus status) {
        jdbc.update("""
                UPDATE user_sessions
                   SET status = ?
                 WHERE id = ?
                """, status.name(), id);
    }

    private UserSession readSession(ResultSet row, int rowNumber) throws SQLException {
        return new UserSession(
                row.getObject("id", UUID.class),
                row.getString("username"),
                row.getString("device_id"),
                row.getString("access_token"),
                row.getString("refresh_token"),
                row.getTimestamp("access_token_expiry").toInstant(),
                row.getTimestamp("refresh_token_expiry").toInstant(),
                SessionStatus.valueOf(row.getString("status")));
    }
}
