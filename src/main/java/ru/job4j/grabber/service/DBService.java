package ru.job4j.grabber.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.model.Vacancy;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 17.02.2018.
 */
public class DBService implements IDBService {

    private static final Logger LOG = LoggerFactory.getLogger(DBService.class);

    private final Properties prs = new Properties();

    private final String propertyFile = "db.properties";

    private Connection conn;

    public DBService() {
        try {
            this.setProperties();
            this.connectToDB();
        } catch (IOException | SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        this.createTable();
    }

    @Override
    public void add(Collection<Vacancy> vacancies) {
        try (PreparedStatement ps = this.conn.prepareStatement(
                "INSERT INTO vacancies (topic, author, description, create_date) SELECT ?, ?, ?, ? "
                        + "WHERE NOT EXISTS(SELECT * FROM vacancies WHERE description = ?)")) {
            this.conn.setAutoCommit(false);
            for (Vacancy vacancy : vacancies) {
                ps.setString(1, vacancy.getTopic());
                ps.setString(2, vacancy.getAuthor());
                ps.setString(3, vacancy.getDescription());
                ps.setTimestamp(4, vacancy.getCreateDate());
                ps.setString(5, vacancy.getDescription());
                ps.executeUpdate();
            }
            this.conn.commit();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            try {
                this.conn.rollback();
            } catch (SQLException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        } finally {
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void delete() {
        try (Statement st = this.conn.createStatement()) {
            st.execute("DELETE FROM vacancies");
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            this.conn.close();
            LOG.info("Connection close.");
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setProperties() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(this.propertyFile);
        this.prs.load(in);
        LOG.info("Properties loaded", this.prs);
    }

    private void connectToDB() throws SQLException {
        this.conn = DriverManager.getConnection(this.prs.getProperty("jdbc.url"),
                this.prs.getProperty("jdbc.username"), this.prs.getProperty("jdbc.password"));
        LOG.info("Connection is established.", conn);
    }

    private void createTable() {
        try (PreparedStatement ps = this.conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS vacancies (id SERIAL PRIMARY KEY NOT NULL, topic TEXT NOT NULL,"
                        + " author TEXT NOT NULL, description TEXT NOT NULL, create_date TIMESTAMP NOT NULL)"
               )) {
            ps.execute();
            LOG.info("Table is created.");
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
