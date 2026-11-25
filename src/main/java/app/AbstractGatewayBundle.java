package app;

import data_access.gateway.HttpLlmProfileEnrichmentGateway;
import data_access.gateway.HttpLlmRecommendationGateway;
import data_access.gateway.IcsCalendarExporter;
import data_access.gateway.LocalUoftCourseCatalogGateway;
import data_access.gateway.NagerPublicHolidayGateway;
import data_access.gateway.RuleBasedExplanationFallback;
import use_case.edit_profile.LlmProfileEnrichmentGateway;
import use_case.edit_schedule.CourseCatalogGateway;
import use_case.recommend_buddies.LlmRecommendationGateway;
import use_case.view_calendar.CalendarExportGateway;
import use_case.view_calendar.PublicHolidayGateway;

/** Supplies the external services shared by the SQLite and in-memory configurations. */
abstract class AbstractGatewayBundle implements DataAccessBundle {
    private final LlmProfileEnrichmentGateway profileGateway = new HttpLlmProfileEnrichmentGateway();
    private final PublicHolidayGateway holidayGateway = new NagerPublicHolidayGateway();
    private final CalendarExportGateway exportGateway = new IcsCalendarExporter();
    private final CourseCatalogGateway courseCatalogGateway = new LocalUoftCourseCatalogGateway();
    private final LlmRecommendationGateway primaryLlmGateway = new HttpLlmRecommendationGateway();
    private final LlmRecommendationGateway fallbackLlmGateway = new RuleBasedExplanationFallback();

    @Override
    public LlmProfileEnrichmentGateway profileEnrichmentGateway() {
        return profileGateway;
    }

    @Override
    public PublicHolidayGateway publicHolidayGateway() {
        return holidayGateway;
    }

    @Override
    public CalendarExportGateway calendarExportGateway() {
        return exportGateway;
    }

    @Override
    public CourseCatalogGateway courseCatalogGateway() {
        return courseCatalogGateway;
    }

    @Override
    public LlmRecommendationGateway primaryLlmGateway() {
        return primaryLlmGateway;
    }

    @Override
    public LlmRecommendationGateway fallbackLlmGateway() {
        return fallbackLlmGateway;
    }
}
