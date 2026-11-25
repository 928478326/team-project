package data_access.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import entity.Community;
import entity.Event;
import entity.User;
import use_case.browse_events.BrowseEventsDataAccessInterface;
import use_case.browse_events.BrowseEventsInputData;
import use_case.create_event.CreateEventDataAccessInterface;
import use_case.join_event.JoinEventDataAccessInterface;
import use_case.view_calendar.ViewCalendarDataAccessInterface;

/**
 * SQLite implementation of the community event, atomic participation, and calendar DAIs.
 *
 * <p>Communities, events, and participations live in the same database file as accounts, so
 * published events and joined spots survive a restart. The constructor creates the three
 * tables on first launch and inserts the default communities.
 */
public final class SqliteEventDataAccess
        implements BrowseEventsDataAccessInterface,
                CreateEventDataAccessInterface,
                JoinEventDataAccessInterface,
                ViewCalendarDataAccessInterface {

    private static final String EVENT_COLUMNS = "e.event_id, e.community_id, e.creator_id, e.title, "
            + "e.description, e.capacity, e.starts_at_ms";

    private static final String SELECT_EVENTS = "SELECT " + EVENT_COLUMNS + " FROM events e";

    private static final String EVENT_ORDER = " ORDER BY e.starts_at_ms, e.event_id";

    private static final String COUNT_PARTICIPANTS =
            "SELECT COUNT(*) FROM participations WHERE event_id = ?";

    private static final String FIND_PARTICIPATION =
            "SELECT joined_at_ms FROM participations WHERE user_id = ? AND event_id = ?";

    private final SqliteDatabase database;
    private final SqliteUserDataAccess users;

    /**
     * Creates the DAO and makes sure the community, event, and participation tables exist.
     *
     * @param database shared database handle (the {@code users} table must already exist)
     * @throws IllegalStateException if the tables cannot be created
     */
    public SqliteEventDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
        this.users = new SqliteUserDataAccess(database);
        initializeSchema();
    }

    private void initializeSchema() {
        try (Connection connection = database.openConnection();
                Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE IF NOT EXISTS communities ("
                    + "community_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL)");
            sql.execute("CREATE TABLE IF NOT EXISTS events ("
                    + "event_id TEXT PRIMARY KEY NOT NULL, "
                    + "community_id TEXT NOT NULL REFERENCES communities(community_id), "
                    + "creator_id TEXT NOT NULL REFERENCES users(user_id), "
                    + "title TEXT NOT NULL, description TEXT NOT NULL, "
                    + "capacity INTEGER NOT NULL, starts_at_ms INTEGER NOT NULL)");
            sql.execute("CREATE TABLE IF NOT EXISTS participations ("
                    + "user_id TEXT NOT NULL REFERENCES users(user_id), "
                    + "event_id TEXT NOT NULL REFERENCES events(event_id), "
                    + "joined_at_ms INTEGER NOT NULL, PRIMARY KEY (user_id, event_id))");
            sql.execute("CREATE INDEX IF NOT EXISTS idx_events_start ON events(starts_at_ms)");
            sql.execute("CREATE INDEX IF NOT EXISTS idx_participations_event "
                    + "ON participations(event_id)");
            seedDefaultCommunities(connection);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not initialize the event tables.", ex);
        }
    }

    private static void seedDefaultCommunities(Connection connection) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT OR IGNORE INTO communities (community_id, name) VALUES (?, ?)")) {
            for (Community community : Community.defaultCommunities()) {
                insert.setString(1, community.getCommunityId());
                insert.setString(2, community.getName());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public boolean communityExists(String communityId) {
        boolean exists = false;
        if (communityId != null) {
            exists = countRows("SELECT COUNT(*) FROM communities WHERE community_id = ?",
                    communityId.trim().toLowerCase(Locale.ROOT)) > 0;
        }
        return exists;
    }

    @Override
    public List<Community> findAllCommunities() {
        final List<Community> result = new ArrayList<>();
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "SELECT community_id, name FROM communities ORDER BY rowid");
                ResultSet rows = sql.executeQuery()) {
            while (rows.next()) {
                result.add(new Community(rows.getString("community_id"), rows.getString("name")));
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read communities.", ex);
        }
        return result;
    }

    @Override
    public List<Event> findEventsByCommunity(String communityId) {
        final boolean all = communityId == null
                || communityId.isBlank()
                || BrowseEventsInputData.ALL_COMMUNITIES.equalsIgnoreCase(communityId);
        final List<Event> result;
        if (all) {
            result = queryEvents(SELECT_EVENTS + EVENT_ORDER);
        }
        else {
            result = queryEvents(SELECT_EVENTS + " WHERE e.community_id = ?" + EVENT_ORDER,
                    communityId.trim().toLowerCase(Locale.ROOT));
        }
        return result;
    }

    @Override
    public int countParticipants(String eventId) {
        return countRows(COUNT_PARTICIPANTS, eventId);
    }

    @Override
    public boolean hasUserJoined(String userId, String eventId) {
        return countRows("SELECT COUNT(*) FROM participations WHERE user_id = ? AND event_id = ?",
                userId, eventId) > 0;
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return users.findUserById(userId);
    }

    @Override
    public void saveEvent(Event event) {
        final String insert = "INSERT INTO events (event_id, community_id, creator_id, title, "
                + "description, capacity, starts_at_ms) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(insert)) {
            final int next = bindStrings(sql, event.getEventId(), event.getCommunityId(), event.getCreatorId(),
                    event.getTitle(), event.getDescription());
            sql.setInt(next, event.getCapacity());
            sql.setLong(next + 1, event.getStartsAt().toEpochMilli());
            sql.executeUpdate();
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not save the event.", ex);
        }
    }

    @Override
    public Optional<Event> findEventById(String eventId) {
        try (Connection connection = database.openConnection()) {
            return findEventIn(connection, eventId);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read the event.", ex);
        }
    }

    /**
     * Joins an event inside one {@code BEGIN IMMEDIATE} transaction.
     *
     * <p>{@code BEGIN IMMEDIATE} takes SQLite's write lock before the capacity check, so two
     * students racing for the last spot, even from different app windows on the same database
     * file, are serialized: the second one sees the updated count and gets {@code FULL}.
     *
     * @param userId joining user ID
     * @param eventId target event ID
     * @param now current instant from the injected clock
     * @return the outcome, the latest participant count, and the join time where relevant
     */
    @Override
    public JoinAttemptResult joinAtomically(String userId, String eventId, Instant now) {
        try (Connection connection = database.openConnection();
                Statement transaction = connection.createStatement()) {
            transaction.execute("BEGIN IMMEDIATE");
            boolean committed = false;
            try {
                final JoinAttemptResult result = attemptJoin(connection, userId, eventId, now);
                transaction.execute("COMMIT");
                committed = true;
                return result;
            }
            finally {
                if (!committed) {
                    transaction.execute("ROLLBACK");
                }
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not join the event.", ex);
        }
    }

    private JoinAttemptResult attemptJoin(Connection connection, String userId, String eventId,
                                          Instant now) throws SQLException {
        final Optional<Event> found = findEventIn(connection, eventId);
        JoinAttemptResult result = new JoinAttemptResult(Outcome.EVENT_NOT_FOUND, 0, 0, null);
        if (found.isPresent()) {
            final Event event = found.get();
            final int count = countRows(connection, COUNT_PARTICIPANTS, eventId);
            final Optional<Instant> joinedAt = findJoinedAt(connection, userId, eventId);
            if (joinedAt.isPresent()) {
                result = new JoinAttemptResult(
                        Outcome.ALREADY_JOINED, count, event.getCapacity(), joinedAt.get());
            }
            else if (!event.isOpenAt(now)) {
                result = new JoinAttemptResult(Outcome.EVENT_CLOSED, count, event.getCapacity(), null);
            }
            else if (!event.hasRemainingSeat(count)) {
                result = new JoinAttemptResult(Outcome.FULL, count, event.getCapacity(), null);
            }
            else {
                insertParticipation(connection, userId, eventId, now);
                result = new JoinAttemptResult(Outcome.JOINED, count + 1, event.getCapacity(), now);
            }
        }
        return result;
    }

    private static void insertParticipation(Connection connection, String userId, String eventId,
                                            Instant now) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO participations (user_id, event_id, joined_at_ms) VALUES (?, ?, ?)")) {
            final int next = bindStrings(insert, userId, eventId);
            insert.setLong(next, now.toEpochMilli());
            insert.executeUpdate();
        }
    }

    @Override
    public List<Event> findJoinedEventsBetween(
            String userId, Instant inclusiveStart, Instant exclusiveEnd) {
        final String query = SELECT_EVENTS
                + " JOIN participations p ON p.event_id = e.event_id "
                + "WHERE p.user_id = ? AND e.starts_at_ms >= ? AND e.starts_at_ms < ?" + EVENT_ORDER;
        final List<Event> result = new ArrayList<>();
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(query)) {
            final int next = bindStrings(sql, userId);
            sql.setLong(next, inclusiveStart.toEpochMilli());
            sql.setLong(next + 1, exclusiveEnd.toEpochMilli());
            readEvents(sql, result);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read the calendar.", ex);
        }
        return result;
    }

    private static Optional<Event> findEventIn(Connection connection, String eventId) throws SQLException {
        final List<Event> found = new ArrayList<>();
        try (PreparedStatement sql = connection.prepareStatement(SELECT_EVENTS + " WHERE e.event_id = ?")) {
            sql.setString(1, eventId);
            readEvents(sql, found);
        }
        return found.stream().findFirst();
    }

    private static Optional<Instant> findJoinedAt(Connection connection, String userId, String eventId)
            throws SQLException {
        Optional<Instant> joinedAt = Optional.empty();
        try (PreparedStatement sql = connection.prepareStatement(FIND_PARTICIPATION)) {
            sql.setString(1, userId);
            sql.setString(2, eventId);
            try (ResultSet row = sql.executeQuery()) {
                if (row.next()) {
                    joinedAt = Optional.of(Instant.ofEpochMilli(row.getLong("joined_at_ms")));
                }
            }
        }
        return joinedAt;
    }

    private List<Event> queryEvents(String query, String... parameters) {
        final List<Event> result = new ArrayList<>();
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(query)) {
            bindStrings(sql, parameters);
            readEvents(sql, result);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read events.", ex);
        }
        return result;
    }

    private static void readEvents(PreparedStatement sql, List<Event> into) throws SQLException {
        try (ResultSet rows = sql.executeQuery()) {
            while (rows.next()) {
                into.add(new Event(
                        rows.getString("event_id"),
                        rows.getString("community_id"),
                        rows.getString("creator_id"),
                        rows.getString("title"),
                        rows.getString("description"),
                        rows.getInt("capacity"),
                        Instant.ofEpochMilli(rows.getLong("starts_at_ms"))));
            }
        }
    }

    private int countRows(String query, String... parameters) {
        try (Connection connection = database.openConnection()) {
            return countRows(connection, query, parameters);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read participation data.", ex);
        }
    }

    private static int countRows(Connection connection, String query, String... parameters)
            throws SQLException {
        int count = 0;
        try (PreparedStatement sql = connection.prepareStatement(query)) {
            bindStrings(sql, parameters);
            try (ResultSet row = sql.executeQuery()) {
                if (row.next()) {
                    count = row.getInt(1);
                }
            }
        }
        return count;
    }

    /**
     * Binds string parameters starting at index 1.
     *
     * @param sql prepared statement
     * @param values values for parameters 1, 2, ...
     * @return the index of the next unbound parameter
     * @throws SQLException if a value cannot be bound
     */
    private static int bindStrings(PreparedStatement sql, String... values) throws SQLException {
        int parameter = 1;
        for (String value : values) {
            sql.setString(parameter, value);
            parameter++;
        }
        return parameter;
    }
}
