package gr.hua.dit.studyrooms.external;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayApiAdapter implements HolidayApiPort {

    private final WebClient holidayWebClient;

    // π.χ. GR για Ελλάδα
    private final String countryCode;

    public HolidayApiAdapter(
            @Qualifier("holidayWebClient") WebClient holidayWebClient,
            @Value("${studyrooms.holiday.country-code:GR}") String countryCode) {
        this.holidayWebClient = holidayWebClient;
        this.countryCode = countryCode;
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        int year = date.getYear();

        try {
            List<HolidayDto> holidays = holidayWebClient.get()
                    .uri("/PublicHolidays/{year}/{country}", year, countryCode)
                    .retrieve()
                    .bodyToFlux(HolidayDto.class)
                    .collectList()
                    .block(); // blocking, αλλά ok για την εργασία

            if (holidays == null) {
                return false;
            }

            String targetDate = date.toString(); // yyyy-MM-dd

            return holidays.stream()
                    .anyMatch(h -> targetDate.equals(h.getDate()));
        } catch (Exception e) {
            // Σε production θα το log-άρουμε. Στην εργασία:
            // Αν αποτύχει το API, απλά δεν το θεωρούμε αργία.
            return false;
        }
    }

    // Εσωτερική DTO κλάση για parse του JSON από το API
    private static class HolidayDto {
        private String date;
        private String localName;
        private String name;

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
