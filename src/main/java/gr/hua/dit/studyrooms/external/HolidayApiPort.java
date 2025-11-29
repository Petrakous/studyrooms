package gr.hua.dit.studyrooms.external;

import java.time.LocalDate;

public interface HolidayApiPort {

    /**
     * Επιστρέφει true αν η ημερομηνία είναι δημόσια αργία.
     */
    boolean isHoliday(LocalDate date);
}
