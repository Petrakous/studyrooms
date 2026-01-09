package gr.hua.dit.studyrooms.service;

import gr.hua.dit.studyrooms.entity.Reservation;
import gr.hua.dit.studyrooms.entity.ReservationStatus;
import gr.hua.dit.studyrooms.entity.StudySpace;
import gr.hua.dit.studyrooms.entity.User;
import gr.hua.dit.studyrooms.external.HolidayApiPort;
import gr.hua.dit.studyrooms.repository.ReservationRepository;
import gr.hua.dit.studyrooms.repository.StudySpaceRepository;
import gr.hua.dit.studyrooms.repository.UserRepository;
import gr.hua.dit.studyrooms.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReservationServiceImpl}.
 * 
 * These tests verify the reservation capacity logic, ensuring that:
 * - Study spaces respect their maximum capacity for overlapping reservations
 * - Cancelled and no-show reservations do not count against capacity
 * 
 * Uses Mockito to mock all dependencies and isolate the service logic.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    // ==================== Mocked Dependencies ====================
    
    /** Repository for managing reservation persistence */
    @Mock
    private ReservationRepository reservationRepository;
    
    /** Repository for managing study space persistence */
    @Mock
    private StudySpaceRepository studySpaceRepository;
    
    /** External API port for checking if a date is a holiday */
    @Mock
    private HolidayApiPort holidayApiPort;
    
    /** Service for sending notifications to users */
    @Mock
    private gr.hua.dit.studyrooms.service.NotificationService notificationService;
    
    /** Repository for managing user persistence */
    @Mock
    private UserRepository userRepository;

    /** The service under test - Mockito injects the mocked dependencies */
    @InjectMocks
    private ReservationServiceImpl reservationService;

    // ==================== Test Fixtures ====================
    
    /** A sample study space used across tests (default capacity: 1) */
    private StudySpace studySpace;
    
    /** A sample user making reservations */
    private User user;
    
    /** The reservation date (tomorrow by default) */
    private LocalDate date;
    
    /** Reservation start time (10:00 AM) */
    private LocalTime startTime;
    
    /** Reservation end time (11:00 AM) */
    private LocalTime endTime;

    /**
     * Sets up the test fixtures before each test.
     * 
     * Creates a study space with:
     * - Capacity of 1 (can be modified in individual tests)
     * - Operating hours: 8:00 AM to 8:00 PM
     * 
     * Configures lenient mock behaviors for common operations:
     * - Study space lookup returns the test space
     * - No holidays by default
     * - No existing reservations by default
     * - User has no reservations for the day
     * - Save operations return the saved entity
     */
    @BeforeEach
    void setUp() {
        // Create a study space with capacity 1 and operating hours 8 AM - 8 PM
        studySpace = new StudySpace();
        studySpace.setId(1L);
        studySpace.setCapacity(1);
        studySpace.setOpenTime(LocalTime.of(8, 0));
        studySpace.setCloseTime(LocalTime.of(20, 0));

        // Create a test user
        user = new User();
        user.setId(1L);

        // Set up reservation time slot: tomorrow from 10 AM to 11 AM
        date = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(10, 0);
        endTime = LocalTime.of(11, 0);

        // Configure default mock behaviors (lenient = won't fail if not used)
        lenient().when(studySpaceRepository.findById(studySpace.getId())).thenReturn(Optional.of(studySpace));
        lenient().when(holidayApiPort.isHoliday(any(LocalDate.class))).thenReturn(false);
        lenient().when(reservationRepository.existsByStudySpaceAndDateAndStatus(any(), any(), any())).thenReturn(false);
        lenient().when(reservationRepository.countByUserAndDateAndStatusIn(any(), any(), any())).thenReturn(0L);
        lenient().when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== Test Cases ====================

    /**
     * Tests that a study space with capacity 1 blocks a second overlapping reservation.
     * 
     * Scenario:
     * - Study space has capacity of 1
     * - There is already 1 overlapping reservation in the same time slot
     * - A second reservation attempt should throw IllegalStateException
     */
    @Test
    void capacityOneBlocksSecondOverlappingReservation() {
        // Simulate that 1 reservation already exists for this time slot
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(1L);

        // Attempting to create another reservation should fail (capacity exceeded)
        assertThrows(IllegalStateException.class, () ->
                reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime));
    }

    /**
     * Tests that a study space with capacity 2 allows two reservations but blocks a third.
     * 
     * Scenario:
     * - Study space has capacity of 2
     * - First call: 1 existing reservation → allows creation (1 + 1 = 2 ≤ capacity)
     * - Second call: 2 existing reservations → blocks creation (2 + 1 = 3 > capacity)
     */
    @Test
    void capacityTwoAllowsTwoButBlocksThirdOverlap() {
        // Increase capacity to 2 for this test
        studySpace.setCapacity(2);
        
        // First call returns 1 (room for one more), second call returns 2 (at capacity)
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(1L)   // First invocation: 1 existing reservation
                .thenReturn(2L);  // Second invocation: 2 existing reservations

        // First reservation should succeed (1 existing + 1 new = 2 = capacity)
        reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime);

        // Second reservation should fail (2 existing + 1 new = 3 > capacity)
        assertThrows(IllegalStateException.class, () ->
                reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime));
    }

    /**
     * Tests that cancelled and no-show reservations do not consume capacity.
     * 
     * This test verifies that when counting overlapping reservations,
     * only "active" statuses are considered. Reservations with status
     * CANCELLED or NO_SHOW should be excluded from the capacity count.
     * 
     * Uses ArgumentCaptor to inspect what statuses are passed to the
     * countOverlappingReservations method and ensures CANCELLED/NO_SHOW
     * are not included.
     */
    @Test
    void cancelledReservationsDoNotConsumeCapacity() {
        // No overlapping reservations exist
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(0L);

        // Create a reservation (should succeed)
        reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime);

        // Capture the collection of statuses passed to countOverlappingReservations
        ArgumentCaptor<Collection<ReservationStatus>> statusesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(reservationRepository).countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), statusesCaptor.capture());

        // Verify that CANCELLED and NO_SHOW statuses are NOT in the list
        // (they should not be counted when checking capacity)
        Collection<ReservationStatus> capturedStatuses = statusesCaptor.getValue();
        capturedStatuses.forEach(status -> {
            String name = status.toString();
            if ("CANCELLED".equals(name) || "NO_SHOW".equals(name)) {
                throw new AssertionError("Cancelled or no-show statuses should not consume capacity");
            }
        });
    }
}
