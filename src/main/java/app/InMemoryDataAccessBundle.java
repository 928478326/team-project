package app;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryEventDataAccess;
import data_access.in_memory.InMemoryRatingDataAccess;
import data_access.in_memory.InMemoryRecommendationDataAccess;
import data_access.in_memory.InMemoryScheduleDataAccess;
import data_access.in_memory.InMemoryUserDataAccess;
import data_access.in_memory.Sha256PasswordHasher;
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
 * Working in-memory {@link DataAccessBundle} backed by {@link InMemoryCampusBuddyStore}.
 * Enables every developer track to run and verify features independently from Day 1.
 */
public final class InMemoryDataAccessBundle extends AbstractGatewayBundle {

    private final PasswordHasher passwordHasher = new Sha256PasswordHasher();
    private final InMemoryCampusBuddyStore store;
    private final InMemoryUserDataAccess userDao;
    private final InMemoryEventDataAccess eventDao;
    private final InMemoryScheduleDataAccess scheduleDao;
    private final InMemoryRatingDataAccess ratingDao;
    private final InMemoryRecommendationDataAccess recommendationDao;

    public InMemoryDataAccessBundle() {
        this.store = InMemoryCampusBuddyStore.createSeeded(passwordHasher);
        this.userDao = new InMemoryUserDataAccess(store);
        this.eventDao = new InMemoryEventDataAccess(store);
        this.scheduleDao = new InMemoryScheduleDataAccess(store);
        this.ratingDao = new InMemoryRatingDataAccess(store);
        this.recommendationDao = new InMemoryRecommendationDataAccess(store);
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
