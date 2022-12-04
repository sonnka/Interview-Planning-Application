package com.intellias.intellistart.interviewplanning.controllers.dto;

import com.intellias.intellistart.interviewplanning.model.dayofweek.DayOfWeek;
import com.intellias.intellistart.interviewplanning.model.week.WeekService;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class DashboardMapDtoTest {

  private static final WeekService weekService = Mockito.mock(WeekService.class);

  private static Arguments[] getArgsForInitializing() {

    DashboardMapDto firstExpected = new DashboardMapDto(10L, weekService);
    firstExpected.setDashboard(new HashMap<>());
    Arrays.stream(DayOfWeek.values()).forEach(d -> firstExpected.getDashboard().put(
        weekService.convertToLocalDate(10L, d), new DashboardDto()
    ));

    return new Arguments[] {
        Arguments.of(10L, weekService, firstExpected)
    };
  }

  @ParameterizedTest
  @MethodSource("getArgsForInitializing")
  void initializeDashboardMapDto(Long weekNum, WeekService weekService, DashboardMapDto expected) {

    DashboardMapDto actual = new DashboardMapDto(weekNum, weekService);
    Assertions.assertEquals(expected, actual);
  }
}
