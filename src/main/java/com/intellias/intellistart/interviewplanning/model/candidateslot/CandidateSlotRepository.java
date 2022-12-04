package com.intellias.intellistart.interviewplanning.model.candidateslot;

import com.intellias.intellistart.interviewplanning.model.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

/**
 * DAO for CandidateSlot entity.
 */
public interface CandidateSlotRepository extends CrudRepository<CandidateSlot, Long> {
  List<CandidateSlot> findByEmail(String email);

  List<CandidateSlot> findByEmailAndDate(String email, LocalDate date);

  Set<CandidateSlot> findByDate(LocalDate date);
}
