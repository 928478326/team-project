package app;

import entity.BuddyScoringStrategy;
import entity.CommonUserFactory;
import entity.CourseMatchStrategy;
import entity.DefaultBuddyScoringStrategy;
import entity.DefaultCourseMatchStrategy;
import entity.User;
import entity.UserFactory;
import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.LoginController;
import interface_adapter.account.LoginPresenter;
import interface_adapter.account.LogoutController;
import interface_adapter.account.LogoutPresenter;
import interface_adapter.account.SignupController;
import interface_adapter.account.SignupPresenter;
import interface_adapter.buddy_rating.BuddyRatingViewModel;
import interface_adapter.buddy_rating.RateBuddyController;
import interface_adapter.buddy_rating.RateBuddyPresenter;
import interface_adapter.buddy_rating.ViewRateableBuddiesController;
import interface_adapter.buddy_rating.ViewRateableBuddiesPresenter;
import interface_adapter.calendar.CalendarViewModel;
import interface_adapter.calendar.ViewCalendarController;
import interface_adapter.calendar.ViewCalendarPresenter;
import interface_adapter.community.BrowseEventsController;
import interface_adapter.community.BrowseEventsPresenter;
import interface_adapter.community.CommunityEventsViewModel;
import interface_adapter.community.CreateEventController;
import interface_adapter.community.CreateEventPresenter;
import interface_adapter.community.JoinEventController;
import interface_adapter.community.JoinEventPresenter;
import interface_adapter.course_match.CourseMatchViewModel;
import interface_adapter.course_match.EditScheduleController;
import interface_adapter.course_match.EditSchedulePresenter;
import interface_adapter.course_match.MatchCourseBuddiesController;
import interface_adapter.course_match.MatchCourseBuddiesPresenter;
import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.EditProfilePresenter;
import interface_adapter.recommendation.RecommendBuddiesController;
import interface_adapter.recommendation.RecommendBuddiesPresenter;
import interface_adapter.recommendation.RecommendationViewModel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import use_case.browse_events.BrowseEventsInputBoundary;
import use_case.browse_events.BrowseEventsInteractor;
import use_case.browse_events.BrowseEventsOutputBoundary;
import use_case.create_event.CreateEventInputBoundary;
import use_case.create_event.CreateEventInteractor;
import use_case.create_event.CreateEventOutputBoundary;
import use_case.edit_profile.EditProfileInputBoundary;
import use_case.edit_profile.EditProfileInteractor;
import use_case.edit_profile.EditProfileOutputBoundary;
import use_case.edit_schedule.EditScheduleInputBoundary;
import use_case.edit_schedule.EditScheduleInteractor;
import use_case.edit_schedule.EditScheduleOutputBoundary;
import use_case.join_event.JoinEventInputBoundary;
import use_case.join_event.JoinEventInteractor;
import use_case.join_event.JoinEventOutputBoundary;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutInteractor;
import use_case.logout.LogoutOutputBoundary;
import use_case.match_course_buddies.MatchCourseBuddiesInputBoundary;
import use_case.match_course_buddies.MatchCourseBuddiesInteractor;
import use_case.match_course_buddies.MatchCourseBuddiesOutputBoundary;
import use_case.rate_buddy.RateBuddyInputBoundary;
import use_case.rate_buddy.RateBuddyInteractor;
import use_case.rate_buddy.RateBuddyOutputBoundary;
import use_case.recommend_buddies.RecommendBuddiesInputBoundary;
import use_case.recommend_buddies.RecommendBuddiesInteractor;
import use_case.recommend_buddies.RecommendBuddiesOutputBoundary;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
import use_case.view_calendar.ViewCalendarInputBoundary;
import use_case.view_calendar.ViewCalendarInteractor;
import use_case.view_calendar.ViewCalendarOutputBoundary;
import use_case.view_rateable_buddies.ViewRateableBuddiesInputBoundary;
import use_case.view_rateable_buddies.ViewRateableBuddiesInteractor;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputBoundary;
import view.AccountView;
import view.BuddyRatingView;
import view.CalendarView;
import view.CommunityEventsView;
import view.CourseMatchView;
import view.MainShellView;
import view.ProfileAndRecommendView;
import view.ViewManager;

/**
 * Fluent Builder assembling the entire CampusBuddy Clean Architecture engine
 * (matching CSC207 Week 8's Main Component &amp; Builder pattern).
 */
public final class AppBuilder {

    public static final ZoneId TORONTO_ZONE = ZoneId.of("America/Toronto");
    private static final int DEFAULT_WIDTH = 1040;
    private static final int DEFAULT_HEIGHT = 720;

    private final DataAccessBundle dataAccessBundle;
    private final Clock clock;
    private final ZoneId zoneId;

    private final UserFactory userFactory = new CommonUserFactory();
    private final CourseMatchStrategy courseMatchStrategy = new DefaultCourseMatchStrategy();
    private final BuddyScoringStrategy buddyScoringStrategy = new DefaultBuddyScoringStrategy();

    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootCardPanel = new JPanel(rootCardLayout);
    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    private final SessionViewModel sessionViewModel = new SessionViewModel();
    @SuppressWarnings("unused")
    private final ViewManager viewManager =
            new ViewManager(rootCardPanel, rootCardLayout, viewManagerModel);

    private AccountViewModel accountViewModel;
    private AccountView accountView;
    private MainShellView mainShellView;

    private CommunityEventsViewModel communityEventsViewModel;
    private CommunityEventsView communityEventsView;

    private CalendarViewModel calendarViewModel;
    private CalendarView calendarView;

    private CourseMatchViewModel courseMatchViewModel;
    private CourseMatchView courseMatchView;

    private BuddyRatingViewModel buddyRatingViewModel;
    private BuddyRatingView buddyRatingView;

    private RecommendationViewModel recommendationViewModel;
    private ProfileAndRecommendView profileAndRecommendView;

    public AppBuilder(DataAccessBundle dataAccessBundle, Clock clock, ZoneId zoneId) {
        this.dataAccessBundle =
                Objects.requireNonNull(dataAccessBundle, "dataAccessBundle must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    public AppBuilder addAccountAndShellViews() {
        accountViewModel = new AccountViewModel();
        accountView = new AccountView(accountViewModel);
        rootCardPanel.add(accountView, accountView.getViewName());

        mainShellView = new MainShellView(sessionViewModel);
        rootCardPanel.add(mainShellView, mainShellView.getViewName());
        return this;
    }

    public AppBuilder addCommunityAndCalendarViews() {
        communityEventsViewModel = new CommunityEventsViewModel();
        communityEventsView = new CommunityEventsView(communityEventsViewModel, sessionViewModel);
        mainShellView.addFeatureTab(
                communityEventsView.getViewName(),
                "Community",
                communityEventsView,
                communityEventsView::refreshData);

        calendarViewModel = new CalendarViewModel();
        calendarView = new CalendarView(calendarViewModel, sessionViewModel);
        mainShellView.addFeatureTab(
                calendarView.getViewName(),
                "Calendar",
                calendarView,
                calendarView::refreshData);
        return this;
    }

    public AppBuilder addCourseMatchAndRatingViews() {
        courseMatchViewModel = new CourseMatchViewModel();
        courseMatchView = new CourseMatchView(courseMatchViewModel, sessionViewModel);
        mainShellView.addFeatureTab(
                courseMatchView.getViewName(),
                "Course Match",
                courseMatchView,
                courseMatchView::refreshData);

        buddyRatingViewModel = new BuddyRatingViewModel();
        buddyRatingView = new BuddyRatingView(buddyRatingViewModel, sessionViewModel);
        mainShellView.addFeatureTab(
                buddyRatingView.getViewName(),
                "Ratings",
                buddyRatingView,
                buddyRatingView::refreshData);
        return this;
    }

    public AppBuilder addProfileAndRecommendView() {
        recommendationViewModel = new RecommendationViewModel();
        profileAndRecommendView =
                new ProfileAndRecommendView(recommendationViewModel, sessionViewModel);
        mainShellView.addFeatureTab(
                profileAndRecommendView.getViewName(),
                "Profile & Recommend",
                profileAndRecommendView,
                profileAndRecommendView::refreshData);
        return this;
    }

    /** Wires Track A use cases: Signup, Login, Logout. */
    public AppBuilder addAuthUseCases() {
        SignupOutputBoundary signupPresenter =
                new SignupPresenter(accountViewModel, sessionViewModel, viewManagerModel);
        SignupInputBoundary signupInteractor =
                new SignupInteractor(
                        dataAccessBundle.signupDataAccess(),
                        signupPresenter,
                        userFactory,
                        dataAccessBundle.passwordHasher());
        accountView.setSignupController(new SignupController(signupInteractor));

        LoginOutputBoundary loginPresenter =
                new LoginPresenter(accountViewModel, sessionViewModel, viewManagerModel);
        LoginInputBoundary loginInteractor =
                new LoginInteractor(
                        dataAccessBundle.loginDataAccess(),
                        loginPresenter,
                        dataAccessBundle.passwordHasher());
        accountView.setLoginController(new LoginController(loginInteractor));

        LogoutOutputBoundary logoutPresenter =
                new LogoutPresenter(accountViewModel, sessionViewModel, viewManagerModel);
        LogoutInputBoundary logoutInteractor = new LogoutInteractor(logoutPresenter);
        mainShellView.setLogoutController(new LogoutController(logoutInteractor));
        return this;
    }

    /** Wires Track B use cases: Browse Events, Create Event, Join Event, View Calendar. */
    public AppBuilder addCommunityAndCalendarUseCases() {
        BrowseEventsOutputBoundary browsePresenter =
                new BrowseEventsPresenter(communityEventsViewModel, zoneId);
        BrowseEventsInputBoundary browseInteractor =
                new BrowseEventsInteractor(
                        dataAccessBundle.browseEventsDataAccess(), browsePresenter, clock);
        communityEventsView.setBrowseEventsController(new BrowseEventsController(browseInteractor));

        CreateEventOutputBoundary createPresenter =
                new CreateEventPresenter(communityEventsViewModel);
        CreateEventInputBoundary createInteractor =
                new CreateEventInteractor(
                        dataAccessBundle.createEventDataAccess(), createPresenter, clock, zoneId);
        communityEventsView.setCreateEventController(new CreateEventController(createInteractor));

        JoinEventOutputBoundary joinPresenter = new JoinEventPresenter(communityEventsViewModel);
        JoinEventInputBoundary joinInteractor =
                new JoinEventInteractor(
                        dataAccessBundle.joinEventDataAccess(), joinPresenter, clock);
        communityEventsView.setJoinEventController(new JoinEventController(joinInteractor));

        ViewCalendarOutputBoundary calendarPresenter =
                new ViewCalendarPresenter(calendarViewModel, sessionViewModel);
        ViewCalendarInputBoundary calendarInteractor =
                new ViewCalendarInteractor(
                        dataAccessBundle.viewCalendarDataAccess(),
                        dataAccessBundle.publicHolidayGateway(),
                        dataAccessBundle.calendarExportGateway(),
                        calendarPresenter,
                        zoneId);
        calendarView.setViewCalendarController(new ViewCalendarController(calendarInteractor));
        return this;
    }

    /** Wires Track C use cases: Edit Schedule, Match Course Buddies, View Rateable Buddies, Rate Buddy. */
    public AppBuilder addCourseMatchAndRatingUseCases() {
        EditScheduleOutputBoundary schedulePresenter =
                new EditSchedulePresenter(courseMatchViewModel);
        EditScheduleInputBoundary scheduleInteractor =
                new EditScheduleInteractor(
                        dataAccessBundle.editScheduleDataAccess(),
                        dataAccessBundle.courseCatalogGateway(),
                        schedulePresenter);
        courseMatchView.setEditScheduleController(new EditScheduleController(scheduleInteractor));

        MatchCourseBuddiesOutputBoundary matchPresenter =
                new MatchCourseBuddiesPresenter(courseMatchViewModel);
        MatchCourseBuddiesInputBoundary matchInteractor =
                new MatchCourseBuddiesInteractor(
                        dataAccessBundle.matchCourseBuddiesDataAccess(),
                        courseMatchStrategy,
                        matchPresenter);
        courseMatchView.setMatchCourseBuddiesController(
                new MatchCourseBuddiesController(matchInteractor));

        ViewRateableBuddiesOutputBoundary rateablePresenter =
                new ViewRateableBuddiesPresenter(buddyRatingViewModel, zoneId);
        ViewRateableBuddiesInputBoundary rateableInteractor =
                new ViewRateableBuddiesInteractor(
                        dataAccessBundle.viewRateableBuddiesDataAccess(), rateablePresenter);
        buddyRatingView.setViewRateableBuddiesController(
                new ViewRateableBuddiesController(rateableInteractor));

        RateBuddyOutputBoundary ratePresenter = new RateBuddyPresenter(buddyRatingViewModel);
        RateBuddyInputBoundary rateInteractor =
                new RateBuddyInteractor(
                        dataAccessBundle.rateBuddyDataAccess(), ratePresenter, clock);
        buddyRatingView.setRateBuddyController(new RateBuddyController(rateInteractor));
        return this;
    }

    /** Wires Track D use cases: Edit Profile, 3+4 Recommend Buddies. */
    public AppBuilder addProfileAndRecommendUseCases() {
        EditProfileOutputBoundary editProfilePresenter =
                new EditProfilePresenter(recommendationViewModel, sessionViewModel);
        EditProfileInputBoundary editProfileInteractor =
                new EditProfileInteractor(
                        dataAccessBundle.editProfileDataAccess(), editProfilePresenter);
        profileAndRecommendView.setEditProfileController(
                new EditProfileController(editProfileInteractor));

        RecommendBuddiesOutputBoundary recommendPresenter =
                new RecommendBuddiesPresenter(recommendationViewModel, zoneId);
        RecommendBuddiesInputBoundary recommendInteractor =
                new RecommendBuddiesInteractor(
                        dataAccessBundle.recommendBuddiesDataAccess(),
                        buddyScoringStrategy,
                        dataAccessBundle.primaryLlmGateway(),
                        dataAccessBundle.fallbackLlmGateway(),
                        recommendPresenter,
                        clock);
        profileAndRecommendView.setRecommendBuddiesController(
                new RecommendBuddiesController(recommendInteractor));
        return this;
    }

    /**
     * Optional developer helper allowing Tracks B/C/D to launch directly into {@link MainShellView}
     * signed in as a seeded demo user (e.g., {@code --dev-user=henry}) while Track A is still
     * implementing Login/Signup.
     *
     * @param username seeded demo username (ignored if null/blank or not found)
     * @return this builder
     */
    public AppBuilder preAuthenticateDevUser(String username) {
        if (username == null || username.isBlank()) {
            return this;
        }
        String normalized = User.normalizeUsername(username);
        Optional<User> found = dataAccessBundle.loginDataAccess().findByUsername(normalized);
        if (found.isPresent()) {
            User user = found.get();
            sessionViewModel.setState(
                    SessionState.loggedIn(
                            user.getUserId(), user.getUsername(), user.getDisplayName()));
            sessionViewModel.firePropertyChanged();
            viewManagerModel.setState(ViewManagerModel.MAIN_SHELL_VIEW);
        }
        return this;
    }

    /**
     * Finalizes and returns the configured root {@link JFrame}.
     *
     * @return ready-to-display Swing application window
     */
    public JFrame build() {
        JFrame application = new JFrame("CampusBuddy — UofT Student Buddy Finder");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        application.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        application.add(rootCardPanel);

        viewManagerModel.firePropertyChanged();
        return application;
    }
}
