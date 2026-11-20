package interface_adapter.recommendation;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import use_case.recommend_buddies.RecommendBuddiesOutputBoundary;
import use_case.recommend_buddies.RecommendBuddiesOutputData;

/**
 * Presenter for 3+4 buddy recommendations.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RecommendBuddiesPresenter implements RecommendBuddiesOutputBoundary {

    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RecommendationViewModel viewModel;
    private final ZoneId displayZone;

    public RecommendBuddiesPresenter(RecommendationViewModel viewModel, ZoneId displayZone) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.displayZone = Objects.requireNonNull(displayZone, "displayZone must not be null");
    }

    @Override
    public void prepareSuccessView(RecommendBuddiesOutputData outputData) {
        List<RecommendationState.RecommendationCard> cards = new ArrayList<>();
        for (RecommendBuddiesOutputData.BuddyRecommendationItem item :
                outputData.getRecommendations()) {
            String totalLabel = String.format(Locale.ROOT, "%.0f/100", item.getTotalScore());
            String breakdown = String.format(
                    Locale.ROOT,
                    "Style %.0f/35 | Interests & History %.0f/35 | Rating %.0f/30",
                    item.getStyleScore(),
                    item.getInterestAndHistoryScore(),
                    item.getRatingScore());
            cards.add(
                    new RecommendationState.RecommendationCard(
                            item.getUserId(),
                            item.getDisplayName(),
                            item.getContactInfo(),
                            item.getWeekendStyleLabel(),
                            totalLabel,
                            breakdown,
                            item.getSharedCommunities(),
                            item.getSharedEventCount(),
                            item.getAverageRatingDisplay(),
                            item.isPenalized(),
                            item.getExplanation(),
                            item.getIcebreaker()));
        }

        List<RecommendationState.SuggestedEventRow> eventRows = new ArrayList<>();
        for (RecommendBuddiesOutputData.SuggestedEventItem eventItem :
                outputData.getSuggestedEvents()) {
            String startsAtText =
                    eventItem.getStartsAt().atZone(displayZone).format(EVENT_FORMATTER);
            eventRows.add(
                    new RecommendationState.SuggestedEventRow(
                            eventItem.getEventId(),
                            eventItem.getCommunityId(),
                            eventItem.getTitle(),
                            startsAtText));
        }

        RecommendationState state = viewModel.getState();
        state.setRecommendations(cards);
        state.setSuggestedEvents(eventRows);
        state.setAiGenerated(outputData.isAiGenerated());
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        RecommendationState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
