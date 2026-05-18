# FormaProf Backend Architecture Documentation


---

## Database Configuration

### Connection Details
- **Host:** db.xgqbmgtrxzenjhoxttxp.supabase.co
- **Port:** 5432
- **Database:** postgres
- **Username:** postgres
- **Password:** camera710sicirca

### Configuration File: `application.properties`
```
spring.datasource.url=jdbc:postgresql://db.xgqbmgtrxzenjhoxttxp.supabase.co:5432/postgres?user=postgres&password=camera710sicirca
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## Core Architecture

### 1. **Base Entity Class** (`BaseEntity.java`)
All domain entities extend this abstract class to inherit:
- `id`: Auto-generated primary key
- `createdAt`: Timestamp when the entity was created
- `updatedAt`: Timestamp when the entity was last updated

Hibernate automatically populates these timestamps using `@PrePersist` and `@PreUpdate` annotations.

---

## Entity Models

### User Management

#### **User.java**
Represents all users on the platform with three role types.

**Fields:**
- `firstName`, `lastName`: User's name
- `email`: Unique, used for authentication
- `password`: User's password
- `role`: UserRole enum (PROFESOR, FORMATOR, ADMIN)
- `school`: Many-to-One relationship with School
- `active`: Boolean flag for user account status
- `emailVerified`: Flag for email verification status
- `phoneNumber`, `avatarUrl`: Optional profile fields

**Role Types:**
```
PROFESOR  - Teacher attending courses
FORMATOR  - Trainer/instructor conducting courses
ADMIN     - Platform administrator
```

#### **School.java**
Represents educational institutions.

**Fields:**
- `name`: School name (unique)
- `county`: Romanian county
- `cui`: Tax ID (CUI - unique)
- `address`: School address
- `directorEmail`: Director's email
- `teacherCount`: Number of enrolled teachers

---

### Course Management

#### **CourseCategory.java**
Predefined course categories for organization.

**Fields:**
- `name`: Category name (e.g., "Matematică", "TIC")
- `description`: Category description
- `color`: Hex color for UI representation
- `active`: Boolean to enable/disable category

#### **Course.java**
Core entity representing a training course.

**Fields:**
- `title`: Course name
- `description`: Detailed course description (TEXT)
- `category`: Many-to-One reference to CourseCategory
- `trainer`: Many-to-One reference to User (FORMATOR role)
- `startDate`, `endDate`: Course date range
- `creditHours`: Accredited credit hours
- `maxParticipants`: Maximum enrollment capacity
- `currentEnrolled`: Current number of enrolled teachers
- `sessionCount`: Total number of course sessions
- `isOnline`: Boolean for online/in-person format
- `location`: Physical location (if in-person)
- `meetingLink`: Video conference URL (if online)
- `status`: CourseStatus enum
- `thumbnailUrl`: Course image URL

**Course Status Types:**
```
DRAFT              - Course being created
ACTIVE             - Currently running/accepting enrollments
COMPLETED          - Course finished
CANCELLED          - Course cancelled
PENDING_APPROVAL   - Awaiting admin approval
```

#### **CourseSession.java**
Individual sessions within a course.

**Fields:**
- `course`: Many-to-One reference to Course
- `topic`: Session topic/subject
- `startTime`, `endTime`: Session date and time
- `meetingLink`: Meeting URL for this session
- `sessionNumber`: Sequence number (1, 2, 3, etc.)
- `attendanceMarked`: Boolean indicating if attendance has been recorded

---

### Enrollment Management

#### **Enrollment.java**
Represents a teacher's enrollment in a course (unique per teacher-course pair).

**Fields:**
- `course`: Many-to-One reference to Course
- `teacher`: Many-to-One reference to User (PROFESOR role)
- `status`: EnrollmentStatus enum
- `certificateGenerated`: Boolean flag

**Enrollment Status Types:**
```
PENDING    - Enrollment awaiting confirmation
CONFIRMED  - Enrollment confirmed
COMPLETED  - Course completed by teacher
CANCELLED  - Enrollment cancelled
```

#### **Attendance.java**
Tracks attendance for each session per enrolled teacher (unique per session-enrollment pair).

**Fields:**
- `session`: Many-to-One reference to CourseSession
- `enrollment`: Many-to-One reference to Enrollment
- `status`: AttendanceStatus enum

**Attendance Status Types:**
```
PRESENT    - Teacher attended the session
ABSENT     - Teacher was absent
NOT_MARKED - Attendance not yet recorded
```

---

### Certification

#### **Certificate.java**
Represents issued certificates for completed courses.

**Fields:**
- `enrollment`: One-to-One reference to Enrollment
- `certificateCode`: Unique verification code (format: FORM-YYYY-XXXXX)
- `issuedDate`: Date certificate was issued
- `status`: CertificateStatus enum
- `certificateUrl`: Download URL for PDF certificate

**Certificate Status Types:**
```
ACTIVE   - Active, valid certificate
REVOKED  - Certificate revoked
PENDING  - Certificate generation pending
```

---

### Course Materials

#### **CourseMaterial.java**
Represents files uploaded by trainers for courses.

**Fields:**
- `course`: Many-to-One reference to Course
- `fileName`: File name with extension
- `fileType`: File type (pdf, ppt, doc, etc.)
- `fileSize`: File size in bytes
- `fileUrl`: Download/storage URL
- `description`: Optional file description
- `downloadCount`: Number of times downloaded

---

### Notifications

#### **Notification.java**
User notifications for important platform events.

**Fields:**
- `user`: Many-to-One reference to User (recipient)
- `title`: Notification title
- `message`: Notification message (TEXT field)
- `type`: NotificationType enum
- `read`: Boolean flag for read status
- `actionUrl`: Optional URL for action-driven notifications

**Notification Types:**
```
INFO       - General information
WARNING    - Warning notification
SUCCESS    - Success confirmation
ERROR      - Error alert
REMINDER   - Reminder notification
```

---

## Repository Layer

All repositories extend the **GenericRepository** interface, which extends JpaRepository and provides standard CRUD operations.


**Standard Methods :**
- `save(T)` - Create or update entity
- `findById(ID)` - Find by primary key
- `findAll()` - Get all entities
- `delete(T)` - Delete entity
- `count()` - Count total entities

### Specialized Query Methods by Repository

#### **UserRepository**
- `findByEmail(String)` - Find user by email
- `findByRole(UserRole)` - Find all users with specific role
- `findBySchoolId(Long)` - Find users from a school
- `findByRoleAndActive(UserRole, Boolean)` - Filter by role and status
- `countByRole(UserRole)` - Count users by role
- `findUnverifiedUsers()` - Find users pending email verification

#### **CourseRepository**
- `findByStatus(CourseStatus)` - Filter by course status
- `findByTrainerId(Long)` - Find courses by trainer
- `findByCategoryId(Long)` - Find courses by category
- `findOnlineCourses()` - Find all online courses
- `findUpcomingCourses(LocalDate)` - Find courses starting after a date
- `findCoursesByDateRange(LocalDate, LocalDate)` - Find courses within date range
- `findPendingApprovalCourses()` - Find courses awaiting admin approval
- `findAvailableCourses()` - Find courses with available spots

#### **CourseSessionRepository**
- `findByCourseId(Long)` - Find all sessions for a course
- `findCourseSessionsOrdered(Long)` - Sessions ordered by sequence
- `findSessionsByTimeRange(LocalDateTime, LocalDateTime)` - Sessions in time range
- `findUnmarkedAttendanceSessions()` - Sessions without recorded attendance

#### **EnrollmentRepository**
- `findByTeacherId(Long)` - Find enrollments by teacher
- `findByCourseId(Long)` - Find enrollments by course
- `findByStatus(EnrollmentStatus)` - Filter by enrollment status
- `findByCourseIdAndTeacherId(Long, Long)` - Find specific enrollment
- `findConfirmedEnrollmentsByTeacher(Long)` - Confirmed enrollments for teacher
- `findConfirmedEnrollmentsByCourse(Long)` - Confirmed enrollments in course
- `countConfirmedEnrollmentsByCourse(Long)` - Count confirmed participants
- `findPendingEnrollments()` - Find pending enrollments

#### **AttendanceRepository**
- `findBySessionId(Long)` - Find attendance records for session
- `findByEnrollmentId(Long)` - Find attendance records for enrollment
- `findBySessionIdAndEnrollmentId(Long, Long)` - Find specific attendance record
- `findAttendanceByEnrollmentOrdered(Long)` - Ordered attendance records
- `countPresentSessionsByEnrollment(Long)` - Count sessions attended
- `countPresentTeachersInSession(Long)` - Count present teachers in session

#### **CertificateRepository**
- `findByCertificateCode(String)` - Find by verification code
- `findByStatus(CertificateStatus)` - Filter by status
- `findCertificatesByTeacher(Long)` - Find certificates issued to teacher
- `findCertificatesByCourse(Long)` - Find certificates for course
- `countActiveCertificatesByTeacher(Long)` - Count active certificates
- `findCertificatesByDateRange(LocalDate, LocalDate)` - Find by issue date
- `findPendingCertificates()` - Find certificates pending generation

#### **CourseMaterialRepository**
- `findByCourseId(Long)` - Find materials for course
- `findCourseMaterialsOrdered(Long)` - Materials ordered by upload date
- `findByFileType(String)` - Find materials by type

#### **NotificationRepository**
- `findByUserId(Long)` - Find notifications for user
- `findUnreadNotificationsByUser(Long)` - Find unread notifications
- `findRecentNotificationsByUser(Long)` - Find last 10 notifications
- `findByType(NotificationType)` - Find notifications by type
- `countUnreadNotifications(Long)` - Count unread notifications

---

## Key Relationships

### One-to-Many Relationships
- User (FORMATOR) → Courses (trainer)
- School → Users
- CourseCategory → Courses
- Course → CourseSessions
- Course → Enrollments
- Course → CourseMaterials
- CourseSession → Attendance records
- Enrollment → Attendance records
- User → Notifications

### Many-to-One Relationships
- Enrollment → User (teacher)
- Enrollment → Course
- Course → User (trainer)
- Course → CourseCategory
- CourseMaterial → Course
- Notification → User

### One-to-One Relationships (Unique Constraints)
- Enrollment (teacher_id + course_id)
- Attendance (session_id + enrollment_id)

---

## File Structure

```
TeacherPlatform/
├── src/main/java/com/example/TeacherPlatform/
│   ├── model/
│   │   ├── BaseEntity.java
│   │   ├── User.java
│   │   ├── UserRole.java
│   │   ├── School.java
│   │   ├── Course.java
│   │   ├── CourseStatus.java
│   │   ├── CourseCategory.java
│   │   ├── CourseSession.java
│   │   ├── Enrollment.java
│   │   ├── EnrollmentStatus.java
│   │   ├── Attendance.java
│   │   ├── AttendanceStatus.java
│   │   ├── Certificate.java
│   │   ├── CertificateStatus.java
│   │   ├── CourseMaterial.java
│   │   ├── Notification.java
│   │   └── NotificationType.java
│   ├── repository/
│   │   ├── GenericRepository.java
│   │   ├── UserRepository.java
│   │   ├── SchoolRepository.java
│   │   ├── CourseCategoryRepository.java
│   │   ├── CourseRepository.java
│   │   ├── CourseSessionRepository.java
│   │   ├── EnrollmentRepository.java
│   │   ├── AttendanceRepository.java
│   │   ├── CertificateRepository.java
│   │   ├── CourseMaterialRepository.java
│   │   └── NotificationRepository.java
│   ├── TeacherPlatformApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## Next Steps

1. **Create Service Layer** - Implement business logic services that use repositories
2. **Create DTOs** - Data Transfer Objects for API requests/responses
3. **Create REST Controllers** - API endpoints for each entity
4. **Add Exception Handling** - Custom exceptions and global error handlers
5. **Add Security** - Spring Security for authentication and authorization
6. **Add Validation** - Input validation annotations and custom validators
7. **Add Testing** - Unit and integration tests


## Database Schema Notes

- PostgreSQL is configured with Hibernate DDL set to `update` mode
- All timestamp fields are automatically managed
- Foreign keys are created automatically by Hibernate
- Unique constraints are enforced at database level
- Indexes are recommended on frequently queried fields:
  - User.email
  - Course.status
  - Enrollment.status
  - Certificate.certificateCode


