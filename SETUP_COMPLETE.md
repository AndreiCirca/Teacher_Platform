


###  **13 JPA Entity Classes**
```
User                Parent entity for all users (teachers, trainers, admins)
School              Educational institutions
CourseCategory      Course categorization system
Course              Main course entity with full details
CourseSession       Individual sessions within courses
Enrollment          Teacher-course relationships
Attendance          Session attendance tracking
Certificate         Issued certificates with verification codes
CourseMaterial      Course files and resources
Notification        User notification system
```

###  **6 Enum Classes for Type Safety**
```
UserRole            PROFESOR | FORMATOR | ADMIN
CourseStatus        DRAFT | ACTIVE | COMPLETED | CANCELLED | PENDING_APPROVAL
EnrollmentStatus    PENDING | CONFIRMED | COMPLETED | CANCELLED
AttendanceStatus    PRESENT | ABSENT | NOT_MARKED
CertificateStatus   ACTIVE | REVOKED | PENDING
NotificationType    INFO | WARNING | SUCCESS | ERROR | REMINDER
```

###  **11 Repository Interfaces** (+ 1 Generic Base)
```
GenericRepository               Base CRUD interface
├── UserRepository             (6 custom query methods)
├── SchoolRepository           (4 custom methods)
├── CourseCategoryRepository   (2 custom methods)
├── CourseRepository           (7 custom query methods)
├── CourseSessionRepository    (4 custom query methods)
├── EnrollmentRepository       (8 custom query methods)
├── AttendanceRepository       (7 custom query methods)
├── CertificateRepository      (7 custom query methods)
├── CourseMaterialRepository   (3 custom methods)
└── NotificationRepository     (5 custom methods)
```

###  **Complete Documentation** 
```
 BACKEND_ARCHITECTURE.md        - Detailed technical reference
 BACKEND_QUICK_REF.md           - Quick start guide
 CREATED_FILES_INVENTORY.md     - Complete file listing
```

---

##  Database Connection

**Configuration:**  To be found in `application.properties`

```properties
URL:      jdbc:postgresql://db.xgqbmgtrxzenjhoxttxp.supabase.co:5432/postgres
Username: postgres
Password: camera710sicirca
Driver:   PostgreSQL 42.x
ORM:      Hibernate 7.2.12
```

---

## Complete File Structure

```
TeacherPlatform/
├── src/main/java/com/example/TeacherPlatform/
│   ├── model/ (17 files)
│   │   ├── BaseEntity.java
│   │   ├── User.java + UserRole.enum
│   │   ├── School.java
│   │   ├── Course.java + CourseStatus.enum
│   │   ├── CourseCategory.java
│   │   ├── CourseSession.java
│   │   ├── Enrollment.java + EnrollmentStatus.enum
│   │   ├── Attendance.java + AttendanceStatus.enum
│   │   ├── Certificate.java + CertificateStatus.enum
│   │   ├── CourseMaterial.java
│   │   ├── Notification.java + NotificationType.enum
│   │   └── BaseEntity.java (abstract base)
│   │
│   └── repository/ (11 files)
│       ├── GenericRepository.java
│       ├── UserRepository.java
│       ├── SchoolRepository.java
│       ├── CourseCategoryRepository.java
│       ├── CourseRepository.java
│       ├── CourseSessionRepository.java
│       ├── EnrollmentRepository.java
│       ├── AttendanceRepository.java
│       ├── CertificateRepository.java
│       ├── CourseMaterialRepository.java
│       └── NotificationRepository.java
│
├── src/main/resources/
│   └── application.properties 
│
├── pom.xml 
├── BACKEND_ARCHITECTURE.md
├── BACKEND_QUICK_REF.md
├── CREATED_FILES_INVENTORY.md
└── [Maven, Git, and IDE files...]
```

---

##  Quick Start

### 1. **Compile the Project**
```bash
cd /Users/lauramuresan/Downloads/TeacherPlatform
./mvnw clean compile
```

### 2. **Run the Application**
```bash
./mvnw spring-boot:run
```
 **Starts on:** http://localhost:8080

### 3. **Database Tables Created Automatically**
On first startup, Hibernate will create:
- `users`
- `schools`
- `course_categories`
- `courses`
- `course_sessions`
- `enrollments`
- `attendance`
- `certificates`
- `course_materials`
- `notifications`

---

##  Key Features Implemented

### Data Integrity
- Automatic timestamp tracking (createdAt, updatedAt)
- Unique constraints on critical fields
- Foreign key relationships
- Composite unique constraints (enrollment, attendance)

###  Query Capabilities
- **50+ custom query methods** across all repositories
- Support for filtering, searching, and complex queries
- Date range filtering
- Status-based filtering
- Count aggregations

###  Clean Architecture
- Generic repository pattern reduces boilerplate
- Clear separation of concerns
- Enum types for type-safe fields
- Lazy loading for performance
- Automatic Hibernate DDL management

###  Developer Friendly
- Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Clear entity relationships with proper annotations
- Comprehensive documentation
- Ready for service layer integration

---

##  Database Schema Preview

```sql
-- Key Tables
users (id PRIMARY KEY, email UNIQUE, role, school_id FK, ...)
schools (id PRIMARY KEY, name UNIQUE, county, ...)
courses (id PRIMARY KEY, title, category_id FK, trainer_id FK, ...)
course_sessions (id PRIMARY KEY, course_id FK, ...)
enrollments (id PRIMARY KEY, course_id+teacher_id UNIQUE CONSTRAINT, ...)
attendance (id PRIMARY KEY, session_id+enrollment_id UNIQUE, ...)
certificates (id PRIMARY KEY, enrollment_id FK, certificate_code UNIQUE, ...)
course_materials (id PRIMARY KEY, course_id FK, ...)
notifications (id PRIMARY KEY, user_id FK, ...)
```

---

## Entity Relationships

```
User ←→ School (Many-to-One)
User (FORMATOR) ←→ Courses (One-to-Many, as trainer)
User (PROFESOR) ←→ Enrollments (One-to-Many)
Course ←→ CourseCategory (Many-to-One)
Course ←→ CourseSessions (One-to-Many)
Course ←→ Enrollments (One-to-Many)
Course ←→ CourseMaterials (One-to-Many)
Enrollment ←→ Attendance (One-to-Many)
Enrollment ←→ Certificate (One-to-One)
CourseSession ←→ Attendance (One-to-Many)
User ←→ Notifications (One-to-Many)
```

## Support & Documentation

- **Architecture Details:** Read `BACKEND_ARCHITECTURE.md`


