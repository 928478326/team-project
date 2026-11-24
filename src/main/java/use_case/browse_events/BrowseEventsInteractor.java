package use_case.browse_events;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import entity.Community;
import entity.Event;
import entity.User;

/**
 * Interactor for browsing events within a community (or across all communities).
 *
 * <p>Events are listed by start time (earliest first, ties broken by event ID). Each row says
 * how many people have joined, whether the current user is one of them, and whether the event
 * is still open for joining.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class BrowseEventsInteractor implements BrowseEventsInputBoundary {

    /** Shown when an event's creator account can no longer be found. */
    public static final String UNKNOWN_CREATOR = "Unknown organizer";

    private static final Comparator<Event> EVENT_ORDER =
            Comparator.comparing(Event::getStartsAt).thenComparing(Event::getEventId);

    private final BrowseEventsDataAccessInterface dataAccess;
    private final BrowseEventsOutputBoundary outputBoundary;
    private final Clock clock;

    public BrowseEventsInteractor(
            BrowseEventsDataAccessInterface dataAccess,
            BrowseEventsOutputBoundary outputBoundary,
            Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(BrowseEventsInputData inputData) {
        final String communityFilter = normalizeFilter(inputData.getCommunityId());
        try {
            if (isAll(communityFilter) || dataAccess.communityExists(communityFilter)) {
                final List<Community> communities = dataAccess.findAllCommunities();
                outputBoundary.prepareSuccessView(new BrowseEventsOutputData(
                        communityFilter,
                        loadItems(inputData.getCurrentUserId(), communityFilter, namesById(communities)),
                        toOptions(communities)));
            }
            else {
                outputBoundary.prepareFailView("Unknown community: " + inputData.getCommunityId());
            }
        }
        catch (IllegalStateException ex) {
            outputBoundary.prepareFailView("Could not load events. Please try again.");
        }
    }

    private List<BrowseEventsOutputData.EventItem> loadItems(
            String currentUserId, String communityFilter, Map<String, String> communityNames) {
        final List<Event> events = new ArrayList<>(dataAccess.findEventsByCommunity(communityFilter));
        events.sort(EVENT_ORDER);

        final Map<String, String> creatorNames = new HashMap<>();
        final Instant now = clock.instant();
        final List<BrowseEventsOutputData.EventItem> items = new ArrayList<>();
        for (Event event : events) {
            final String creatorName =
                    creatorNames.computeIfAbsent(event.getCreatorId(), this::creatorDisplayName);
            items.add(new BrowseEventsOutputData.EventItem(
                    event.getEventId(),
                    event.getCommunityId(),
                    communityNames.getOrDefault(event.getCommunityId(), event.getCommunityId()),
                    creatorName,
                    event.getTitle(),
                    event.getDescription(),
                    event.getCapacity(),
                    dataAccess.countParticipants(event.getEventId()),
                    event.getStartsAt(),
                    !currentUserId.isEmpty() && dataAccess.hasUserJoined(currentUserId, event.getEventId()),
                    event.isOpenAt(now)));
        }
        return items;
    }

    private static Map<String, String> namesById(List<Community> communities) {
        final Map<String, String> names = new HashMap<>();
        for (Community community : communities) {
            names.put(community.getCommunityId(), community.getName());
        }
        return names;
    }

    private static List<BrowseEventsOutputData.CommunityOption> toOptions(List<Community> communities) {
        final List<BrowseEventsOutputData.CommunityOption> options = new ArrayList<>();
        for (Community community : communities) {
            options.add(new BrowseEventsOutputData.CommunityOption(community.getCommunityId(), community.getName()));
        }
        return options;
    }

    private String creatorDisplayName(String creatorId) {
        return dataAccess.findUserById(creatorId).map(User::getDisplayName).orElse(UNKNOWN_CREATOR);
    }

    /**
     * Turns the requested filter into either {@link BrowseEventsInputData#ALL_COMMUNITIES} or a
     * lowercase community ID, matching how {@link Event} stores community IDs.
     */
    private static String normalizeFilter(String communityId) {
        final String filter;
        if (isAll(communityId)) {
            filter = BrowseEventsInputData.ALL_COMMUNITIES;
        }
        else {
            filter = communityId.trim().toLowerCase(Locale.ROOT);
        }
        return filter;
    }

    private static boolean isAll(String communityId) {
        return BrowseEventsInputData.ALL_COMMUNITIES.equalsIgnoreCase(communityId.trim());
    }
}
