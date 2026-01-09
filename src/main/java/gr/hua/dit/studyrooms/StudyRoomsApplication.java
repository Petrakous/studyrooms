package gr.hua.dit.studyrooms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the StudyRooms Spring Boot application.
 * 
 * The @SpringBootApplication annotation is a convenience annotation that combines:
 * - @Configuration: Marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration mechanism
 * - @ComponentScan: Enables component scanning in this package and sub-packages
 */
@SpringBootApplication
public class StudyRoomsApplication {

	/**
	 * Application entry point. Bootstraps the Spring application context,
	 * starts the embedded web server, and initializes all Spring components.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		// Launch the Spring Boot application with the specified configuration class
		SpringApplication.run(StudyRoomsApplication.class, args);
	}

}


