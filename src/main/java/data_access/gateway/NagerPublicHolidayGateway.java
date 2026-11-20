package data_access.gateway;

import java.util.Collections;
import java.util.List;
import use_case.view_calendar.PublicHolidayGateway;

/**
 * External REST API adapter fetching Canadian/Ontario public holidays from Nager.Date
 * ({@code GET https://date.nager.at/api/v3/PublicHolidays/{year}/{countryCode}}).
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class NagerPublicHolidayGateway implements PublicHolidayGateway {

    public static final String BASE_URL = "https://date.nager.at/api/v3/PublicHolidays";

    @Override
    public List<PublicHoliday> fetchPublicHolidays(int year, String countryCode) {
        // TODO [Track B]: Call BASE_URL + "/" + year + "/" + countryCode with OkHttp (timeout <= 5s),
        // parse JSON array via org.json.JSONArray, cache per year in memory, and return empty list
        // gracefully on network/HTTP/parse errors.
        return Collections.emptyList();
    }
}
