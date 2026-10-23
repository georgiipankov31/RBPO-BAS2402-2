package ru.mtuci.rbpo.lab2.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.Room;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.model.User;

@Repository
public class HousingRepository {
    private final JdbcTemplate jdbc;

    public HousingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> users() {
        return jdbc.query("""
                SELECT id,
                       name
                  FROM users
                 ORDER BY id
                """, this::readUser);
    }

    public User user(long id) {
        List<User> values = jdbc.query("""
                SELECT id,
                       name
                  FROM users
                 WHERE id = ?
                """, this::readUser, id);
        return values.isEmpty() ? null : values.get(0);
    }

    public User saveUser(Long id, String name) {
        if (id == null) {
            return jdbc.queryForObject("""
                    INSERT INTO users (name)
                         VALUES (?)
                      RETURNING id,
                                name
                    """, this::readUser, name);
        }
        jdbc.update("""
                UPDATE users
                   SET name = ?
                 WHERE id = ?
                """, name, id);
        return user(id);
    }

    public void deleteUser(long id) {
        jdbc.update("""
                DELETE FROM users
                      WHERE id = ?
                """, id);
    }

    public List<Room> rooms() {
        return jdbc.query("""
                SELECT id,
                       number,
                       capacity,
                       available
                  FROM rooms
                 ORDER BY id
                """, this::readRoom);
    }

    public Room room(long id) {
        List<Room> values = jdbc.query("""
                SELECT id,
                       number,
                       capacity,
                       available
                  FROM rooms
                 WHERE id = ?
                """, this::readRoom, id);
        return values.isEmpty() ? null : values.get(0);
    }

    public Room saveRoom(Long id, String number, int capacity, boolean available) {
        if (id == null) {
            return jdbc.queryForObject("""
                    INSERT INTO rooms (number, capacity, available)
                         VALUES (?, ?, ?)
                      RETURNING id,
                                number,
                                capacity,
                                available
                    """, this::readRoom, number, capacity, available);
        }
        jdbc.update("""
                UPDATE rooms
                   SET number = ?,
                       capacity = ?,
                       available = ?
                 WHERE id = ?
                """, number, capacity, available, id);
        return room(id);
    }

    public void deleteRoom(long id) {
        jdbc.update("""
                DELETE FROM rooms
                      WHERE id = ?
                """, id);
    }

    public int occupiedPlaces(long roomId) {
        return jdbc.queryForObject("""
                SELECT count(*)
                  FROM residence_contracts
                 WHERE room_id = ?
                   AND status = 'ACTIVE'
                """, Integer.class, roomId);
    }

    public boolean hasActiveContract(long userId) {
        return jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                      FROM residence_contracts c
                      JOIN housing_applications a ON a.id = c.application_id
                     WHERE a.user_id = ?
                       AND c.status = 'ACTIVE'
                )
                """, Boolean.class, userId);
    }

    public List<HousingApplicationEntry> applications() {
        return jdbc.query("""
                SELECT id,
                       user_id,
                       submitted_at,
                       status
                  FROM housing_applications
                 ORDER BY id
                """, this::readApplication);
    }

    public HousingApplicationEntry application(long id) {
        List<HousingApplicationEntry> values = jdbc.query("""
                SELECT id,
                       user_id,
                       submitted_at,
                       status
                  FROM housing_applications
                 WHERE id = ?
                """, this::readApplication, id);
        return values.isEmpty() ? null : values.get(0);
    }

    public HousingApplicationEntry saveApplication(Long id, long userId, String status) {
        if (id == null) {
            return jdbc.queryForObject("""
                    INSERT INTO housing_applications (user_id, status)
                         VALUES (?, ?)
                      RETURNING id,
                                user_id,
                                submitted_at,
                                status
                    """, this::readApplication, userId, status);
        }
        jdbc.update("""
                UPDATE housing_applications
                   SET user_id = ?,
                       status = ?
                 WHERE id = ?
                """, userId, status, id);
        return application(id);
    }

    public void setApplicationStatus(long id, String status) {
        jdbc.update("""
                UPDATE housing_applications
                   SET status = ?
                 WHERE id = ?
                """, status, id);
    }

    public void deleteApplication(long id) {
        jdbc.update("""
                DELETE FROM housing_applications
                      WHERE id = ?
                """, id);
    }

    public List<ResidenceContract> contracts() {
        return jdbc.query("""
                SELECT id,
                       application_id,
                       room_id,
                       check_in_date,
                       check_out_date,
                       status
                  FROM residence_contracts
                 ORDER BY id
                """, this::readContract);
    }

    public ResidenceContract contract(long id) {
        List<ResidenceContract> values = jdbc.query("""
                SELECT id,
                       application_id,
                       room_id,
                       check_in_date,
                       check_out_date,
                       status
                  FROM residence_contracts
                 WHERE id = ?
                """, this::readContract, id);
        return values.isEmpty() ? null : values.get(0);
    }

    public ResidenceContract saveContract(Long id, long applicationId, long roomId,
            LocalDate checkInDate, LocalDate checkOutDate, String status) {
        if (id == null) {
            return jdbc.queryForObject("""
                    INSERT INTO residence_contracts (application_id, room_id, check_in_date, check_out_date, status)
                         VALUES (?, ?, ?, ?, ?)
                      RETURNING id,
                                application_id,
                                room_id,
                                check_in_date,
                                check_out_date,
                                status
                    """, this::readContract, applicationId, roomId, checkInDate, checkOutDate, status);
        }
        jdbc.update("""
                UPDATE residence_contracts
                   SET application_id = ?,
                       room_id = ?,
                       check_in_date = ?,
                       check_out_date = ?,
                       status = ?
                 WHERE id = ?
                """, applicationId, roomId, checkInDate, checkOutDate, status, id);
        return contract(id);
    }

    public boolean applicationHasActiveContract(long applicationId) {
        return jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                      FROM residence_contracts
                     WHERE application_id = ?
                       AND status = 'ACTIVE'
                )
                """, Boolean.class, applicationId);
    }

    public void deleteContract(long id) {
        jdbc.update("""
                DELETE FROM residence_contracts
                      WHERE id = ?
                """, id);
    }

    public List<RoomTransferRequest> transfers() {
        return jdbc.query("""
                SELECT id,
                       contract_id,
                       target_room_id,
                       submitted_at,
                       status
                  FROM room_transfer_requests
                 ORDER BY id
                """, this::readTransfer);
    }

    public RoomTransferRequest transfer(long id) {
        List<RoomTransferRequest> values = jdbc.query("""
                SELECT id,
                       contract_id,
                       target_room_id,
                       submitted_at,
                       status
                  FROM room_transfer_requests
                 WHERE id = ?
                """, this::readTransfer, id);
        return values.isEmpty() ? null : values.get(0);
    }

    public RoomTransferRequest saveTransfer(Long id, long contractId, long targetRoomId, String status) {
        if (id == null) {
            return jdbc.queryForObject("""
                    INSERT INTO room_transfer_requests (contract_id, target_room_id, status)
                         VALUES (?, ?, ?)
                      RETURNING id,
                                contract_id,
                                target_room_id,
                                submitted_at,
                                status
                    """, this::readTransfer, contractId, targetRoomId, status);
        }
        jdbc.update("""
                UPDATE room_transfer_requests
                   SET contract_id = ?,
                       target_room_id = ?,
                       status = ?
                 WHERE id = ?
                """, contractId, targetRoomId, status, id);
        return transfer(id);
    }

    public void cancelPendingTransfers(long contractId) {
        jdbc.update("""
                UPDATE room_transfer_requests
                   SET status = 'CANCELLED'
                 WHERE contract_id = ?
                   AND status = 'NEW'
                """, contractId);
    }

    public void deleteTransfer(long id) {
        jdbc.update("""
                DELETE FROM room_transfer_requests
                      WHERE id = ?
                """, id);
    }

    private User readUser(ResultSet row, int rowNumber) throws SQLException {
        return new User(row.getLong("id"), row.getString("name"));
    }

    private Room readRoom(ResultSet row, int rowNumber) throws SQLException {
        return new Room(row.getLong("id"), row.getString("number"),
                row.getInt("capacity"), row.getBoolean("available"));
    }

    private HousingApplicationEntry readApplication(ResultSet row, int rowNumber) throws SQLException {
        return new HousingApplicationEntry(row.getLong("id"), row.getLong("user_id"),
                row.getTimestamp("submitted_at").toInstant(), row.getString("status"));
    }

    private ResidenceContract readContract(ResultSet row, int rowNumber) throws SQLException {
        return new ResidenceContract(row.getLong("id"), row.getLong("application_id"),
                row.getLong("room_id"), row.getObject("check_in_date", LocalDate.class),
                row.getObject("check_out_date", LocalDate.class), row.getString("status"));
    }

    private RoomTransferRequest readTransfer(ResultSet row, int rowNumber) throws SQLException {
        return new RoomTransferRequest(row.getLong("id"), row.getLong("contract_id"),
                row.getLong("target_room_id"), row.getTimestamp("submitted_at").toInstant(),
                row.getString("status"));
    }
}
