package gr.hua.dit.studyrooms.external;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;


// Service class that adapts the external Holiday API to the application's interface
@Service
public class HolidayApiAdapter implements HolidayApiPort {


    // WebClient used to make HTTP requests to the external holiday API
    private final WebClient holidayWebClient;

    // Country code for which to check holidays (e.g., "GR" for Greece)
    private final String countryCode;


    /**
     * Constructor for HolidayApiAdapter.
     * @param holidayWebClient the WebClient bean for making API requests
     * @param countryCode the country code to use for holiday checks (default: GR)
     */
    public HolidayApiAdapter(
            @Qualifier("holidayWebClient") WebClient holidayWebClient,
            @Value("${studyrooms.holiday.country-code:GR}") String countryCode) {
        this.holidayWebClient = holidayWebClient;
        this.countryCode = countryCode;
    }


    /**
     * Checks if the given date is a public holiday in the configured country.
     * Calls the external holiday API and compares the date to the list of holidays.
     * If the API call fails, returns false (not a holiday).
     * @param date the date to check
     * @return true if the date is a holiday, false otherwise
     */
    @Override
    public boolean isHoliday(LocalDate date) {
        int year = date.getYear();

        try {
            // Call the external API to get the list of holidays for the year and country
            List<HolidayDto> holidays = holidayWebClient.get()
                    .uri("/PublicHolidays/{year}/{country}", year, countryCode)
                    .retrieve()
                    .bodyToFlux(HolidayDto.class)
                    .collectList()
                    .block(); // Blocking call for simplicity in this context

            if (holidays == null) {
                return false;
            }

            String targetDate = date.toString(); // Format: yyyy-MM-dd

            // Check if any holiday matches the target date
            return holidays.stream()
                    .anyMatch(h -> targetDate.equals(h.getDate()));
        } catch (Exception e) {
            // In production, we would log the error. For this assignment:
            // If the API fails, simply do not consider it a holiday.
            return false;
        }
    }

    /**
     * Internal DTO class for parsing the JSON response from the holiday API.
     * Maps the relevant fields from the API response.
     */
    private static class HolidayDto {
        private String date;      // Date of the holiday (yyyy-MM-dd)
        private String localName; // Localized name of the holiday
        private String name;      // English name of the holiday

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getLocalName() {
            return localName;
        }

        public void setLocalName(String localName) {
            this.localName = localName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
