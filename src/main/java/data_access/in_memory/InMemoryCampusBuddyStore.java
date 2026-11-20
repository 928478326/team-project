package data_access.in_memory;

import entity.BuddyRating;
import entity.Community;
import entity.CourseEnrollment;
import entity.Event;
import entity.Participation;
import entity.PasswordHasher;
import entity.User;
import entity.WeekendStyle;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Shared thread-safe in-memory data store used by the {@code InMemory*DataAccess} implementations.
 *
 * <p>Allows Tracks B, C, and D to develop, run, and unit-test their entire vertical slices
 * immediately from Day 1 without waiting for Track A's SQLite persistence implementation.
 */
public final class InMemoryCampusBuddyStore {

    /** Demo password shared by all 5 seeded demo accounts. */
    public static final String DEMO_PASSWORD = "pass1234";

    private final Map<String, User> usersById = new LinkedHashMap<>();
    private final Map<String, String> userIdByUsername = new LinkedHashMap<>();
    private final Map<String, Community> communitiesById = new LinkedHashMap<>();
    private final Map<String, Event> eventsById = new LinkedHashMap<>();
    private final Map<String, Participation> participationsByKey = new LinkedHashMap<>();
    private final Map<String, CourseEnrollment> enrollmentsByKey = new LinkedHashMap<>();
    private final Map<String, BuddyRating> ratingsByKey = new LinkedHashMap<>();

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
        InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
        String demoHash = passwordHasher.hash(DEMO_PASSWORD);
        ZoneId toronto = ZoneId.of("America/Toronto");

        User james = new User(
                "u-james",
                "james",
                demoHash,
                "James",
                "CS undergrad organizing study groups & boardgame nights.",
                "discord: james_uoft",
                WeekendStyle.SOCIAL_EVENTS,
                Set.of(Community.STUDY_ID, Community.SPORTS_ID, Community.GAMING_ID));
        User herry = new User(
                "u-herry",
                "herry",
                demoHash,
                "Herry",
                "Always down for Robarts late-night coding and coffee.",
                "discord: herry_cs",
                WeekendStyle.STUDY_FOCUS,
                Set.of(Community.STUDY_ID, Community.FOOD_ID));
        User gracie = new User(
                "u-gracie",
                "gracie",
                demoHash,
                "Gracie",
                "Boardgames, bubble tea, and CSC207 debugging sessions.",
                "ig: @gracie.uoft",
                WeekendStyle.INDOOR_CHILL,
                Set.of(Community.STUDY_ID, Community.GAMING_ID, Community.CHILL_ID));
        User henry = new User(
                "u-henry",
                "henry",
                demoHash,
                "Henry",
                "Looking for cozy weekend boardgame & study buddies.",
                "discord: henry_csc207",
                WeekendStyle.INDOOR_CHILL,
                Set.of(Community.STUDY_ID, Community.GAMING_ID, Community.CHILL_ID));
        User alex = new User(
                "u-alex",
                "alex",
                demoHash,
                "Alex",
                "Varsity gym & pickup basketball every Saturday morning.",
                "ig: @alex.athletics",
                WeekendStyle.OUTDOOR_ACTIVE,
                Set.of(Community.SPORTS_ID, Community.FOOD_ID));

        store.putUser(james);
        store.putUser(herry);
        store.putUser(gracie);
        store.putUser(henry);
        store.putUser(alex);

        Instant dec03 = ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, toronto).toInstant();
        Instant dec06 = ZonedDateTime.of(2026, 12, 6, 18, 0, 0, 0, toronto).toInstant();
        Instant nov15 = ZonedDateTime.of(2026, 11, 15, 16, 0, 0, 0, toronto).toInstant();

        Event reviewEvent = new Event(
                "e-csc207-review",
                Community.STUDY_ID,
                james.getUserId(),
                "CSC207 Final Review @ Bahen",
                "Reviewing Clean Architecture, SOLID, and Design Patterns.",
                4,
                dec03);
        Event boardgameEvent = new Event(
                "e-boardgame-night",
                Community.GAMING_ID,
                gracie.getUserId(),
                "Weekend Boardgame Night",
                "Catan & Codenames near St. George campus.",
                3,
                dec06);
        Event pastHotpotEvent = new Event(
                "e-midterm-hotpot",
                Community.FOOD_ID,
                james.getUserId(),
                "Post-Midterm Hotpot Hangout",
                "Celebrating finishing CSC207 Midterm 2!",
                5,
                nov15);

        store.putEvent(reviewEvent);
        store.putEvent(boardgameEvent);
        store.putEvent(pastHotpotEvent);

        Instant joinedTime = ZonedDateTime.of(2026, 11, 10, 12, 0, 0, 0, toronto).toInstant();
        store.putParticipation(new Participation(gracie.getUserId(), pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(henry.getUserId(), pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(alex.getUserId(), pastHotpotEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(james.getUserId(), reviewEvent.getEventId(), joinedTime));
        store.putParticipation(new Participation(gracie.getUserId(), boardgameEvent.getEventId(), joinedTime));

        store.putEnrollment(new CourseEnrollment(gracie.getUserId(), "CSC207H1F", "LEC0101", "TUT0201"));
        store.putEnrollment(new CourseEnrollment(henry.getUserId(), "CSC207H1F", "LEC0101", "TUT0201"));
        store.putEnrollment(new CourseEnrollment(james.getUserId(), "CSC207H1F", "LEC0101", "TUT0301"));
        store.putEnrollment(new CourseEnrollment(alex.getUserId(), "CSC207H1F", "LEC5101", ""));
        store.putEnrollment(new CourseEnrollment(gracie.getUserId(), "STA247H1F", "LEC0101", "TUT0101"));

        store.putRating(
                new BuddyRating(
                        henry.getUserId(),
                        gracie.getUserId(),
                        pastHotpotEvent.getEventId(),
                        5,
                        true,
                        "Super friendly and organized!",
                        joinedTime));

        return store;
    }

    public synchronized Map<String, User> usersById() {
        return usersById;
    }

    public synchronized Map<String, String> userIdByUsername() {
        return userIdByUsername;
    }

    public synchronized Map<String, Community> communitiesById() {
        return communitiesById;
    }

    public synchronized Map<String, Event> eventsById() {
        return eventsById;
    }

    public synchronized Map<String, Participation> participationsByKey() {
        return participationsByKey;
    }

    public synchronized Map<String, CourseEnrollment> enrollmentsByKey() {
        return enrollmentsByKey;
    }

    public synchronized Map<String, BuddyRating> ratingsByKey() {
        return ratingsByKey;
    }

    public synchronized void putUser(User user) {
        usersById.put(user.getUserId(), user);
        userIdByUsername.put(user.getUsername(), user.getUserId());
    }

    public synchronized void putEvent(Event event) {
        eventsById.put(event.getEventId(), event);
    }

    public synchronized void putParticipation(Participation participation) {
        participationsByKey.put(
                participationKey(participation.getUserId(), participation.getEventId()),
                participation);
    }

    public synchronized void putEnrollment(CourseEnrollment enrollment) {
        enrollmentsByKey.put(
                enrollmentKey(enrollment.getUserId(), enrollment.getCourseCode()),
                enrollment);
    }

    public synchronized void putRating(BuddyRating rating) {
        ratingsByKey.put(
                ratingKey(rating.getRaterId(), rating.getRateeId(), rating.getEventId()),
                rating);
    }

    public static String participationKey(String userId, String eventId) {
        return userId + "|" + eventId;
    }

    public static String enrollmentKey(String userId, String courseCode) {
        return userId + "|" + courseCode;
    }

    public static String ratingKey(String raterId, String rateeId, String eventId) {
        return raterId + "|" + rateeId + "|" + eventId;
    }
}
