import { Routes, Route, Navigate } from 'react-router-dom'
import LoginRegister from '../pages/LoginRegister'
import Dashboard from '../pages/teacher/Dashboard'
import BrowseCourses from '../pages/teacher/BrowseCourses'
import MyEnrollments from '../pages/teacher/MyEnrollments'
import MyCertificates from '../pages/teacher/MyCertificates'
import Notifications from '../pages/teacher/Notifications'
import TrainerDashboard from '../pages/trainer/TrainerDashboard'
import MyCourses from '../pages/trainer/MyCourses'
import Participants from '../pages/trainer/Participants'
import AttendanceCatalog from '../pages/trainer/AttendanceCatalog'
import ProposeCourse from '../pages/trainer/ProposeCourse'

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" />} />
            <Route path="/login" element={<LoginRegister />} />

            {/* Teacher routes */}
            <Route path="/teacher" element={<Dashboard />} />
            <Route path="/teacher/courses" element={<BrowseCourses />} />
            <Route path="/teacher/enrollments" element={<MyEnrollments />} />
            <Route path="/teacher/certificates" element={<MyCertificates />} />
            <Route path="/teacher/notifications" element={<Notifications />} />

            {/* Trainer routes */}
            <Route path="/trainer" element={<TrainerDashboard />} />
            <Route path="/trainer/courses" element={<MyCourses />} />
            <Route path="/trainer/participants" element={<Participants />} />
            <Route path="/trainer/attendance" element={<AttendanceCatalog />} />
            <Route path="/trainer/propose" element={<ProposeCourse />} />
        </Routes>
    )
}