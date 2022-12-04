package com.intellias.intellistart.interviewplanning.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for Interviewer SLot DTO.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class InterviewerSlotDto {

  private Long interviewerId;
  private Long interviewerSlotId;
  private Long week;
  @NonNull
  private String dayOfWeek;
  @NonNull
  private String from;
  @NonNull
  private String to;

}
