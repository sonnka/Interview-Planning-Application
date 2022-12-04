package com.intellias.intellistart.interviewplanning.initialization;

import com.intellias.intellistart.interviewplanning.model.user.Role;
import com.intellias.intellistart.interviewplanning.model.user.User;
import com.intellias.intellistart.interviewplanning.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Class for initializing first coordinator in the system.
 */
@Component
public class StartDataLoader implements ApplicationRunner {

  private final UserRepository userRepository;

  @Value("${first-coordinator-email}")
  private String email;

  @Autowired
  public StartDataLoader(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {

    User firstCoordinator = new User(null, email, Role.COORDINATOR);
    firstCoordinator = userRepository.save(firstCoordinator);
    System.out.println("Added first user: " + firstCoordinator);
  }
}