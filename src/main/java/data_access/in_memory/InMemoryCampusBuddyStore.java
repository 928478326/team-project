package data_access.in_memory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import entity.BuddyRating;
import entity.Community;
import entity.CourseEnrollment;
import entity.Event;
import entity.Participation;
import entity.PasswordHasher;
import entity.PublicProfile;
import entity.User;
import entity.WeekendStyle;

/**
 * Shared thread-safe in-memory data store used by the {@code InMemory*DataAccess} implementations.
 *
 * <p>Allows Tracks B, C, and D to develop, run, and unit-test their entire vertical slices
 * immediately from Day 1 without waiting for SQLite persistence.
 */
public final class InMemoryCampusBuddyStore {

    /** Demo password shared by all 5 seeded demo accounts. */
    public static final String DEMO_PASSWORD = "pass1234";

    private static final String KEY_SEPARATOR = "|";
    private static final String USER_JAMES = "u-james";
    private static final String USER_HERRY = "u-herry";
    private static final String USER_GRACIE = "u-gracie";
    private static final String USER_HENRY = "u-henry";
    private static final String USER_ALEX = "u-alex";
    private static final String COURSE_CSC207 = "CSC207H1F";
    private static final String LEC_0101 = "LEC0101";

    private static final int DEMO_YEAR = 2026;
    private static final int NOVEMBER = 11;
    private static final int DECEMBER = 12;
    private static final int DAY_THREE = 3;
    private static final int DAY_SIX = 6;
    private static final int DAY_TEN = 10;
    private static final int DAY_FIFTEEN = 15;
    private static final int HOUR_NOON = 12;
    private static final int HOUR_TWO_PM = 14;
    private static final int HOUR_FOUR_PM = 16;
    private static final int HOUR_SIX_PM = 18;
    private static final int CAPACITY_THREE = 3;
    private static final int CAPACITY_FOUR = 4;
    private static final int CAPACITY_FIVE = 5;
    private static final int FIVE_STAR_RATING = 5;

    private final Map<String, User> usersById = new LinkedHashMap<>();
    private final Map<String, String> userIdByUsername = new LinkedHashMap<>();
    private final Map<String, Community> communitiesById = new LinkedHashMap<>();
    private final Map<String, Event> eventsById = new LinkedHashMap<>();
    private final Map<String, Participation> participationsByKey = new LinkedHashMap<>();
    private final Map<String, CourseEnrollment> enrollmentsByKey = new LinkedHashMap<>();
    private final Map<String, BuddyRating> ratingsByKey = new LinkedHashMap<>();

    /** Constructs a store initialized with the default campus communities. */
    public InMemoryCampusBuddyStore() {
        for (Community community : Community.defaultCommunities()) {
            communitiesById.put(community.getCommunityId(), community);
        }
    }

    /**
     * Creates a store pre-populated with 5 realistic UofT demo students, sample events,
     * shared participations, course schedules, and initial ratings.
     *
     * @param passwordHasher password hasher used to encode demo passwords
     * @return seeded in-memory store
     */
    public static InMemoryCampusBuddyStore createSeeded(PasswordHasher passwordHasher) {
        final InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
        final String demoHash = passwordHasher.hash(DEMO_PASSWORD);
        seedUsers(store, demoHash);
        seedEventsAndSocialGraph(store);
        return store;
    }

    private static void seedUsers(InMemoryCampusBuddyStore store, String demoHash) {
        store.putUser(createDemoUser(
                USER_JAMES,
                "james",
                demoHash,
                new PublicProfile(
                        "James",
                        "CS undergrad organizing study groups & boardgame nights.",
                        "discord: james_uoft",
                        WeekendStyle.SOCIAL_EVENTS,
                        Set.of(Community.STUDY_ID, Community.SPORTS_ID, Community.GAMING_ID))));
        store.putUser(createDemoUser(
                USER_HERRY,
                "herry",
                demoHash,
                new PublicProfile(
                        "Herry",
                        "Always down for Robarts late-night coding and coffee.",
                        "discord: herry_cs",
                        WeekendStyle.STUDY_FOCUS,
                        Set.of(Community.STUDY_ID, Community.FOOD_ID))));
        store.putUser(createDemoUser(
                USER_GRACIE,
                "gracie",
                demoHash,
                new PublicProfile(
                        "Gracie",
                        "Boardgames, bubble tea, and CSC207 debugging sessions.",
                        "ig: @gracie.uoft",
                        WeekendStyle.INDOOR_CHILL,
                        Set.of(Community.STUDY_ID, Community.GAMING_ID, Community.CHILL_ID))));
        store.putUser(createDemoUser(
                USER_HENRY,
                "henry",
                demoHash,
                new PublicProfile(
                        "Henry",
                        "Looking for cozy weekend boardgame & study buddies.",
                        "discord: henry_csc207",
                        WeekendStyle.INDOOR_CHILL,
                        Set.of(Community.STUDY_ID, Community.GAMING_ID, Community.CHILL_ID))));
        store.putUser(createDemoUser(
                USER_ALEX,
                "alex",
                demoHash,
                new PublicProfile(
                        "Alex",
                        "Varsity gym & pickup basketball every Saturday morning.",
                        "ig: @alex.athletics",
                        WeekendStyle.OUTDOOR_ACTIVE,
                        Set.of(Community.SPORTS_ID, Community.FOOD_ID))));
    }

    private static void seedEventsAndSocialGraph(InMemoryCampusBuddyStore store) {
        final ZoneId toronto = ZoneId.of("America/Toronto");
        final Instant dec03 =
                ZonedDateTime.of(DEMO_YEAR, DECEMBER, DAY_THREE, HOUR_TWO_PM, 0, 0, 0, toronto)
                        .toInstant();
        final Instant dec06 =
                ZonedDateTime.of(DEMO_YEAR, DECEMBER, DAY_SIX, HOUR_SIX_PM, 0, 0, 0, toronto)
                        .toInstant();
        final Instant nov15 =
                ZonedDateTime.of(DEMO_YEAR, NOVEMBER, DAY_FIFTEEN, HOUR_FOUR_PM, 0, 0, 0, toronto)
                        .toInstant();
        final Instant joinedTime =
                ZonedDateTime.of(DEMO_YEAR, NOVEMBER, DAY_TEN, HOUR_NOON, 0, 0, 0, toronto)
                        .toInstant();

        final Event reviewEvent = new Event(
                "e-csc207-review",
                Community.STUDY_ID,
                USER_JAMES,
                "CSC207 Final Review @ Bahen",
                "Reviewing Clean Architecture, SOLID, and Design Patterns.",
                CAPACITY_FOUR,
                dec03);
        final Event boardgameEvent = new Event(
                "e-boardgame-night",
                Community.GAMING_ID,
                USER_GRACIE,
                "Weekend Boardgame Night",
                "Catan & Codenames near St. George campus.",
                CAPACITY_THREE,
                dec06);
        final Event pastHotpotEvent = new Event(
                "e-midterm-hotpot",
                Community.FOOD_ID,
                USER_JAMES,
                "Post-Midterm Hotpot Hangout",
                "Celebrating finishing CSC207 Midterm 2!",
                CAPACITY_FIVE,
                nov15);

        store.putEvent(reviewEvent);
        store.putEvent(boardgameEvent);
        store.putEvent(pastHotpotEvent);

        store.putParticipation(new Participation(USER_GRACIE, pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(USER_HENRY, pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(USER_ALEX, pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(USER_JAMES, reviewEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(USER_GRACIE, boardgameEvent.getEventId(), joinedTime));

        store.putEnrollment(new CourseEnrollment(USER_GRACIE, COURSE_CSC207, LEC_0101, "TUT0201"));
        store.putEnrollment(new CourseEnrollment(USER_HENRY, COURSE_CSC207, LEC_0101, "TUT0201"));
        store.putEnrollment(new CourseEnrollment(USER_JAMES, COURSE_CSC207, LEC_0101, "TUT0301"));
        store.putEnrollment(new CourseEnrollment(USER_ALEX, COURSE_CSC207, "LEC5101", ""));
        store.putEnrollment(new CourseEnrollment(USER_GRACIE, "STA247H1F", LEC_0101, "TUT0101"));

        store.putRating(new BuddyRating(
                USER_HENRY,
                USER_GRACIE,
                pastHotpotEvent.getEventId(),
                FIVE_STAR_RATING,
                true,
                "Super friendly and organized!",
                joinedTime));
    }

    private static User createDemoUser(
            String userId, String username, String passwordHash, PublicProfile profile) {
        return new User(userId, username, passwordHash, profile);
    }

    /**
     * Returns the internal map of users keyed by user ID.
     *
     * @return users map
     */
    public synchronized Map<String, User> usersById() {
        return usersById;
    }

    /**
     * Returns the internal lookup map of user IDs keyed by normalized username.
     *
     * @return username-to-ID map
     */
    public synchronized Map<String, String> userIdByUsername() {
        return userIdByUsername;
    }

    /**
     * Returns the internal map of communities keyed by community ID.
     *
     * @return communities map
     */
    public synchronized Map<String, Community> communitiesById() {
        return communitiesById;
    }

    /**
     * Returns the internal map of events keyed by event ID.
     *
     * @return events map
     */
    public synchronized Map<String, Event> eventsById() {
        return eventsById;
    }

    /**
     * Returns the internal map of event participations keyed by composite key.
     *
     * @return participations map
     */
    public synchronized Map<String, Participation> participationsByKey() {
        return participationsByKey;
    }

    /**
     * Returns the internal map of course enrollments keyed by composite key.
     *
     * @return enrollments map
     */
    public synchronized Map<String, CourseEnrollment> enrollmentsByKey() {
        return enrollmentsByKey;
    }

    /**
     * Returns the internal map of buddy ratings keyed by composite key.
     *
     * @return ratings map
     */
    public synchronized Map<String, BuddyRating> ratingsByKey() {
        return ratingsByKey;
    }

    /**
     * Inserts or replaces a user in the store.
     *
     * @param user user entity to store
     */
    public synchronized void putUser(User user) {
        usersById.put(user.getUserId(), user);
        userIdByUsername.put(user.getUsername(), user.getUserId());
    }

    /**
     * Inserts or replaces an event in the store.
     *
     * @param event event entity to store
     */
    public synchronized void putEvent(Event event) {
        eventsById.put(event.getEventId(), event);
    }

    /**
     * Inserts or replaces a participation record in the store.
     *
     * @param participation participation entity to store
     */
    public synchronized void putParticipation(Participation participation) {
        participationsByKey.put(
                participationKey(participation.getUserId(), participation.getEventId()),
                participation);
    }

    /**
     * Inserts or replaces a course enrollment record in the store.
     *
     * @param enrollment enrollment entity to store
     */
    public synchronized void putEnrollment(CourseEnrollment enrollment) {
        enrollmentsByKey.put(
                enrollmentKey(enrollment.getUserId(), enrollment.getCourseCode()),
                enrollment);
    }

    /**
     * Inserts or replaces a buddy rating record in the store.
     *
     * @param rating buddy rating entity to store
     */
    public synchronized void putRating(BuddyRating rating) {
        ratingsByKey.put(
                ratingKey(rating.getRaterId(), rating.getRateeId(), rating.getEventId()),
                rating);
    }

    /**
     * Builds the composite lookup key for a user-event participation.
     *
     * @param userId user ID
     * @param eventId event ID
     * @return composite key string
     */
    public static String participationKey(String userId, String eventId) {
        return userId + KEY_SEPARATOR + eventId;
    }

    /**
     * Builds the composite lookup key for a user-course enrollment.
     *
     * @param userId user ID
     * @param courseCode course code
     * @return composite key string
     */
    public static String enrollmentKey(String userId, String courseCode) {
        return userId + KEY_SEPARATOR + courseCode;
    }

    /**
     * Builds the composite lookup key for a buddy rating.
     *
     * @param raterId rater user ID
     * @param rateeId ratee user ID
     * @param eventId shared event ID
     * @return composite key string
     */
    public static String ratingKey(String raterId, String rateeId, String eventId) {
        return raterId + KEY_SEPARATOR + rateeId + KEY_SEPARATOR + eventId;
    }
}
