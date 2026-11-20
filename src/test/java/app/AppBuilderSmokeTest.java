package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.GraphicsEnvironment;
import java.time.Clock;
import javax.swing.JFrame;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Smoke test verifying that {@link AppBuilder} assembles all 13 Clean Architecture use cases,
 * 6 Swing views, and the dev pre-authentication path without wiring errors.
 */
class AppBuilderSmokeTest {

    @Test
    void testAppBuilderAssemblesCompleteCleanArchitectureEngine() {
        Assumptions.assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Skipping JFrame assembly check in headless environment.");

        DataAccessBundle bundle = new InMemoryDataAccessBundle();
        JFrame frame =
                new AppBuilder(bundle, Clock.systemUTC(), AppBuilder.TORONTO_ZONE)
                        .addAccountAndShellViews()
                        .addCommunityAndCalendarViews()
                        .addCourseMatchAndRatingViews()
                        .addProfileAndRecommendView()
                        .addAuthUseCases()
                        .addCommunityAndCalendarUseCases()
                        .addCourseMatchAndRatingUseCases()
                        .addProfileAndRecommendUseCases()
                        .preAuthenticateDevUser("henry")
                        .build();

        assertNotNull(frame);
        assertEquals("CampusBuddy — UofT Student Buddy Finder", frame.getTitle());
        frame.dispose();
    }
}
