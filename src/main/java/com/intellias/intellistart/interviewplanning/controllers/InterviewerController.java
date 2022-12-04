package com.intellias.intellistart.interviewplanning.controllers;

import com.intellias.intellistart.interviewplanning.controllers.dto.BookingLimitDto;
import com.intellias.intellistart.interviewplanning.controllers.dto.InterviewerSlotDto;
import com.intellias.intellistart.interviewplanning.exceptions.BookingLimitException;
import com.intellias.intellistart.interviewplanning.exceptions.SlotException;
import com.intellias.intellistart.interviewplanning.exceptions.UserException;
import com.intellias.intellistart.interviewplanning.model.bookinglimit.BookingLimit;
import com.intellias.intellistart.interviewplanning.model.bookinglimit.BookingLimitService;
import com.intellias.intellistart.interviewplanning.model.interviewerslot.InterviewerSlot;
import com.intellias.intellistart.interviewplanning.model.interviewerslot.InterviewerSlotDtoValidator;
import com.intellias.intellistart.interviewplanning.model.interviewerslot.InterviewerSlotService;
import com.intellias.intellistart.interviewplanning.model.user.User;
import com.intellias.intellistart.interviewplanning.model.user.UserService;
import com.intellias.intellistart.interviewplanning.model.week.WeekService;
import com.intellias.intellistart.interviewplanning.security.JwtUserDetails;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing requests from users with Interview role.
 */
@RestController
public class InterviewerController {

  private final InterviewerSlotService interviewerSlotService;
  private final InterviewerSlotDtoValidator interviewerSlotDtoValidator;
  private final BookingLimitService bookingLimitService;
  private final WeekService weekService;
  private final UserService userService;

  /**
   * Constructor.
   *
   * @param interviewerSlotService      - interviewerSlotService
   * @param interviewerSlotDtoValidator - interviewerSlotDtoValidator
   * @param bookingLimitService         - bookingLimitService
   * @param weekService                 - weekService
   * @param userService                 - userService
   */
  @Autowired
  public InterviewerController(
      InterviewerSlotService interviewerSlotService,
      InterviewerSlotDtoValidator interviewerSlotDtoValidator,
      BookingLimitService bookingLimitService,
      WeekService weekService,
      UserService userService) {
    this.interviewerSlotService = interviewerSlotService;
    this.interviewerSlotDtoValidator = interviewerSlotDtoValidator;
    this.bookingLimitService = bookingLimitService;
    this.weekService = weekService;
    this.userService = userService;
  }

  /**
   * Post Request for creating slot.
   *
   * @param interviewerSlotDto - DTO from request
   * @param interviewerId      - user Id from request
   * @return interviewerSlotDto - and/or HTTP status
   *
   * @throws SlotException when:
   *     <ul>
   *     <li>cannot edit this week
   *     <li>invalid boundaries of time period
   *     <li>when slot is not found by slotId
   *     <li>slot is overlapping
   *     </ul>
   *
   * @throws UserException invalid user (interviewer) exception
   */
  @PostMapping("/interviewers/{interviewerId}/slots")
  public ResponseEntity<InterviewerSlotDto> createInterviewerSlot(
      @RequestBody InterviewerSlotDto interviewerSlotDto,
      @PathVariable("interviewerId") Long interviewerId,
      Authentication authentication
  ) throws SlotException, UserException {

    interviewerSlotDtoValidator
        .validateAndCreate(interviewerSlotDto, authentication, interviewerId);

    return new ResponseEntity<>(interviewerSlotDto, HttpStatus.OK);
  }

  /**
   * Post Request for updating slot.
   *
   * @param interviewerSlotDto - DTO from request
   * @param interviewerId      - user Id from request
   * @param slotId             - slot Id from request
   *
   * @return interviewerSlotDto - and/or HTTP status
   *
   * @throws UserException when:
   *     <ul>
   *     <li>cannot edit this week
   *     <li>invalid boundaries of time period
   *     <li>when slot is not found by slotId
   *     </ul>
   *
   * @throws SlotException - when slot has at least one booking or slot overlaps
   */
  @PostMapping("/interviewers/{interviewerId}/slots/{slotId}")
  public ResponseEntity<InterviewerSlotDto> updateInterviewerSlot(
      @RequestBody InterviewerSlotDto interviewerSlotDto,
      @PathVariable("interviewerId") Long interviewerId,
      @PathVariable("slotId") Long slotId,
      Authentication authentication
  ) throws SlotException, UserException {

    interviewerSlotDtoValidator.validateAndUpdate(interviewerSlotDto,
        authentication, interviewerId, slotId);

    return new ResponseEntity<>(interviewerSlotDto, HttpStatus.OK);
  }

  /**
   * Post Request for creating booking limit.
   *
   * @param bookingLimitDto - DTO for BookingLimit
   * @param interviewerId   - user id from request
   * @return BookingLimitDto and HTTP status
   * @throws UserException - invalid user (interviewer) exception or not interviewer id
   * @throws BookingLimitException - invalid bookingLimit exception
   */
  @PostMapping("/interviewers/{interviewerId}/booking-limits")
  public ResponseEntity<BookingLimitDto> createBookingLimit(
      @RequestBody BookingLimitDto bookingLimitDto,
      @PathVariable("interviewerId") Long interviewerId)
      throws UserException, BookingLimitException {

    User user = userService.getUserById(interviewerId);

    bookingLimitDto.setUserId(interviewerId);

    BookingLimit bookingLimit = bookingLimitService.createBookingLimit(user,
        bookingLimitDto.getBookingLimit());

    return ResponseEntity.ok(new BookingLimitDto(bookingLimit));
  }

  /**
   * Request for getting booking limit for current week.
   *
   * @param interviewerId - user Id from request
   * @return BookingLimitDto and HTTP status
   * @throws UserException - invalid user (interviewer) exception or ot interviewer id
   */
  @GetMapping("/interviewers/{interviewerId}/booking-limits/current-week")
  public ResponseEntity<BookingLimitDto> getBookingLimitForCurrentWeek(
      @PathVariable("interviewerId") Long interviewerId)
      throws UserException {

    User user = userService.getUserById(interviewerId);

    BookingLimit bookingLimit = bookingLimitService.getBookingLimitForCurrentWeek(user);

    return ResponseEntity.ok(new BookingLimitDto(bookingLimit));
  }

  /**
   * Request for getting booking limit for next week.
   *
   * @param interviewerId user Id from request
   * @return BookingLimitDto and HTTP status
   * @throws UserException - invalid user (interviewer) exception or ot interviewer id
   */
  @GetMapping("/interviewers/{interviewerId}/booking-limits/next-week")
  public ResponseEntity<BookingLimitDto> getBookingLimitForNextWeek(
      @PathVariable("interviewerId") Long interviewerId)
      throws UserException {

    User user = userService.getUserById(interviewerId);

    BookingLimit bookingLimit = bookingLimitService.getBookingLimitForNextWeek(user);

    return ResponseEntity.ok(new BookingLimitDto(bookingLimit));
  }

  /**
   * Request for getting Interviewer Slots of current user for current week.
   *
   * @param authentication - user
   * @return {@link List} of {@link InterviewerSlot}
   */
  @GetMapping("/interviewers/current/slots")
  public ResponseEntity<List<InterviewerSlot>> getInterviewerSlotsForCurrentWeek(
      Authentication authentication) {
    JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();

    String email = jwtUserDetails.getEmail();
    Long currentWeekId = weekService.getCurrentWeek().getId();

    List<InterviewerSlot> slots = interviewerSlotService.getSlotsByWeek(email, currentWeekId);

    return new ResponseEntity<>(slots, HttpStatus.OK);
  }

  /**
   * Request for getting Interviewer Slots of current user for next week.
   *
   * @param authentication - user
   * @return {@link List} of {@link InterviewerSlot}
   */
  @GetMapping("/interviewers/next/slots")
  public ResponseEntity<List<InterviewerSlot>> getInterviewerSlotsForNextWeek(
      Authentication authentication) {
    JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();

    String email = jwtUserDetails.getEmail();
    Long nextWeekId = weekService.getNextWeek().getId();

    List<InterviewerSlot> slots = interviewerSlotService.getSlotsByWeek(email, nextWeekId);

    return new ResponseEntity<>(slots, HttpStatus.OK);
  }
}