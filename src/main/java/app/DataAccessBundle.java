package app;

import entity.PasswordHasher;
import use_case.browse_events.BrowseEventsDataAccessInterface;
import use_case.create_event.CreateEventDataAccessInterface;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.edit_schedule.CourseCatalogGateway;
import use_case.edit_schedule.EditScheduleDataAccessInterface;
import use_case.join_event.JoinEventDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.match_course_buddies.MatchCourseBuddiesDataAccessInterface;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.recommend_buddies.LlmRecommendationGateway;
import use_case.recommend_buddies.RecommendBuddiesDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;
import use_case.view_calendar.CalendarExportGateway;
import use_case.view_calendar.PublicHolidayGateway;
import use_case.view_calendar.ViewCalendarDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;

/**
 * Abstract Factory interface supplying every Data Access Interface and External Gateway
 * needed by {@link AppBuilder}.
 *
 * <p>Allows the composition root to switch seamlessly between {@link InMemoryDataAccessBundle}
 * (Day-1 parallel development &amp; fast testing) and {@link SqliteDataAccessBundle} (production)
 * without modifying a single Interactor, Presenter, Controller, or View (DIP + Abstract Factory).
 */
public interface DataAccessBundle {

    PasswordHasher passwordHasher();

    SignupDataAccessInterface signupDataAccess();

    LoginDataAccessInterface loginDataAccess();

    BrowseEventsDataAccessInterface browseEventsDataAccess();

    CreateEventDataAccessInterface createEventDataAccess();

    JoinEventDataAccessInterface joinEventDataAccess();

    ViewCalendarDataAccessInterface viewCalendarDataAccess();

    PublicHolidayGateway publicHolidayGateway();

    CalendarExportGateway calendarExportGateway();

    EditScheduleDataAccessInterface editScheduleDataAccess();

    CourseCatalogGateway courseCatalogGateway();

    MatchCourseBuddiesDataAccessInterface matchCourseBuddiesDataAccess();

    ViewRateableBuddiesDataAccessInterface viewRateableBuddiesDataAccess();

    RateBuddyDataAccessInterface rateBuddyDataAccess();

    EditProfileDataAccessInterface editProfileDataAccess();

    RecommendBuddiesDataAccessInterface recommendBuddiesDataAccess();

    LlmRecommendationGateway primaryLlmGateway();

    LlmRecommendationGateway fallbackLlmGateway();
}
