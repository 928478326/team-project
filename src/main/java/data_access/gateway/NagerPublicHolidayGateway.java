package data_access.gateway;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import use_case.view_calendar.PublicHolidayGateway;

/**
 * External REST API adapter fetching Canadian/Ontario public holidays from Nager.Date
 * ({@code GET https://date.nager.at/api/v3/PublicHolidays/{year}/{countryCode}}).
 *
 * <p>Nager.Date returns every provincial holiday in Canada (for example four different holidays
 * on the third Monday of February), so this gateway keeps only holidays that are either
 * nationwide or observed in {@link #DEFAULT_PROVINCE} (Ontario).
 *
 * <p>Successful results are cached per year and country for the life of the app. Failures are
 * never cached, so the next calendar refresh tries the API again. Any network, HTTP, or JSON
 * problem results in an empty list, never an exception.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class NagerPublicHolidayGateway implements PublicHolidayGateway {

    public static final String BASE_URL = "https://date.nager.at/api/v3/PublicHolidays";

    /** ISO 3166-2 code of the province whose regional holidays are kept (UofT is in Ontario). */
    public static final String DEFAULT_PROVINCE = "CA-ON";

    /** Upper bound on the whole request, so a slow API never stalls the calendar for long. */
    private static final int TIMEOUT_SECONDS = 5;

    private final OkHttpClient client;
    private final String baseUrl;
    private final String provinceCode;
    private final Map<String, List<PublicHoliday>> cache = new ConcurrentHashMap<>();

    /** Creates a gateway for the real Nager.Date API, keeping Ontario holidays. */
    public NagerPublicHolidayGateway() {
        this(BASE_URL, DEFAULT_PROVINCE);
    }

    /**
     * Creates a gateway against a custom base URL (used by tests to point at a local server).
     *
     * @param baseUrl URL that {@code /{year}/{countryCode}} is appended to
     * @param provinceCode ISO 3166-2 province code such as {@code CA-ON}
     */
    NagerPublicHolidayGateway(String baseUrl, String provinceCode) {
        this.baseUrl = baseUrl;
        this.provinceCode = provinceCode;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<PublicHoliday> fetchPublicHolidays(int year, String countryCode) {
        final String country = Objects.requireNonNullElse(countryCode, "").trim().toUpperCase(Locale.ROOT);
        final String cacheKey = year + "/" + country;
        List<PublicHoliday> holidays = cache.get(cacheKey);
        if (holidays == null) {
            final String body = download(baseUrl + "/" + cacheKey);
            if (body.isEmpty()) {
                holidays = Collections.emptyList();
            }
            else {
                holidays = parse(body, country);
                if (!holidays.isEmpty()) {
                    cache.put(cacheKey, holidays);
                }
            }
        }
        return holidays;
    }

    /**
     * Performs the HTTP GET.
     *
     * @param url full request URL
     * @return the response body, or an empty string on any network or HTTP failure
     */
    private String download(String url) {
        final Request request = new Request.Builder().url(url).header("Accept", "application/json").build();
        String body = "";
        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            if (response.isSuccessful() && responseBody != null) {
                body = responseBody.string();
            }
        }
        catch (IOException ex) {
            body = "";
        }
        return body;
    }

    /**
     * Converts the Nager.Date JSON array into holidays observed in the configured province.
     * Malformed entries are skipped; a malformed document yields an empty list.
     *
     * @param json response body
     * @param country upper-case country code that was requested
     * @return unmodifiable list of holidays in API order
     */
    private List<PublicHoliday> parse(String json, String country) {
        final List<PublicHoliday> holidays = new ArrayList<>();
        try {
            final JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject entry = array.optJSONObject(i);
                if (entry != null && isObservedHere(entry, country)) {
                    addIfValid(entry, holidays);
                }
            }
        }
        catch (JSONException ex) {
            holidays.clear();
        }
        return Collections.unmodifiableList(holidays);
    }

    /**
     * A holiday counts if it is nationwide, or if its {@code counties} list includes our province.
     * The province filter only applies when the province belongs to the requested country.
     *
     * @param entry one holiday from the API response
     * @param country requested country code, such as {@code CA}
     * @return {@code true} if the holiday is observed in the configured province
     */
    private boolean isObservedHere(JSONObject entry, String country) {
        final boolean filterByProvince = provinceCode != null && provinceCode.startsWith(country + "-");
        final JSONArray counties = entry.optJSONArray("counties");
        boolean observed = !filterByProvince || entry.optBoolean("global", true) || counties == null;
        if (!observed) {
            for (int i = 0; i < counties.length(); i++) {
                if (provinceCode.equalsIgnoreCase(counties.optString(i))) {
                    observed = true;
                }
            }
        }
        return observed;
    }

    private static void addIfValid(JSONObject entry, List<PublicHoliday> holidays) {
        try {
            final LocalDate date = LocalDate.parse(entry.optString("date"));
            holidays.add(new PublicHoliday(date, entry.optString("localName"), entry.optString("name")));
        }
        catch (DateTimeParseException ex) {
            // Skip an entry with a missing or malformed date; keep the rest of the year.
        }
    }
}
