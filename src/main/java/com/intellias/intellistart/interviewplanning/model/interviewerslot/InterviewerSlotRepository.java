package com.intellias.intellistart.interviewplanning.model.interviewerslot;

import com.intellias.intellistart.interviewplanning.model.dayofweek.DayOfWeek;
import com.intellias.intellistart.interviewplanning.model.user.User;
import com.intellias.intellistart.interviewplanning.model.week.Week;
import com.intellias.intellistart.interviewplanning.model.week.Week;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

/**
 * DAO for InterviewerSlot entity.
 */
public interface InterviewerSlotRepository extends CrudRepository<InterviewerSlot, Long> {
  List<InterviewerSlot> getInterviewerSlotsByUserIdAndWeekIdAndDayOfWeek(
      Long userId, Long weekId, DayOfWeek dayOfWeek);

  List<InterviewerSlot> findInterviewerSlotsByUser(User user);

  List<InterviewerSlot> getInterviewerSlotsByUserEmailAndWeekId(String userEmail, Long weekId);

  Set<InterviewerSlot> findByWeek(Week week);
}
