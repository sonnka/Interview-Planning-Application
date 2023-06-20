package com.interviewplanning.controllers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.interviewplanning.model.candidateslot.CandidateSlot;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for CandidateSlot.
 */
@Getter
@Setter
@NoArgsConstructor
public class CandidateSlotDto {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate date;

  private String from;

  private String to;

  /**
   * Create CandidateSlotDto object from CandidateSlot.
   *
   * @param candidateSlot - The object for which a DTO is created.
   */
  public CandidateSlotDto(CandidateSlot candidateSlot) {
    this.setDate(candidateSlot.getDate());
    this.setFrom(candidateSlot.getPeriod().getFrom().toString());
    this.setTo(candidateSlot.getPeriod().getTo().toString());
  }
}
