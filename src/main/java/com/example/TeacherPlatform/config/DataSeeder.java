package com.example.TeacherPlatform.config;

import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.*;
import com.example.TeacherPlatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final CourseRepository courseRepository;
    private final CourseSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database...");

        // ── SCHOOLS ──────────────────────────────────────────────────────────
        School school1 = school("Colegiul Național Silvania", "Sălaj", "RO12345678",
                "Str. Unirii 1, Zalău", "director@silvania.ro", 48);
        School school2 = school("Liceul Teoretic Onisifor Ghibu", "Cluj", "RO23456789",
                "Str. Onisifor Ghibu 15, Cluj-Napoca", "director@ghibu.ro", 62);
        School school3 = school("Colegiul Național Mihai Viteazul", "Prahova", "RO34567890",
                "B-dul Republicii 24, Ploiești", "director@mihaivitiazul.ro", 74);
        School school4 = school("Liceul Vocațional Pedagogic", "Mureș", "RO45678901",
                "Str. Gh. Doja 8, Târgu Mureș", "director@pedagogic-ms.ro", 39);
        School school5 = school("Colegiul Național Gheorghe Lazăr", "Sibiu", "RO56789012",
                "Str. Gh. Lazăr 4, Sibiu", "director@lazar-sibiu.ro", 55);
        schoolRepository.saveAll(List.of(school1, school2, school3, school4, school5));

        // ── ADMIN ────────────────────────────────────────────────────────────
        User admin = user("Admin", "Platform", "admin@teacherplatform.ro",
                "Admin@1234", UserRole.ADMIN, null);
        userRepository.save(admin);

        // ── FORMATORI (trainers) ──────────────────────────────────────────────
        User f1 = user("Elena", "Dumitrescu", "elena.dumitrescu@formatori.ro",
                "Formator@1234", UserRole.FORMATOR, school2);
        User f2 = user("Andrei", "Popescu", "andrei.popescu@formatori.ro",
                "Formator@1234", UserRole.FORMATOR, school3);
        User f3 = user("Maria", "Georgescu", "maria.georgescu@formatori.ro",
                "Formator@1234", UserRole.FORMATOR, school5);
        userRepository.saveAll(List.of(f1, f2, f3));

        // ── PROFESORI (teachers) ──────────────────────────────────────────────
        User p1 = user("Ioan", "Mureșan", "ioan.muresan@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school1);
        User p2 = user("Cristina", "Stan", "cristina.stan@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school1);
        User p3 = user("Victor", "Radu", "victor.radu@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school2);
        User p4 = user("Andreea", "Ionescu", "andreea.ionescu@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school3);
        User p5 = user("Mihai", "Vlad", "mihai.vlad@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school4);
        User p6 = user("Laura", "Constantin", "laura.constantin@edu.ro",
                "Profesor@1234", UserRole.PROFESOR, school5);
        userRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        // ── COURSES ───────────────────────────────────────────────────────────
        Course c1 = course(
                "Metode moderne de predare în era digitală",
                "Tehnici active de predare, flip classroom și integrarea instrumentelor digitale în lecție.",
                f1, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20),
                25, 30, true, null, "https://meet.google.com/abc-defg-hij");

        Course c2 = course(
                "Evaluare autentică și feedback formativ",
                "Metode de evaluare continuă, rubrici, portofol​ii digitale și feedback eficient.",
                f1, LocalDate.now().plusDays(5), LocalDate.now().plusDays(35),
                20, 25, true, null, "https://meet.google.com/klm-nopq-rst");

        Course c3 = course(
                "Managementul clasei incluzive",
                "Strategii pentru diferențierea instrucției și sprijinul elevilor cu CES.",
                f2, LocalDate.now().minusDays(5), LocalDate.now().plusDays(15),
                15, 20, false, "Sala de conferințe, Ploiești", null);

        Course c4 = course(
                "Gamification în educație",
                "Proiectarea activităților gamificate și utilizarea platformelor interactive.",
                f2, LocalDate.now().plusDays(14), LocalDate.now().plusDays(44),
                10, 35, true, null, "https://meet.google.com/uvw-xyz-123");

        Course c5 = course(
                "Competențe digitale pentru profesori",
                "Suita Google Workspace for Education, Canva, și instrumente AI pentru pregătirea lecțiilor.",
                f3, LocalDate.now().minusDays(20), LocalDate.now().minusDays(1),
                30, 40, true, null, "https://meet.google.com/456-789-abc");

        courseRepository.saveAll(List.of(c1, c2, c3, c4, c5));

        // Update enrolled counts & status
        c1.setCurrentEnrolled(4); c1.setStatus(CourseStatus.ACTIVE);
        c3.setCurrentEnrolled(2); c3.setStatus(CourseStatus.ACTIVE);
        c5.setCurrentEnrolled(3); c5.setStatus(CourseStatus.COMPLETED);
        courseRepository.saveAll(List.of(c1, c2, c3, c4, c5));

        // ── SESSIONS ──────────────────────────────────────────────────────────
        CourseSession s1a = session(c1, "Flip Classroom — teorie și practică", 1,
                LocalDateTime.now().minusDays(8));
        CourseSession s1b = session(c1, "Instrumente digitale: Padlet, Mentimeter, Kahoot", 2,
                LocalDateTime.now().minusDays(3));
        CourseSession s1c = session(c1, "Proiectarea lecției interactive", 3,
                LocalDateTime.now().plusDays(5));

        CourseSession s3a = session(c3, "Cadrul legal al incluziunii în România", 1,
                LocalDateTime.now().minusDays(4));
        CourseSession s3b = session(c3, "Planuri de intervenție personalizată", 2,
                LocalDateTime.now().plusDays(3));

        CourseSession s5a = session(c5, "Google Classroom și Google Meet", 1,
                LocalDateTime.now().minusDays(18));
        CourseSession s5b = session(c5, "Canva pentru educație", 2,
                LocalDateTime.now().minusDays(11));
        CourseSession s5c = session(c5, "Instrumente AI: ChatGPT, Gemini în predare", 3,
                LocalDateTime.now().minusDays(4));

        s1a.setAttendanceMarked(true);
        s1b.setAttendanceMarked(true);
        s5a.setAttendanceMarked(true);
        s5b.setAttendanceMarked(true);
        s5c.setAttendanceMarked(true);

        sessionRepository.saveAll(List.of(s1a, s1b, s1c, s3a, s3b, s5a, s5b, s5c));

        c1.setSessionCount(3);
        c3.setSessionCount(2);
        c5.setSessionCount(3);
        courseRepository.saveAll(List.of(c1, c3, c5));

        // ── ENROLLMENTS ───────────────────────────────────────────────────────
        Enrollment e_p1_c1 = enrollment(p1, c1, EnrollmentStatus.CONFIRMED);
        Enrollment e_p2_c1 = enrollment(p2, c1, EnrollmentStatus.CONFIRMED);
        Enrollment e_p3_c1 = enrollment(p3, c1, EnrollmentStatus.CONFIRMED);
        Enrollment e_p4_c1 = enrollment(p4, c1, EnrollmentStatus.PENDING);

        Enrollment e_p1_c3 = enrollment(p1, c3, EnrollmentStatus.CONFIRMED);
        Enrollment e_p5_c3 = enrollment(p5, c3, EnrollmentStatus.CONFIRMED);

        Enrollment e_p3_c5 = enrollment(p3, c5, EnrollmentStatus.CONFIRMED);
        Enrollment e_p4_c5 = enrollment(p4, c5, EnrollmentStatus.CONFIRMED);
        Enrollment e_p6_c5 = enrollment(p6, c5, EnrollmentStatus.CONFIRMED);

        enrollmentRepository.saveAll(List.of(
                e_p1_c1, e_p2_c1, e_p3_c1, e_p4_c1,
                e_p1_c3, e_p5_c3,
                e_p3_c5, e_p4_c5, e_p6_c5));

        // ── ATTENDANCE (for past sessions) ────────────────────────────────────
        attendanceRepository.saveAll(List.of(
                att(s1a, e_p1_c1, AttendanceStatus.PRESENT),
                att(s1a, e_p2_c1, AttendanceStatus.PRESENT),
                att(s1a, e_p3_c1, AttendanceStatus.ABSENT),
                att(s1a, e_p4_c1, AttendanceStatus.PRESENT),
                att(s1b, e_p1_c1, AttendanceStatus.PRESENT),
                att(s1b, e_p2_c1, AttendanceStatus.ABSENT),
                att(s1b, e_p3_c1, AttendanceStatus.PRESENT),
                att(s1b, e_p4_c1, AttendanceStatus.PRESENT),

                att(s5a, e_p3_c5, AttendanceStatus.PRESENT),
                att(s5a, e_p4_c5, AttendanceStatus.PRESENT),
                att(s5a, e_p6_c5, AttendanceStatus.PRESENT),
                att(s5b, e_p3_c5, AttendanceStatus.PRESENT),
                att(s5b, e_p4_c5, AttendanceStatus.ABSENT),
                att(s5b, e_p6_c5, AttendanceStatus.PRESENT),
                att(s5c, e_p3_c5, AttendanceStatus.PRESENT),
                att(s5c, e_p4_c5, AttendanceStatus.PRESENT),
                att(s5c, e_p6_c5, AttendanceStatus.PRESENT)
        ));

        // Mark completed course enrollments
        e_p3_c5.setCertificateGenerated(true);
        e_p4_c5.setCertificateGenerated(true);
        e_p6_c5.setCertificateGenerated(true);
        enrollmentRepository.saveAll(List.of(e_p3_c5, e_p4_c5, e_p6_c5));

        log.info("Database seeded successfully. Credentials: admin@teacherplatform.ro / Admin@1234");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private School school(String name, String county, String taxId,
                          String address, String directorEmail, int teacherCount) {
        School s = new School();
        s.setName(name);
        s.setCounty(county);
        s.setTaxId(taxId);
        s.setAddress(address);
        s.setDirectorEmail(directorEmail);
        s.setTeacherCount(teacherCount);
        return s;
    }

    private User user(String firstName, String lastName, String email,
                      String rawPassword, UserRole role, School school) {
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setSchool(school);
        u.setActive(true);
        u.setEmailVerified(true);
        return u;
    }

    private Course course(String title, String description, User trainer,
                          LocalDate start, LocalDate end,
                          int creditHours, int maxParticipants,
                          boolean isOnline, String location, String meetingLink) {
        Course c = new Course();
        c.setTitle(title);
        c.setDescription(description);
        c.setTrainer(trainer);
        c.setStartDate(start);
        c.setEndDate(end);
        c.setCreditHours(creditHours);
        c.setMaxParticipants(maxParticipants);
        c.setCurrentEnrolled(0);
        c.setSessionCount(0);
        c.setIsOnline(isOnline);
        c.setLocation(location);
        c.setMeetingLink(meetingLink);
        c.setStatus(CourseStatus.ACTIVE);
        c.setThumbnailUrl("");
        return c;
    }

    private CourseSession session(Course course, String topic, int number, LocalDateTime start) {
        CourseSession cs = new CourseSession();
        cs.setCourse(course);
        cs.setTopic(topic);
        cs.setSessionNumber(number);
        cs.setStartTime(start);
        cs.setEndTime(start.plusHours(2));
        cs.setMeetingLink(course.getMeetingLink());
        cs.setAttendanceMarked(false);
        return cs;
    }

    private Enrollment enrollment(User teacher, Course course, EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setTeacher(teacher);
        e.setCourse(course);
        e.setStatus(status);
        e.setCertificateGenerated(false);
        return e;
    }

    private Attendance att(CourseSession session, Enrollment enrollment, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setSession(session);
        a.setEnrollment(enrollment);
        a.setStatus(status);
        return a;
    }
}
