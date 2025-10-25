package no.setup.bankers.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Sql {

    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static final class DbException extends RuntimeException {
        public DbException(String msg, Throwable cause) { super(msg, cause); }
        public DbException(Throwable cause) { super(cause); }
    }

    public <T> Optional<T> one(Connection c, String q, Binder b, Mapper<T> m) {
        try (PreparedStatement ps = c.prepareStatement(q)) {
            if (b != null) b.bind(ps); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(m.map(rs));
                return Optional.empty();
            }
        }
        catch (SQLException e) {
            throw new DbException("one(): " + q, e);
        }
    }

    public <T> List<T> list(Connection c, String q, Binder b, Mapper<T> m) {
        List<T> res = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(q)) {
            if (b != null) b.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.add(m.map(rs));
                return res;
            }
        } catch (SQLException e) {
            throw new DbException("list(): " + q, e);
        }
    }

    public int update(Connection c, String q, Binder b) {
        try (PreparedStatement ps = c.prepareStatement(q)) {
            if (b != null) b.bind(ps);

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("update(): " + q, e);
        }
    }

    public int insert(Connection c, String q, Binder b) {
        try (PreparedStatement ps = c.prepareStatement(q, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (b != null) b.bind(ps);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DbException("insert(): " + q, e);
        }
    }
}

