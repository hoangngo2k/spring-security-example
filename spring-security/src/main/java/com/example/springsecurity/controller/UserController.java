package com.example.springsecurity.controller;

import com.example.springsecurity.dto.LoginRequest;
import com.example.springsecurity.dto.SignUpRequest;
import com.example.springsecurity.dto.UserDto;
import com.example.springsecurity.entity.Role;
import com.example.springsecurity.entity.User;
import com.example.springsecurity.enums.ERole;
import com.example.springsecurity.service.RoleService;
import com.example.springsecurity.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, RoleService roleService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public ModelAndView login(Model model) {
        LoginRequest loginRequest = new LoginRequest();
        model.addAttribute("login", loginRequest);
        return new ModelAndView("login");
    }

    @PostMapping("/signin")
    public RedirectView login(@Validated @ModelAttribute("user") LoginRequest loginRequest, Model model) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDto userDto = (UserDto) authentication.getPrincipal();
        model.addAttribute("user", userDto);
        List<String> roles = userDto.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new RedirectView("/home");
    }

    @GetMapping("/register")
    public ModelAndView register(Model model) {
        SignUpRequest signUpRequest = new SignUpRequest();
        model.addAttribute(signUpRequest);
        return new ModelAndView("register");
    }

    @PostMapping("/signup")
    public RedirectView registerUser(@ModelAttribute("user") SignUpRequest signUpRequest, Model model) {
        if (userService.existsUserByUsername(signUpRequest.getUsername()) || userService.existsUserByEmail(signUpRequest.getEmail()))
            return new RedirectView("/register");
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword()));
        Set<String> roleStr = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if(roleStr == null) {
            Role role = roleService.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role is not found"));
            roles.add(role);
        } else {
            roleStr.forEach(s -> {
                switch (s) {
                    case "admin" -> {
                        Role roleAdmin = roleService.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role is not found"));
                        roles.add(roleAdmin);
                    }
                    case "user" -> {
                        Role role = roleService.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Role is not found"));
                        roles.add(role);
                    }
                }
            });
        }
        user.setRoles(roles);
        userService.save(user);
        return new RedirectView("/login");
    }
}
