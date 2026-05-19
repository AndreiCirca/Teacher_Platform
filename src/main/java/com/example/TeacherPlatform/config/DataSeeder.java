package com.example.TeacherPlatform.config;

import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.*;
import com.example.TeacherPlatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final CourseCategoryRepository courseCategoryRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void run(String... args) throws Exception {

        if (schoolRepository.count() > 0) {
            System.out.println(">>> Baza de date conține deja date. Seeder-ul a fost ignorat.");
            return;
        }

        System.out.println(">>> Pornire Data Seeder (Fără Securitate) pentru FormaProf...");


        School s1 = schoolRepository.save(new School("Colegiul Național 'Emil Racoviță'", "Cluj", "RO1234567", "Str. Mihail Kogălniceanu nr. 9, Cluj-Napoca", "director.racovita@gmail.com", 65));
        School s2 = schoolRepository.save(new School("Liceul Teoretic 'Avram Iancu'", "Cluj", "RO7654321", "Str. Onisifor Ghibu nr. 25, Cluj-Napoca", "avram.iancu.cluj@edu.ro", 48));
        School s3 = schoolRepository.save(new School("Colegiul Național 'Silvania'", "Sălaj", "RO9876543", "Str. Unirii nr. 1, Zalău", "cns.zalau@yahoo.com", 55));
        School s4 = schoolRepository.save(new School("Liceul Teoretic 'German' Satu Mare", "Satu Mare", "RO1122334", "Str. Petőfi Sándor nr. 4, Satu Mare", "german.sm@gmail.com", 32));
        School s5 = schoolRepository.save(new School("Școala Gimnazială 'Ioan Bob'", "Cluj", "RO4433221", "Str. Ioan Bob nr. 10, Cluj-Napoca", "contact@ioanbob.ro", 40));


        String parolaSimpla = "Parola123!";

        User admin = userRepository.save(new User("Andreea", "Popescu", "admin@formaprof.ro", parolaSimpla, UserRole.ADMIN, s1, true, true, "0740111222", "avatar_admin.png"));

        User formator1 = userRepository.save(new User("Marius", "Ionescu", "marius.ionescu@formaprof.ro", parolaSimpla, UserRole.FORMATOR, s2, true, true, "0740333444", "avatar_marius.png"));
        User formator2 = userRepository.save(new User("Miruna", "Dumitrescu", "miruna.d@formaprof.ro", parolaSimpla, UserRole.FORMATOR, s3, true, true, "0740555666", "avatar_miruna.png"));

        User prof1 = userRepository.save(new User("Filip", "Mureșan", "filip.muresan@edu.ro", parolaSimpla, UserRole.PROFESOR, s4, true, true, "0740777888", "avatar_filip.png"));
        User prof2 = userRepository.save(new User("Victor", "Albu", "victor.albu@edu.ro", parolaSimpla, UserRole.PROFESOR, s5, true, true, "0740999000", "avatar_victor.png"));

        CourseCategory cat1 = courseCategoryRepository.save(new CourseCategory("Matematică", "Metodici avansate și didactica matematicii", "#0F6E56", true));
        CourseCategory cat2 = courseCategoryRepository.save(new CourseCategory("Informatică / TIC", "Platforme EdTech și programare modernă", "#BA7517", true));
        CourseCategory cat3 = courseCategoryRepository.save(new CourseCategory("Educație incluzivă", "Strategii practice pentru integrarea elevilor", "#6A1B9A", true));
        CourseCategory cat4 = courseCategoryRepository.save(new CourseCategory("Management școlar", "Ghid de bune practici pentru directori și lideri", "#1565C0", true));
        CourseCategory cat5 = courseCategoryRepository.save(new CourseCategory("Limba română", "Tehnici inovatoare de analiză literară", "#C62828", true));

        Course c1 = courseRepository.save(new Course("Didactica matematicii în gimnaziu", "Optimizarea procesului de predare-învățare a geometriei.", cat1, formator1, LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 24, 25, 2, 3, true, "Online", "https://zoom.us/j/test1", CourseStatus.ACTIVE, "thumb_mate.png"));
        Course c2 = courseRepository.save(new Course("TIC pentru profesorii de liceu", "Integrarea instrumentelor digitale AI în procesul educațional.", cat2, formator1, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 40, 20, 0, 2, true, "Online", "https://zoom.us/j/test2", CourseStatus.ACTIVE, "thumb_tic.png"));
        Course c3 = courseRepository.save(new Course("Educație incluzivă — strategii practice", "Metode de lucru pentru clase cu elevi cu CES.", cat3, formator2, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 16, 30, 0, 2, false, "Sala 12, Corp B - UBB", "", CourseStatus.ACTIVE, "thumb_incluziune.png"));
        Course c4 = courseRepository.save(new Course("Lider în educație: Management modern", "Curs dedicat managementului strategic de succes.", cat4, formator2, LocalDate.now().minusDays(10), LocalDate.now().minusDays(6), 30, 15, 0, 3, true, "Online", "https://zoom.us/j/test4", CourseStatus.COMPLETED, "thumb_management.png"));
        Course c5 = courseRepository.save(new Course("Inovație în predarea limbii române", "Abordări creative în receptarea textului liric.", cat5, formator1, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), 12, 25, 0, 1, true, "Online", "https://zoom.us/j/test5", CourseStatus.DRAFT, "thumb_romana.png"));

        c1.setCurrentEnrolled(2);
        courseRepository.save(c1);

        CourseSession cs1 = courseSessionRepository.save(new CourseSession(c1, "Introducere și Geometrie Plană", LocalDateTime.now().plusDays(5).withHour(9).withMinute(0), LocalDateTime.now().plusDays(5).withHour(13).withMinute(0), "https://zoom.us/j/test1", 1, true));
        CourseSession cs2 = courseSessionRepository.save(new CourseSession(c1, "Metodica Trigonometriei", LocalDateTime.now().plusDays(6).withHour(9).withMinute(0), LocalDateTime.now().plusDays(6).withHour(13).withMinute(0), "https://zoom.us/j/test1", 2, false));
        CourseSession cs3 = courseSessionRepository.save(new CourseSession(c1, "Evaluarea în Geometria Spațială", LocalDateTime.now().plusDays(7).withHour(9).withMinute(0), LocalDateTime.now().plusDays(7).withHour(13).withMinute(0), "https://zoom.us/j/test1", 3, false));

        CourseSession cs4 = courseSessionRepository.save(new CourseSession(c3, "Identificarea nevoilor speciale", LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), LocalDateTime.now().plusDays(2).withHour(14).withMinute(0), "", 1, false));
        CourseSession cs5 = courseSessionRepository.save(new CourseSession(c3, "Adaptarea curriculară individualizată", LocalDateTime.now().plusDays(3).withHour(10).withMinute(0), LocalDateTime.now().plusDays(3).withHour(14).withMinute(0), "", 2, false));

        Enrollment e1 = enrollmentRepository.save(new Enrollment(c1, prof1, EnrollmentStatus.CONFIRMED, false));
        Enrollment e2 = enrollmentRepository.save(new Enrollment(c1, prof2, EnrollmentStatus.CONFIRMED, false));
        Enrollment e3 = enrollmentRepository.save(new Enrollment(c2, prof1, EnrollmentStatus.PENDING, false));
        Enrollment e4 = enrollmentRepository.save(new Enrollment(c3, prof2, EnrollmentStatus.CONFIRMED, false));
        Enrollment e5 = enrollmentRepository.save(new Enrollment(c4, prof1, EnrollmentStatus.COMPLETED, true));

        attendanceRepository.save(new Attendance(cs1, e1, AttendanceStatus.PRESENT));
        attendanceRepository.save(new Attendance(cs1, e2, AttendanceStatus.PRESENT));
        attendanceRepository.save(new Attendance(cs2, e1, AttendanceStatus.NOT_MARKED));
        attendanceRepository.save(new Attendance(cs4, e4, AttendanceStatus.NOT_MARKED));
        attendanceRepository.save(new Attendance(cs5, e4, AttendanceStatus.NOT_MARKED));

        courseMaterialRepository.save(new CourseMaterial(c1, "Suport_Curs_Geometrie.pdf", "application/pdf", 10485760L, "https://storage.supabase.com/materials/geo.pdf", "Suportul teoretic complet pentru modulele 1-3", 14));
        courseMaterialRepository.save(new CourseMaterial(c1, "Fise_Lucru_Gimnaziu.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 2048576L, "https://storage.supabase.com/materials/fise.docx", "Fișe de lucru editabile pentru elevi", 25));
        courseMaterialRepository.save(new CourseMaterial(c2, "Ghid_Instrumente_AI_EdTech.pdf", "application/pdf", 15728640L, "https://storage.supabase.com/materials/ai_edtech.pdf", "Ghid practic utilizare ChatGPT și Curipod", 0));
        courseMaterialRepository.save(new CourseMaterial(c3, "Ghid_CES_Metodica.pdf", "application/pdf", 5242880L, "https://storage.supabase.com/materials/ces.pdf", "Strategii de diferențiere la clasă", 3));
        courseMaterialRepository.save(new CourseMaterial(c4, "Management_Strategic_Scoli.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", 8388608L, "https://storage.supabase.com/materials/management.pptx", "Prezentarea PowerPoint a cursului de management", 19));

        certificateRepository.save(new Certificate(e5, "FORM-2026-00412", LocalDate.now().minusDays(6), CertificateStatus.ACTIVE, "https://storage.supabase.com/certs/cert_management_prof1.pdf"));
        certificateRepository.save(new Certificate(e5, "FORM-2025-00911", LocalDate.of(2025, 11, 10), CertificateStatus.ACTIVE, "https://storage.supabase.com/certs/old_cert1.pdf"));
        certificateRepository.save(new Certificate(e5, "FORM-2025-00754", LocalDate.of(2025, 7, 20), CertificateStatus.ACTIVE, "https://storage.supabase.com/certs/old_cert2.pdf"));
        certificateRepository.save(new Certificate(e5, "FORM-2024-00124", LocalDate.of(2024, 5, 15), CertificateStatus.ACTIVE, "https://storage.supabase.com/certs/old_cert3.pdf"));
        certificateRepository.save(new Certificate(e5, "FORM-2024-00083", LocalDate.of(2024, 3, 22), CertificateStatus.REVOKED, "https://storage.supabase.com/certs/old_cert4_rev.pdf"));

        notificationRepository.save(new Notification(prof1, "Cursul 'Didactica matematicii' începe în curând", "Prima sesiune online pornește în 5 zile la ora 09:00.", NotificationType.REMINDER, false, "/api/courses/" + c1.getId()));
        notificationRepository.save(new Notification(prof1, "Certificat generat cu succes!", "Felicitări! Certificatul tău pentru cursul de Management modern a fost emis.", NotificationType.SUCCESS, false, "/api/certificates"));
        notificationRepository.save(new Notification(prof2, "Înscriere confirmată", "Ai fost acceptat la cursul 'Educație incluzivă — strategii practice'.", NotificationType.INFO, true, "/api/enrollments"));
        notificationRepository.save(new Notification(formator1, "Prezență nesalvată", "Nu ai marcat prezența pentru sesiunea 2 de la Cursul de Didactică.", NotificationType.WARNING, false, "/api/attendances"));
        notificationRepository.save(new Notification(admin, "Cerere nouă de aprobare curs", "Formatorul Marius Ionescu a trimis o propunere de curs nouă spre revizuire.", NotificationType.INFO, false, "/api/courses"));

        System.out.println(">>> Data Seeder finalizat cu succes! Toate datele de test sunt acum în Supabase.");
    }
}