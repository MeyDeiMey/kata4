package software.ulpgc.model;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SqliteTitleLoader implements TitleLoader {
    private final DatabaseConnection connection;
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS titles (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                primary_title TEXT NOT NULL,
                original_title TEXT NOT NULL,
                is_adult INTEGER NOT NULL,
                start_year INTEGER,
                end_year INTEGER,
                runtime_minutes INTEGER,
                genres TEXT
            )
            """;

    private static final String INSERT_SQL = """
            INSERT OR REPLACE INTO titles (
                id, type, primary_title, original_title, is_adult,
                start_year, end_year, runtime_minutes, genres
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL = "SELECT * FROM titles";

    public SqliteTitleLoader(String dbPath) {
        this.connection = new DatabaseConnection(dbPath);
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    @Override
    public void save(List<Title> titles) throws SQLException {
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            conn.setAutoCommit(false);
            try {
                for (Title title : titles) {
                    mapTitleToStatement(stmt, title);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Title> load() throws SQLException {
        List<Title> titles = new ArrayList<>();

        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                titles.add(mapResultSetToTitle(rs));
            }
        }

        return titles;
    }

    private void mapTitleToStatement(PreparedStatement stmt, Title title) throws SQLException {
        stmt.setString(1, title.id());
        stmt.setString(2, title.type().name());
        stmt.setString(3, title.primaryTitle());
        stmt.setString(4, title.originalTitle());
        stmt.setInt(5, title.isAdult() ? 1 : 0);
        stmt.setObject(6, title.startYear().map(Year::getValue).orElse(null));
        stmt.setObject(7, title.endYear().map(Year::getValue).orElse(null));
        stmt.setObject(8, title.runtimeDuration().map(d -> (int)d.toMinutes()).orElse(null));
        stmt.setString(9, String.join(",", title.genres().stream().map(Enum::name).toList()));
    }

    private Title mapResultSetToTitle(ResultSet rs) throws SQLException {
        String genres = rs.getString("genres");
        List<Title.Genre> genreList = genres != null && !genres.isEmpty() ?
                Arrays.stream(genres.split(","))
                        .map(Title.Genre::valueOf)
                        .toList() :
                List.of();

        return new Title(
                rs.getString("id"),
                Title.TitleType.valueOf(rs.getString("type")),
                rs.getString("primary_title"),
                rs.getString("original_title"),
                rs.getInt("is_adult") == 1,
                Optional.ofNullable(rs.getObject("start_year"))
                        .map(y -> Year.of((Integer) y)),
                Optional.ofNullable(rs.getObject("end_year"))
                        .map(y -> Year.of((Integer) y)),
                Optional.empty(),
                genreList
        );
    }
}