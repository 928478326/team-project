package use_case.view_calendar;

/**
 * Immutable input data for viewing or exporting a monthly calendar.
 */
public final class ViewCalendarInputData {

    private final String userId;
    private final int year;
    private final int month;
    private final String exportFilePath;

    /**
     * Constructs a calendar request.
     *
     * @param userId active user ID
     * @param year calendar year (e.g., 2026)
     * @param month calendar month 1..12
     * @param exportFilePath optional target path for {@code .ics} export (null/blank for view only)
     */
    public ViewCalendarInputData(String userId, int year, int month, String exportFilePath) {
        this.userId = userId == null ? "" : userId.trim();
        this.year = year;
        this.month = month;
        this.exportFilePath = exportFilePath == null ? "" : exportFilePath.trim();
    }

    public String getUserId() {
        return userId;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public String getExportFilePath() {
        return exportFilePath;
    }

    /**
     * Checks whether this request also asks to write an {@code .ics} file.
     *
     * @return {@code true} if {@code exportFilePath} is non-blank
     */
    public boolean isExportRequested() {
        return !exportFilePath.isEmpty();
    }
}
