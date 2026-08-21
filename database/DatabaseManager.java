package database;

import models.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    public DatabaseManager(String user, String password) {
        this.user = user;
        this.password = password;

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            this.url = "jdbc:postgresql://localhost:5432/postgres";
        } else {
            this.url = "jdbc:postgresql://pg:5432/studs";
        }

        connect();
        createTables();
    }

    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            DriverManager.setLogWriter(new java.io.PrintWriter(System.out));

            System.out.println("[ОТЛАДКА КОДА] Имя пользователя отправляемое в БД: '" + this.user + "' (длина: " + this.user.length() + ")");
            System.out.println("[ОТЛАДКА КОДА] Пароль отправляемый в БД: '" + this.password + "' (длина: " + this.password.length() + ")");

            System.out.println("[Отладка] Отправка данных на URL: " + this.url);
            this.connection = DriverManager.getConnection(this.url, user, password);
            System.out.println("[БД] Успешное подключение к: " + this.url);
        } catch (Exception e) {
            System.err.println("[КРИТИЧЕСКАЯ ОШИБКА БД] Не удалось подключиться: " + e.getMessage());
            System.exit(1);
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(100) PRIMARY KEY, " +
                    "password_hash VARCHAR(40) NOT NULL);");

            stmt.execute("CREATE TABLE IF NOT EXISTS study_groups (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "x INT, " +
                    "y FLOAT, " +
                    "creation_date TEXT NOT NULL, " +
                    "students_count BIGINT, " +
                    "expelled_students INT, " +
                    "form_of_education TEXT, " +
                    "semester TEXT, " +
                    "admin_name TEXT, " +
                    "admin_birthday TIMESTAMP, " +
                    "admin_eye_color TEXT, " +
                    "loc_x BIGINT, " +
                    "loc_y BIGINT, " +
                    "loc_z FLOAT, " +
                    "loc_name TEXT, " +
                    "owner VARCHAR(100) REFERENCES users(username));");
        } catch (SQLException e) {
            System.err.println("[БД] Ошибка при инициализации таблиц: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] result = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-1 не найден", e);
        }
    }

    public boolean validateUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) return false;
        String hash = hashPassword(password);

        try (PreparedStatement checkStmt = connection.prepareStatement("SELECT password_hash FROM users WHERE username = ?")) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash").equals(hash);
            } else {
                try (PreparedStatement insStmt = connection.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)")) {
                    insStmt.setString(1, username);
                    insStmt.setString(2, hash);
                    insStmt.executeUpdate();
                    System.out.println("[БД] Зарегистрирован новый пользователь: " + username);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[БД] Ошибка валидации пользователя: " + e.getMessage());
            return false;
        }
    }

    public LinkedHashMap<Long, StudyGroup> loadCollection() {
        LinkedHashMap<Long, StudyGroup> collection = new LinkedHashMap<>();
        String query = "SELECT * FROM study_groups";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                Coordinates coords = new Coordinates(rs.getInt("x"), rs.getFloat("y"));
                ZonedDateTime creationDate = ZonedDateTime.parse(rs.getString("creation_date"));
                long studentsCount = rs.getLong("students_count");
                Integer expelledStudents = rs.getInt("expelled_students");
                if (rs.wasNull()) expelledStudents = null;

                FormOfEducation form = FormOfEducation.valueOf(rs.getString("form_of_education"));
                Semester semester = rs.getString("semester") != null ? Semester.valueOf(rs.getString("semester")) : null;

                String adminName = rs.getString("admin_name");
                java.util.Date adminBirthday = rs.getTimestamp("admin_birthday") != null ? new java.util.Date(rs.getTimestamp("admin_birthday").getTime()) : null;
                Color eyeColor = rs.getString("admin_eye_color") != null ? Color.valueOf(rs.getString("admin_eye_color")) : null;

                Location loc = null;
                if (rs.getString("loc_name") != null || rs.getLong("loc_x") != 0) {
                    loc = new Location(rs.getLong("loc_x"), rs.getLong("loc_y"), rs.getFloat("loc_z"), rs.getString("loc_name"));
                }

                Person admin = new Person(adminName, adminBirthday, eyeColor, loc);
                StudyGroup group = new StudyGroup(id, name, coords, creationDate, studentsCount, expelledStudents, form, semester, admin);

                collection.put(id, group);
            }
        } catch (SQLException e) {
            System.err.println("[БД] Ошибка при загрузке коллекции: " + e.getMessage());
        }
        return collection;
    }

    public long insertGroup(StudyGroup group, String owner) throws SQLException {
        String query = "INSERT INTO study_groups(name, x, y, creation_date, students_count, expelled_students, form_of_education, semester, admin_name, admin_birthday, admin_eye_color, loc_x, loc_y, loc_z, loc_name, owner) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCoordinates().getX());
            ps.setFloat(3, group.getCoordinates().getY());
            ps.setString(4, group.getCreationDate().toString());
            ps.setLong(5, group.getStudentsCount());
            if (group.getExpelledStudents() != null) ps.setInt(6, group.getExpelledStudents()); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, group.getFormOfEducation().name());
            ps.setString(8, group.getSemesterEnum() != null ? group.getSemesterEnum().name() : null);
            ps.setString(9, group.getGroupAdmin().getName());
            ps.setTimestamp(10, group.getGroupAdmin().getBirthday() != null ? new Timestamp(group.getGroupAdmin().getBirthday().getTime()) : null);
            ps.setString(11, group.getGroupAdmin().getEyeColor() != null ? group.getGroupAdmin().getEyeColor().name() : null);

            if (group.getGroupAdmin().getLocation() != null) {
                ps.setLong(12, group.getGroupAdmin().getLocation().getX());
                ps.setLong(13, group.getGroupAdmin().getLocation().getY());
                ps.setFloat(14, group.getGroupAdmin().getLocation().getZ());
                ps.setString(15, group.getGroupAdmin().getLocation().getName());
            } else {
                ps.setNull(12, Types.BIGINT); ps.setNull(13, Types.BIGINT); ps.setNull(14, Types.REAL); ps.setNull(15, Types.VARCHAR);
            }
            ps.setString(16, owner);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
            throw new SQLException("Не удалось получить сгенерированный ID");
        }
    }

    public boolean checkOwner(long id, String username) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT owner FROM study_groups WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("owner").equals(username);
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    public boolean deleteGroup(long id, String username) throws SQLException {
        if (!checkOwner(id, username)) return false;
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM study_groups WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void updateGroup(long id, StudyGroup group) throws SQLException {
        String query = "UPDATE study_groups SET name = ?, x = ?, y = ?, students_count = ?, expelled_students = ?, " +
                "form_of_education = ?, semester = ?, admin_name = ?, admin_birthday = ?, admin_eye_color = ?, " +
                "loc_x = ?, loc_y = ?, loc_z = ?, loc_name = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCoordinates().getX());
            ps.setFloat(3, group.getCoordinates().getY());
            ps.setLong(4, group.getStudentsCount());
            if (group.getExpelledStudents() != null) ps.setInt(5, group.getExpelledStudents()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, group.getFormOfEducation().name());
            ps.setString(7, group.getSemesterEnum() != null ? group.getSemesterEnum().name() : null);
            ps.setString(8, group.getGroupAdmin().getName());
            ps.setTimestamp(9, group.getGroupAdmin().getBirthday() != null ? new Timestamp(group.getGroupAdmin().getBirthday().getTime()) : null);
            ps.setString(10, group.getGroupAdmin().getEyeColor() != null ? group.getGroupAdmin().getEyeColor().name() : null);

            if (group.getGroupAdmin().getLocation() != null) {
                ps.setLong(11, group.getGroupAdmin().getLocation().getX());
                ps.setLong(12, group.getGroupAdmin().getLocation().getY());
                ps.setFloat(13, group.getGroupAdmin().getLocation().getZ());
                ps.setString(14, group.getGroupAdmin().getLocation().getName());
            } else {
                ps.setNull(11, Types.BIGINT); ps.setNull(12, Types.BIGINT); ps.setNull(13, Types.REAL); ps.setNull(14, Types.VARCHAR);
            }
            ps.setLong(15, id);

            ps.executeUpdate();
        }
    }

    public void clearUserGroups(String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM study_groups WHERE owner = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
}