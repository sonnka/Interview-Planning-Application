package com.intellias.intellistart.interviewplanning.controllers;

import com.intellias.intellistart.interviewplanning.controllers.dto.CandidateDto;
import com.intellias.intellistart.interviewplanning.controllers.dto.FacebookOauthInfoDto;
import com.intellias.intellistart.interviewplanning.controllers.dto.JwtRequest;
import com.intellias.intellistart.interviewplanning.controllers.dto.JwtResponse;
import com.intellias.intellistart.interviewplanning.exceptions.SecurityException;
import com.intellias.intellistart.interviewplanning.exceptions.SecurityException.SecurityExceptionProfile;
import com.intellias.intellistart.interviewplanning.model.user.User;
import com.intellias.intellistart.interviewplanning.model.user.UserService;
import com.intellias.intellistart.interviewplanning.security.JwtUserDetails;
import com.intellias.intellistart.interviewplanning.security.JwtUserDetailsService;
import com.intellias.intellistart.interviewplanning.utils.FacebookUtil;
import com.intellias.intellistart.interviewplanning.utils.FacebookUtil.FacebookScopes;
import com.intellias.intellistart.interviewplanning.utils.JwtUtil;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

/**
 * Controller for authentication and authenticated requests.
 */
@RestController
@CrossOrigin
public class JwtAuthenticationController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final JwtUserDetailsService userDetailsService;
  private final FacebookUtil facebookUtil;
  private final UserService userService;

  /**
   * Constructor.
   */
  @Autowired
  public JwtAuthenticationController(AuthenticationManager authenticationManager,
      JwtUtil jwtUtil, JwtUserDetailsService userDetailsService,
      FacebookUtil facebookUtil, UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.facebookUtil = facebookUtil;
    this.userService = userService;
  }

  /**
   * Method that mappings the authentication request through generating
   * JWT by Facebook Token.
   *
   * @param jwtRequest object with facebookToken field - gained by user oauth2 token
   * @return JWT
   */
  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  public ResponseEntity<?> createAuthenticationToken(
      @RequestBody JwtRequest jwtRequest) {

    Map<FacebookScopes, String> userScopes;
    try {
      userScopes = facebookUtil
          .getScope(jwtRequest.getFacebookToken());
    } catch (RestClientException e) {
      throw new SecurityException(SecurityExceptionProfile.BAD_FACEBOOK_TOKEN);
    }

    String email = userScopes.get(FacebookScopes.EMAIL);
    String name = userScopes.get(FacebookScopes.NAME);

    authenticate(email);

    final JwtUserDetails userDetails = (JwtUserDetails) userDetailsService
        .loadUserByEmailAndName(email, name);

    JwtResponse jwtResponse = new JwtResponse(jwtUtil.generateToken(userDetails));

    return ResponseEntity.ok(jwtResponse);
  }

  private void authenticate(String username) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, username));
    } catch (BadCredentialsException e) {
      throw new SecurityException(SecurityExceptionProfile.BAD_CREDENTIALS);
    }
  }

  /**
   * GET request for getting info about current User.
   *
   * @param authentication - Spring security auth object.
   *
   * @return User - user object with current info.
   */
  @GetMapping("/me")
  public ResponseEntity<?> getMyself(Authentication authentication) {

    JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();

    User user = userService.getUserByEmail(jwtUserDetails.getEmail());
    if (user == null) {
      return ResponseEntity.ok(new CandidateDto(jwtUserDetails.getEmail()));
    }

    return ResponseEntity.ok(user);
  }

  /**
   * GET request for getting application facebook client id.
   *
   * @param facebookClientId auto-injected from environmental variables facebook client id.
   * @return DTO with simple string.
   */
  @GetMapping("/oauth2/facebook/v15.0")
  public FacebookOauthInfoDto getFacebookClientId(
      @Value("${spring.security.oauth2.client.registration.facebook.clientId}")
      String facebookClientId,
      @Value("${spring.security.oauth2.client.registration.facebook.redirectUri}")
      String redirectUri) {

    String requestUrl = String.format(FacebookUtil.userFacebookTokenUrlV15,
        facebookClientId, redirectUri);

    return new FacebookOauthInfoDto(facebookClientId, redirectUri, requestUrl);
  }
}