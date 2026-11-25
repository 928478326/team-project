package app;

import data_access.sqlite.Pbkdf2PasswordHasher;
import data_access.sqlite.SqliteDatabase;
import data_access.sqlite.SqliteEventDataAccess;
import data_access.sqlite.SqliteRatingDataAccess;
import data_access.sqlite.SqliteRecommendationDataAccess;
import data_access.sqlite.SqliteScheduleDataAccess;
import data_access.sqlite.SqliteUserDataAccess;
import entity.PasswordHasher;
import use_case.browse_events.BrowseEventsDataAccessInterface;
import use_case.create_event.CreateEventDataAccessInterface;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.edit_schedule.EditScheduleDataAccessInterface;
import use_case.join_event.JoinEventDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.match_course_buddies.MatchCourseBuddiesDataAccessInterface;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.recommend_buddies.RecommendBuddiesDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;
import use_case.view_calendar.ViewCalendarDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;

/**
 * SQLite wiring for the persistent account MVP. Feature DAOs share the database handle.
 */
public final class SqliteDataAccessBundle extends AbstractGatewayBundle {

    private final PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
    private final SqliteUserDataAccess userDao;
    private final SqliteEventDataAccess eventDao;
    private final SqliteScheduleDataAccess scheduleDao;
    private final SqliteRatingDataAccess ratingDao;
    private final SqliteRecommendationDataAccess recommendationDao;

    public SqliteDataAccessBundle(String dbFilePath) {
        final SqliteDatabase database = new SqliteDatabase(dbFilePath);
        database.initializeSchema();

        this.userDao = new SqliteUserDataAccess(database);
        this.eventDao = new SqliteEventDataAccess(database);
        this.scheduleDao = new SqliteScheduleDataAccess(database);
        this.ratingDao = new SqliteRatingDataAccess(database);
        this.recommendationDao = new SqliteRecommendationDataAccess(database);
    }

    @Override
    public PasswordHasher passwordHasher() {
        return passwordHasher;
    }

    @Override
    public SignupDataAccessInterface signupDataAccess() {
        return userDao;
    }

    @Override
    public LoginDataAccessInterface loginDataAccess() {
        return userDao;
    }

    @Override
    public BrowseEventsDataAccessInterface browseEventsDataAccess() {
        return eventDao;
    }

    @Override
    public CreateEventDataAccessInterface createEventDataAccess() {
        return eventDao;
    }

    @Override
    public JoinEventDataAccessInterface joinEventDataAccess() {
        return eventDao;
    }

    @Override
    public ViewCalendarDataAccessInterface viewCalendarDataAccess() {
        return eventDao;
    }

    @Override
    public EditScheduleDataAccessInterface editScheduleDataAccess() {
        return scheduleDao;
    }

    @Override
    public MatchCourseBuddiesDataAccessInterface matchCourseBuddiesDataAccess() {
        return scheduleDao;
    }

    @Override
    public ViewRateableBuddiesDataAccessInterface viewRateableBuddiesDataAccess() {
        return ratingDao;
    }

    @Override
    public RateBuddyDataAccessInterface rateBuddyDataAccess() {
        return ratingDao;
    }

    @Override
    public EditProfileDataAccessInterface editProfileDataAccess() {
        return userDao;
    }

    @Override
    public RecommendBuddiesDataAccessInterface recommendBuddiesDataAccess() {
        return recommendationDao;
    }
}
