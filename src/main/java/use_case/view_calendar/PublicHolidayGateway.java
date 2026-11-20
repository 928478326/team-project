package use_case.view_calendar;

import java.time.LocalDate;
import java.util.List;

/**
 * External gateway abstraction for fetching public holidays (e.g., via Nager.Date REST API).
 * Decouples calendar business rules from HTTP/JSON libraries (DIP).
 */
public interface PublicHolidayGateway {

    /**
     * Fetches public holidays for the given year and ISO country code (e.g., {@code "CA"}).
     * Implementations should degrade gracefully (return an empty list) if the external API
     * is unreachable or times out.
     *
     * @param year calendar year
     * @param countryCode two-letter ISO country code
     * @return list of holidays for that year (never null)
     */
    List<PublicHoliday> fetchPublicHolidays(int year, String countryCode);

    /**
     * Immutable holiday record returned by {@link PublicHolidayGateway}.
     */
    final class PublicHoliday {

        private final LocalDate date;
        private final String localName;
        private final String name;

        public PublicHoliday(LocalDate date, String localName, String name) {
            this.date = date;
            this.localName = localName;
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getLocalName() {
            return localName;
        }

        public String getName() {
            return name;
        }
    }
}
