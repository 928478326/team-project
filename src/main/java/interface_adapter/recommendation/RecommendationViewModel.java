package interface_adapter.recommendation;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Profile &amp; 3+4 Buddy Recommendation view.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RecommendationViewModel extends ViewModel<RecommendationState> {

    public static final String VIEW_NAME = "profile_and_recommend";

    public RecommendationViewModel() {
        super(VIEW_NAME);
        setState(new RecommendationState());
    }
}
