package com.intellias.intellistart.interviewplanning.model.booking.validation;

import com.intellias.intellistart.interviewplanning.exceptions.BookingException;
import com.intellias.intellistart.interviewplanning.exceptions.BookingException.BookingExceptionProfile;
import com.intellias.intellistart.interviewplanning.exceptions.BookingLimitException;
import com.intellias.intellistart.interviewplanning.exceptions.BookingLimitException.BookingLimitExceptionProfile;
import com.intellias.intellistart.interviewplanning.exceptions.SlotException;
import com.intellias.intellistart.interviewplanning.exceptions.SlotException.SlotExceptionProfile;
import com.intellias.intellistart.interviewplanning.exceptions.UserException;
import com.intellias.intellistart.interviewplanning.model.booking.Booking;
import com.intellias.intellistart.interviewplanning.model.bookinglimit.BookingLimitService;
import com.intellias.intellistart.interviewplanning.model.candidateslot.CandidateSlot;
import com.intellias.intellistart.interviewplanning.model.interviewerslot.InterviewerSlot;
import com.intellias.intellistart.interviewplanning.model.interviewerslot.InterviewerSlotService;
import com.intellias.intellistart.interviewplanning.model.period.Period;
import com.intellias.intellistart.interviewplanning.model.period.PeriodService;
import com.intellias.intellistart.interviewplanning.model.period.services.TimeService;
import com.intellias.intellistart.interviewplanning.model.user.User;
import com.intellias.intellistart.interviewplanning.model.week.WeekService;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Business logic Booking validator.
 */
@Component
public class BookingValidator {

  private static final int BOOKING_PERIOD_DURATION_MINUTES = 90;
  private static final int DESCRIPTION_MAX_SIZE = 4000;
  private static final int SUBJECT_MAX_SIZE = 255;
  private final PeriodService periodService;
  private final TimeService timeService;
  private final WeekService weekService;
  private final BookingLimitService bookingLimitService;
  private final InterviewerSlotService interviewerSlotService;

  /**
   * Constructor.
   */
  @Autowired
  public BookingValidator(PeriodService periodService, TimeService timeService,
      WeekService weekService, BookingLimitService bookingLimitService,
      InterviewerSlotService interviewerSlotService) {
    this.periodService = periodService;
    this.timeService = timeService;
    this.weekService = weekService;
    this.bookingLimitService = bookingLimitService;
    this.interviewerSlotService = interviewerSlotService;
  }

  /**
   * Perform business logic validation for updating Booking.
   *
   * @param updatingBooking Booking with old parameters
   * @param newDataBooking Booking with new parameters
   *
   * @throws UserException          if new booking's interviewer has invalid role
   * @throws SlotException          if duration of new Period is invalid
   * @throws BookingLimitException  if interviewer's booking limit is exceeded
   * @throws BookingException       if periods of InterviewSlot, CandidateSlot do not
   *                                intersect with new Period or new Period is overlapping
   *                                with existing Periods of InterviewerSlot and
   *                                CandidateSlot
   */
  public void validateUpdating(Booking updatingBooking, Booking newDataBooking)
      throws SlotException, BookingException, UserException, BookingLimitException {
    Period newPeriod = newDataBooking.getPeriod();

    int periodDuration = timeService.calculateDurationMinutes(
        newPeriod.getFrom(), newPeriod.getTo());
    if (periodDuration != BOOKING_PERIOD_DURATION_MINUTES) {
      throw new SlotException(SlotExceptionProfile.INVALID_BOUNDARIES);
    }

    if (newDataBooking.getSubject().length() > SUBJECT_MAX_SIZE) {
      throw new BookingException(BookingExceptionProfile.INVALID_SUBJECT);
    }
    if (newDataBooking.getDescription().length() > DESCRIPTION_MAX_SIZE) {
      throw new BookingException(BookingExceptionProfile.INVALID_DESCRIPTION);
    }

    InterviewerSlot newInterviewerSlot = newDataBooking.getInterviewerSlot();
    CandidateSlot newCandidateSlot = newDataBooking.getCandidateSlot();

    LocalDate interviewerSlotDate = weekService.convertToLocalDate(
        newInterviewerSlot.getWeek().getId(), newInterviewerSlot.getDayOfWeek());

    if (!interviewerSlotDate.equals(newCandidateSlot.getDate())) {
      throw new BookingException(BookingExceptionProfile.SLOTS_NOT_INTERSECTING);
    }

    if (!periodService.isFirstInsideSecond(newPeriod, newInterviewerSlot.getPeriod())
        || !periodService.isFirstInsideSecond(newPeriod, newCandidateSlot.getPeriod())) {
      throw new BookingException(BookingExceptionProfile.SLOTS_NOT_INTERSECTING);
    }

    Collection<Booking> interviewSlotBookings = newInterviewerSlot.getBookings();
    Collection<Booking> candidateSlotBookings = newCandidateSlot.getBookings();

    validatePeriodNotOverlappingWithOtherBookingPeriods(
        updatingBooking, newPeriod, interviewSlotBookings);

    validatePeriodNotOverlappingWithOtherBookingPeriods(
        updatingBooking, newPeriod, candidateSlotBookings);

    if (!newInterviewerSlot.equals(updatingBooking.getInterviewerSlot())) {
      User newInterviewer = newInterviewerSlot.getUser();
      List<InterviewerSlot> interviewerSlotsNewInterviewer = interviewerSlotService
          .getInterviewerSlotsByUserAndWeekAndDayOfWeek(
              newInterviewer, newInterviewerSlot.getWeek(), newInterviewerSlot.getDayOfWeek());

      long bookingsNumber = interviewerSlotsNewInterviewer.stream()
          .map(InterviewerSlot::getBookings)
          .flatMap(Collection::stream)
          .count();

      long bookingLimit = bookingLimitService
          .getBookingLimitForCurrentWeek(newInterviewer).getBookingLimit();

      if (bookingsNumber >= bookingLimit) {
        throw new BookingLimitException(BookingLimitExceptionProfile.BOOKING_LIMIT_IS_EXCEEDED);
      }
    }
  }

  private void validatePeriodNotOverlappingWithOtherBookingPeriods(
      Booking updatingBooking, Period period, Collection<Booking> bookings)
      throws BookingException {

    for (Booking booking : bookings) {
      if (periodService.areOverlapping(booking.getPeriod(), period)
          && !booking.equals(updatingBooking)) {
        throw new BookingException(BookingExceptionProfile.SLOTS_NOT_INTERSECTING);
      }
    }
  }

  /**
   * Alias for {@link #validateUpdating(Booking, Booking)}.
   */
  public void validateCreating(Booking newBooking)
      throws SlotException, BookingException, BookingLimitException, UserException {
    validateUpdating(newBooking, newBooking);
  }
}
