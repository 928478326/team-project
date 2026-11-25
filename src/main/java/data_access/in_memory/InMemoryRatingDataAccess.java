package data_access.in_memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

/** Reads and writes feedback on shared events in the seeded development store. */
public final class InMemoryRatingDataAccess
        implements ViewRateableBuddiesDataAccessInterface, RateBuddyDataAccessInterface {
    private final InMemoryCampusBuddyStore store;

    public InMemoryRatingDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public List<RateableBuddyItem> findRateableBuddies(String userId, Instant asOf) {
        synchronized (store) {
            final List<RateableBuddyItem> result = new ArrayList<>();
            for (Participation peer : store.participationsByKey().values()) {
                final Event event = store.eventsById().get(peer.getEventId());
                final User buddy = store.usersById().get(peer.getUserId());
                if (buddy != null && !userId.equals(buddy.getUserId()) && joinedBy(userId, event, asOf)) {
                    final BuddyRating prior = store.ratingsByKey().get(
                            InMemoryCampusBuddyStore.ratingKey(userId, buddy.getUserId(), event.getEventId()));
                    result.add(toItem(event, buddy, prior));
                }
            }
            return result;
        }
    }

    @Override
    public boolean saveIfEligible(BuddyRating rating) {
        synchronized (store) {
            final Event event = store.eventsById().get(rating.getEventId());
            final boolean eligible = joinedBy(rating.getRaterId(), event, rating.getRatedAt())
                    && store.participationsByKey().containsKey(
                            InMemoryCampusBuddyStore.participationKey(rating.getRateeId(), rating.getEventId()));
            if (eligible) {
                store.putRating(rating);
            }
            return eligible;
        }
    }

    private boolean joinedBy(String userId, Event event, Instant asOf) {
        return event != null && !event.isOpenAt(asOf) && store.participationsByKey().containsKey(
                InMemoryCampusBuddyStore.participationKey(userId, event.getEventId()));
    }

    private RateableBuddyItem toItem(Event event, User buddy, BuddyRating prior) {
        int score = 0;
        String comment = "";
        boolean wouldMeetAgain = false;
        if (prior != null) {
            score = prior.getScore();
            comment = prior.getComment();
            wouldMeetAgain = prior.isWouldMeetAgain();
        }
        return new RateableBuddyItem(event.getEventId(), event.getTitle(), event.getStartsAt(), buddy.getUserId(),
                buddy.getDisplayName(), prior != null, score, wouldMeetAgain, comment);
    }
}
