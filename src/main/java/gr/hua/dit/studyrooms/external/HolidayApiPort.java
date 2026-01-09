
// Interface for checking if a date is a public holiday
package gr.hua.dit.studyrooms.external;

import java.time.LocalDate;


/**
 * Port interface for holiday-checking functionality.
 * Implementations should provide logic to determine if a date is a public holiday.
 */
public interface HolidayApiPort {

    /**
     * Checks if the given date is a public holiday.
     * @param date the date to check
     * @return true if the date is a public holiday, false otherwise
     */
    boolean isHoliday(LocalDate date);
}
