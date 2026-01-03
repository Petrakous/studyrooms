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

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private StudySpaceRepository studySpaceRepository;
    @Mock
    private HolidayApiPort holidayApiPort;
    @Mock
    private gr.hua.dit.studyrooms.service.NotificationService notificationService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private StudySpace studySpace;
    private User user;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        studySpace = new StudySpace();
        studySpace.setId(1L);
        studySpace.setCapacity(1);
        studySpace.setOpenTime(LocalTime.of(8, 0));
        studySpace.setCloseTime(LocalTime.of(20, 0));

        user = new User();
        user.setId(1L);

        date = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(10, 0);
        endTime = LocalTime.of(11, 0);

        lenient().when(studySpaceRepository.findById(studySpace.getId())).thenReturn(Optional.of(studySpace));
        lenient().when(holidayApiPort.isHoliday(any(LocalDate.class))).thenReturn(false);
        lenient().when(reservationRepository.existsByStudySpaceAndDateAndStatus(any(), any(), any())).thenReturn(false);
        lenient().when(reservationRepository.countByUserAndDateAndStatusIn(any(), any(), any())).thenReturn(0L);
        lenient().when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void capacityOneBlocksSecondOverlappingReservation() {
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () ->
                reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime));
    }

    @Test
    void capacityTwoAllowsTwoButBlocksThirdOverlap() {
        studySpace.setCapacity(2);
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(1L)
                .thenReturn(2L);

        reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime);

        assertThrows(IllegalStateException.class, () ->
                reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime));
    }

    @Test
    void cancelledReservationsDoNotConsumeCapacity() {
        when(reservationRepository.countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), any()))
                .thenReturn(0L);

        reservationService.createReservation(user, studySpace.getId(), date, startTime, endTime);

        ArgumentCaptor<Collection<ReservationStatus>> statusesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(reservationRepository).countOverlappingReservations(eq(studySpace), eq(date), eq(startTime), eq(endTime), statusesCaptor.capture());

        Collection<ReservationStatus> capturedStatuses = statusesCaptor.getValue();
        capturedStatuses.forEach(status -> {
            String name = status.toString();
            if ("CANCELLED".equals(name) || "NO_SHOW".equals(name)) {
                throw new AssertionError("Cancelled or no-show statuses should not consume capacity");
            }
        });
    }
}
