package app;

import java.time.Clock;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Application entry point assembling CampusBuddy via {@link AppBuilder}.
 *
 * <p>Supported optional CLI flags for independent parallel development:
 * <ul>
 *   <li>{@code --sqlite} — use {@link SqliteDataAccessBundle} (default until Track A merges SQLite
 *       is {@link InMemoryDataAccessBundle}; flip default after SQLite integration)</li>
 *   <li>{@code --dev-user=<username>} — sign in immediately as a seeded demo user
 *       (e.g., {@code --dev-user=henry}, {@code james}, {@code herry}, {@code gracie}) so Tracks
 *       B/C/D can preview their Swing tabs before Login/Signup is implemented</li>
 * </ul>
 */
public final class Main {

    public static final String DEFAULT_SQLITE_PATH = "data/campusbuddy.db";

    private Main() {}

    /**
     * Launches the CampusBuddy desktop UI on the Swing Event Dispatch Thread.
     *
     * @param args optional runtime flags ({@code --sqlite}, {@code --dev-user=...})
     */
    public static void main(String[] args) {
        boolean useSqlite = false;
        String devUser = null;
        for (String arg : args) {
            if ("--sqlite".equals(arg)) {
                useSqlite = true;
            } else if (arg.startsWith("--dev-user=")) {
                devUser = arg.substring("--dev-user=".length());
            }
        }

        final DataAccessBundle bundle =
                useSqlite
                        ? new SqliteDataAccessBundle(DEFAULT_SQLITE_PATH)
                        : new InMemoryDataAccessBundle();
        final String preAuthUser = devUser;

        SwingUtilities.invokeLater(
                () -> {
                    JFrame application =
                            new AppBuilder(bundle, Clock.systemUTC(), AppBuilder.TORONTO_ZONE)
                                    .addAccountAndShellViews()
                                    .addCommunityAndCalendarViews()
                                    .addCourseMatchAndRatingViews()
                                    .addProfileAndRecommendView()
                                    .addAuthUseCases()
                                    .addCommunityAndCalendarUseCases()
                                    .addCourseMatchAndRatingUseCases()
                                    .addProfileAndRecommendUseCases()
                                    .preAuthenticateDevUser(preAuthUser)
                                    .build();
                    application.pack();
                    application.setLocationRelativeTo(null);
                    application.setVisible(true);
                });
    }
}
