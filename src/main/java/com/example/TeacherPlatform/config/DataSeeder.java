package com.example.TeacherPlatform.config;

import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.*;
import com.example.TeacherPlatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // [SMART CHECK] Dacă avem deja utilizatori, oprim seeder-ul ca să nu duplicăm datele
        if (userRepository.count() > 0) {
            log.info("Baza de date conține deja date. Trecem peste DataSeeder.");
            return;
        }

        log.info("Începem popularea bazei de date cu informații inițiale...");

        // 1. ȘCOLI
        School cns = new School();
        cns.setName("Colegiul Național Silvania");
        cns.setCounty("Sălaj");
        cns.setTaxId("RO123456");
        cns.setAddress("Strada Unirii, Zalău");
        cns.setTeacherCount(45);
        schoolRepository.save(cns);

        School ubb = new School();
        ubb.setName("Facultatea de Matematică și Informatică");
        ubb.setCounty("Cluj");
        ubb.setTaxId("RO654321");
        ubb.setAddress("Strada Kogălniceanu, Cluj-Napoca");
        ubb.setTeacherCount(120);
        schoolRepository.save(ubb);

        // 2. UTILIZATORI (Formatori / Echipă)
        User alin = createUser("Alin", "Pop", "alin@tech.ro", UserRole.FORMATOR, cns);
        User andreea = createUser("Andreea", "Ionescu", "andreea@tech.ro", UserRole.FORMATOR, ubb);

        // 3. UTILIZATORI (Profesori / Cursanți)
        User filip = createUser("Filip", "Mureșan", "filip@edu.ro", UserRole.PROFESOR, cns);
        User victor = createUser("Victor", "Dan", "victor@edu.ro", UserRole.PROFESOR, cns);
        User marc = createUser("Marc", "Stan", "marc@edu.ro", UserRole.PROFESOR, ubb);
        User miruna = createUser("Miruna", "Radu", "miruna@edu.ro", UserRole.PROFESOR, ubb);
        User marius = createUser("Marius", "Vlad", "marius@edu.ro", UserRole.PROFESOR, cns);

        userRepository.saveAll(List.of(alin, andreea, filip, victor, marc, miruna, marius));

        // 4. CATEGORII CURSURI
        CourseCategory catRobotics = new CourseCategory();
        catRobotics.setName("Robotică și Automatizări");
        catRobotics.setColor("#E74C3C");
        categoryRepository.save(catRobotics);

        CourseCategory catOOP = new CourseCategory();
        catOOP.setName("Programare și Arhitectură");
        catOOP.setColor("#3498DB");
        categoryRepository.save(catOOP);

        // 5. CURSURI
        Course courseRos = new Course();
        courseRos.setTitle("Navigație Autonomă cu ROS 2");
        courseRos.setDescription("Implementare SLAM și configurare environment pentru roboți mobili.");
        courseRos.setCategory(catRobotics);
        courseRos.setTrainer(alin);
        courseRos.setStartDate(LocalDate.now().plusDays(5));
        courseRos.setEndDate(LocalDate.now().plusDays(20));
        courseRos.setCreditHours(20);
        courseRos.setMaxParticipants(15);
        courseRos.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(courseRos);

        Course courseCleanCode = new Course();
        courseCleanCode.setTitle("Clean Code și Design Patterns");
        courseCleanCode.setDescription("Principii SOLID și modelare UML în proiecte complexe Java/C++.");
        courseCleanCode.setCategory(catOOP);
        courseCleanCode.setTrainer(andreea);
        courseCleanCode.setStartDate(LocalDate.now().minusDays(10)); // A început deja
        courseCleanCode.setEndDate(LocalDate.now().plusDays(5));
        courseCleanCode.setCreditHours(15);
        courseCleanCode.setMaxParticipants(25);
        courseCleanCode.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(courseCleanCode);

        // 6. SESIUNI (Course Sessions)
        CourseSession session1 = createSession(courseCleanCode, "Principiile SOLID aplicate", 1, LocalDateTime.now().minusDays(2));
        CourseSession session2 = createSession(courseCleanCode, "Design Patterns (Factory, Strategy)", 2, LocalDateTime.now().plusDays(2));
        sessionRepository.saveAll(List.of(session1, session2));

        // 7. ENROLLMENTS
        Enrollment enFilip = createEnrollment(courseCleanCode, filip);
        Enrollment enVictor = createEnrollment(courseCleanCode, victor);
        Enrollment enMarc = createEnrollment(courseRos, marc);
        enrollmentRepository.saveAll(List.of(enFilip, enVictor, enMarc));

        // 8. PREZENȚE (Attendance) - Marcăm prezența pentru prima sesiune de Clean Code
        Attendance attFilip = createAttendance(session1, enFilip, AttendanceStatus.PRESENT);
        Attendance attVictor = createAttendance(session1, enVictor, AttendanceStatus.ABSENT);
        attendanceRepository.saveAll(List.of(attFilip, attVictor));

        // Setăm că s-a făcut prezența la sesiune
        session1.setAttendanceMarked(true);
        sessionRepository.save(session1);

        log.info("Baza de date a fost populată cu succes!");
    }

    // Helper methods pentru a păstra codul din run() curat
    private User createUser(String firstName, String lastName, String email, UserRole role, School school) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("parola123"); // Deocamdată plaintext, cum e în logica ta actuală
        user.setRole(role);
        user.setSchool(school);
        user.setActive(true);
        user.setEmailVerified(true);
        return user;
    }

    private CourseSession createSession(Course course, String topic, int number, LocalDateTime start) {
        CourseSession session = new CourseSession();
        session.setCourse(course);
        session.setTopic(topic);
        session.setSessionNumber(number);
        session.setStartTime(start);
        session.setEndTime(start.plusHours(2));
        session.setMeetingLink("https://meet.google.com/test-link");
        session.setAttendanceMarked(false);
        return session;
    }

    private Enrollment createEnrollment(Course course, User teacher) {
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setTeacher(teacher);
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setCertificateGenerated(false);
        return enrollment;
    }

    private Attendance createAttendance(CourseSession session, Enrollment enrollment, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setSession(session);
        attendance.setEnrollment(enrollment);
        attendance.setStatus(status);
        return attendance;
    }
}