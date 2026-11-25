package use_case.create_event;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import entity.Event;

/**
 * Interactor implementing the Create Event use case.
 *
 * <p>Validates the raw form fields, converts the Toronto-local start time to a UTC instant,
 * and saves the event. By team rule the creator is NOT automatically added as a participant.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CreateEventInteractor implements CreateEventInputBoundary {

    /** Expected local date-time pattern in the configured Toronto zone. */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    /**
     * Same layout as {@link #DATE_TIME_PATTERN}, but with {@code uuuu} so that STRICT resolving
     * rejects impossible dates such as {@code 2026-02-30} instead of silently clamping them.
     */
    private static final DateTimeFormatter START_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final CreateEventDataAccessInterface dataAccess;
    private final CreateEventOutputBoundary outputBoundary;
    private final Clock clock;
    private final ZoneId zoneId;

    public CreateEventInteractor(
            CreateEventDataAccessInterface dataAccess,
            CreateEventOutputBoundary outputBoundary,
            Clock clock,
            ZoneId zoneId) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    @Override
    public void execute(CreateEventInputData inputData) {
        String error;
        try {
            error = validate(inputData);
        }
        catch (IllegalStateException ex) {
            error = "Could not check the community. Please try again.";
        }
        if (error.isEmpty()) {
            publish(inputData);
        }
        else {
            outputBoundary.prepareFailView(error);
        }
    }

    /**
     * Checks every field in form order and returns the first problem found.
     *
     * @param inputData raw form fields
     * @return user-facing error message, or an empty string when the input is valid
     */
    private String validate(CreateEventInputData inputData) {
        final String communityId = normalizeCommunityId(inputData.getCommunityId());
        final String error;
        if (inputData.getCreatorId().isEmpty()) {
            error = "Sign in before publishing an event.";
        }
        else if (communityId.isEmpty() || !dataAccess.communityExists(communityId)) {
            error = "Choose a valid community.";
        }
        else if (inputData.getTitle().isBlank()) {
            error = "Enter an event title.";
        }
        else if (parseCapacity(inputData.getCapacityText()).isEmpty()) {
            error = "Capacity must be a whole number from " + Event.MIN_CAPACITY
                    + " to " + Event.MAX_CAPACITY + ".";
        }
        else {
            error = validateStartTime(inputData.getStartsAtLocalText());
        }
        return error;
    }

    private String validateStartTime(String startsAtLocalText) {
        final Optional<LocalDateTime> local = parseLocalStart(startsAtLocalText);
        final String error;
        if (local.isEmpty()) {
            error = "Enter the start time as " + DATE_TIME_PATTERN + " (for example 2026-12-03 14:00).";
        }
        else if (isSkippedByClockChange(local.get())) {
            error = "That time does not exist in Toronto because the clocks move forward for "
                    + "daylight saving. Choose a different start time.";
        }
        else if (!toInstant(local.get()).isAfter(clock.instant())) {
            error = "The start time must be in the future.";
        }
        else {
            error = "";
        }
        return error;
    }

    private void publish(CreateEventInputData inputData) {
        final Event event = new Event(
                UUID.randomUUID().toString(),
                normalizeCommunityId(inputData.getCommunityId()),
                inputData.getCreatorId(),
                inputData.getTitle(),
                inputData.getDescription(),
                parseCapacity(inputData.getCapacityText()).orElseThrow(),
                toInstant(parseLocalStart(inputData.getStartsAtLocalText()).orElseThrow()));
        boolean saved;
        try {
            dataAccess.saveEvent(event);
            saved = true;
        }
        catch (IllegalStateException ex) {
            saved = false;
        }
        if (saved) {
            outputBoundary.prepareSuccessView(new CreateEventOutputData(
                    event.getEventId(),
                    event.getCommunityId(),
                    event.getTitle(),
                    event.getCapacity(),
                    event.getStartsAt()));
        }
        else {
            outputBoundary.prepareFailView("Could not save the event. Please try again.");
        }
    }

    private static String normalizeCommunityId(String communityId) {
        return communityId.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses the capacity text and checks it against the {@link Event} limits.
     *
     * @param capacityText raw capacity text
     * @return the capacity, or empty if it is not a whole number in range
     */
    private static Optional<Integer> parseCapacity(String capacityText) {
        Optional<Integer> result;
        try {
            final int capacity = Integer.parseInt(capacityText.trim());
            if (capacity >= Event.MIN_CAPACITY && capacity <= Event.MAX_CAPACITY) {
                result = Optional.of(capacity);
            }
            else {
                result = Optional.empty();
            }
        }
        catch (NumberFormatException ex) {
            result = Optional.empty();
        }
        return result;
    }

    /**
     * Parses a Toronto-local {@code yyyy-MM-dd HH:mm} string.
     *
     * @param startsAtLocalText raw start time text
     * @return the local start date-time, or empty if the text is not a real calendar date-time
     */
    private static Optional<LocalDateTime> parseLocalStart(String startsAtLocalText) {
        Optional<LocalDateTime> result;
        try {
            result = Optional.of(LocalDateTime.parse(startsAtLocalText.trim(), START_TIME_FORMATTER));
        }
        catch (DateTimeParseException ex) {
            result = Optional.empty();
        }
        return result;
    }

    /**
     * Checks whether a local time falls in the hour skipped when daylight saving starts
     * (for example 2027-03-14 02:30 in Toronto). Such a time has no valid UTC offset, and
     * {@code atZone} would silently move it an hour later.
     *
     * @param local local start date-time
     * @return {@code true} if the time never happens on the wall clock in the configured zone
     */
    private boolean isSkippedByClockChange(LocalDateTime local) {
        return zoneId.getRules().getValidOffsets(local).isEmpty();
    }

    /**
     * Converts a local start time to an instant. When the clocks fall back and a time happens
     * twice, the earlier occurrence (daylight time) is used.
     *
     * @param local local start date-time that exists in the configured zone
     * @return the start instant
     */
    private Instant toInstant(LocalDateTime local) {
        return local.atZone(zoneId).toInstant();
    }
}
