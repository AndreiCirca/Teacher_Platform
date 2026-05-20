package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.AuthRequest;
import com.example.TeacherPlatform.dataTransferObject.AuthResponse;
import com.example.TeacherPlatform.dataTransferObject.RegisterRequest;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SchoolService schoolService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
            throw new RuntimeException("Acest email este deja folosit.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        user.setEmailVerified(false); // Conform cerințelor, implicit e false

        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Școala nu a fost găsită"));
            user.setSchool(school);
        }

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == UserRole.PROFESOR && savedUser.getSchool() != null) {
            schoolService.incrementTeacherCount(savedUser.getSchool().getId());
        }

        log.info("S-a trimis email de verificare către: " + savedUser.getEmail());

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        return new AuthResponse(token, savedUser.getEmail(),
                savedUser.getRole().name(), savedUser.getFirstName(), savedUser.getLastName());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Email sau parolă incorectă."));

        if (!user.getActive()) {
            throw new RuntimeException("Acest cont este dezactivat.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(), request.getPassword()));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(),
                user.getRole().name(), user.getFirstName(), user.getLastName());
    }

    @Transactional
    public String verifyEmail(String token) {
        // În producție, token-ul e validat. Pentru MVP, simulăm că token = emailul encodat sau un identificator
        // Presupunem că JWT Util poate extrage email-ul dintr-un token valid de înregistrare
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token de verificare invalid sau expirat.");
        }
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        user.setEmailVerified(true);
        userRepository.save(user);
        return "Email verificat cu succes. Contul este acum activat complet.";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Nu există niciun cont asociat cu acest email."));

        // Generăm un token temporar (aici folosim JWT-ul nostru ca token de resetare)
        String resetToken = jwtUtil.generateToken(user.getEmail(), "RESET_PASSWORD");
        log.info("S-a trimis email de resetare parolă către: " + email + ". Token: " + resetToken);

        return "Un email cu instrucțiuni pentru resetarea parolei a fost trimis.";
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token de resetare invalid sau expirat.");
        }
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Parola a fost resetată cu succes.";
    }

    public AuthResponse refreshToken(String oldToken) {
        if (oldToken != null && oldToken.startsWith("Bearer ")) {
            oldToken = oldToken.substring(7);
        }

        if (!jwtUtil.isTokenValid(oldToken)) {
            throw new RuntimeException("Token invalid. Nu se poate face refresh.");
        }

        String email = jwtUtil.extractEmail(oldToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(newToken, user.getEmail(), user.getRole().name(), user.getFirstName(), user.getLastName());
    }
}