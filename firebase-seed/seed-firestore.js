/**
 * Firestore Seed Data Script
 *
 * Cách sử dụng:
 * 1. Tải serviceAccountKey.json từ Firebase Console:
 *    - Vào Project Settings → Service accounts → Generate new private key
 *    - Lưu file vào thư mục firebase-seed/ (cùng cấp với file này)
 *
 * 2. Cài dependencies:
 *    cd firebase-seed
 *    npm install firebase-admin
 *
 * 3. Chạy script:
 *    node seed-firestore.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seedData() {
    console.log('Seeding Firestore data...\n');

    // --- Hospitals ---
    const hospitals = {
        'hospital_1': { name: 'City Heart Hospital', image: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800', rating: 4.8, reviewCount: 450, specialties: 'Cardiology,Emergency,Surgery', distance: '1.2 km', priceRange: '$$$', address: '123 Medical Plaza, Downtown', phone: '+1 (555) 123-4567', operatingHours: '24/7', latitude: 37.7749, longitude: -122.4194 },
        'hospital_2': { name: "Children's Medical Center", image: 'https://images.unsplash.com/photo-1587351021759-3e566b6af7cc?w=800', rating: 4.9, reviewCount: 389, specialties: 'Pediatrics,Neonatology,Child Surgery', distance: '2.5 km', priceRange: '$$', address: '456 Kids Avenue, Northside', phone: '+1 (555) 234-5678', operatingHours: 'Mon-Sat: 8AM-8PM', latitude: 37.7849, longitude: -122.4094 },
        'hospital_3': { name: 'Advanced Diagnostic Center', image: 'https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=800', rating: 4.7, reviewCount: 267, specialties: 'Radiology,Laboratory,Imaging', distance: '0.8 km', priceRange: '$$', address: '789 Health Street, Midtown', phone: '+1 (555) 345-6789', operatingHours: 'Mon-Fri: 7AM-9PM', latitude: 37.7649, longitude: -122.4294 },
        'hospital_4': { name: "Women's Wellness Hospital", image: 'https://images.unsplash.com/photo-1516549655169-df83a0774514?w=800', rating: 4.8, reviewCount: 512, specialties: 'Obstetrics,Gynecology,Maternity', distance: '3.1 km', priceRange: '$$$', address: '321 Care Boulevard, Eastside', phone: '+1 (555) 456-7890', operatingHours: '24/7', latitude: 37.7549, longitude: -122.3994 },
    };

    console.log('Adding hospitals...');
    for (const [id, data] of Object.entries(hospitals)) {
        await db.collection('hospitals').doc(id).set(data);
    }
    console.log(`  ✓ ${Object.keys(hospitals).length} hospitals added\n`);

    // --- Doctors ---
    const doctors = {
        'doctor_1': { name: 'Dr. Sarah Johnson', specialization: 'Cardiologist', hospitalId: 'hospital_1', hospitalName: 'City Heart Hospital', rating: 4.9, reviewCount: 234, experience: 12, consultationFee: 150, image: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400', nextAvailable: 'Today, 3:00 PM', bio: 'Board certified cardiologist with expertise in preventive cardiology and heart disease management.', availableSlots: '09:00 AM,10:00 AM,11:00 AM,02:00 PM,03:00 PM,04:00 PM' },
        'doctor_2': { name: 'Dr. Michael Chen', specialization: 'Pediatrician', hospitalId: 'hospital_2', hospitalName: "Children's Medical Center", rating: 4.8, reviewCount: 189, experience: 8, consultationFee: 120, image: 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400', nextAvailable: 'Tomorrow, 10:00 AM', bio: 'Specialized in pediatric care with a gentle approach to treating children.', availableSlots: '10:00 AM,11:00 AM,01:00 PM,02:00 PM,03:00 PM' },
        'doctor_3': { name: 'Dr. Emily Rodriguez', specialization: 'Dermatologist', hospitalId: 'hospital_3', hospitalName: 'Advanced Diagnostic Center', rating: 4.7, reviewCount: 156, experience: 10, consultationFee: 130, image: 'https://images.unsplash.com/photo-1594824476967-48c8b964273f?w=400', nextAvailable: 'Mar 8, 9:00 AM', bio: 'Expert in medical and cosmetic dermatology treatments.', availableSlots: '09:00 AM,10:30 AM,02:00 PM,03:30 PM,04:30 PM' },
        'doctor_4': { name: 'Dr. James Wilson', specialization: 'Orthopedic', hospitalId: 'hospital_4', hospitalName: "Women's Wellness Hospital", rating: 4.9, reviewCount: 201, experience: 15, consultationFee: 180, image: 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400', nextAvailable: 'Today, 4:00 PM', bio: 'Specializes in sports medicine and joint replacement surgery.', availableSlots: '09:00 AM,11:00 AM,02:00 PM,04:00 PM' },
        'doctor_5': { name: 'Dr. Lisa Anderson', specialization: 'General Practitioner', hospitalId: 'hospital_1', hospitalName: 'City Heart Hospital', rating: 4.8, reviewCount: 312, experience: 14, consultationFee: 100, image: 'https://images.unsplash.com/photo-1638202993928-7267aad84c31?w=400', nextAvailable: 'Today, 2:00 PM', bio: 'Comprehensive primary care for patients of all ages.', availableSlots: '09:00 AM,10:00 AM,11:00 AM,02:00 PM,03:00 PM,04:00 PM,05:00 PM' },
    };

    console.log('Adding doctors...');
    for (const [id, data] of Object.entries(doctors)) {
        await db.collection('doctors').doc(id).set(data);
    }
    console.log(`  ✓ ${Object.keys(doctors).length} doctors added\n`);

    // --- Demo User ---
    // NOTE: You must first create this user in Firebase Auth (email: sarah.williams@email.com, password: password123)
    // Then replace USER_UID below with the actual UID from Firebase Auth
    const USER_UID = 'REPLACE_WITH_ACTUAL_UID';

    if (USER_UID !== 'REPLACE_WITH_ACTUAL_UID') {
        console.log('Adding user profile...');
        await db.collection('users').doc(USER_UID).set({
            uid: USER_UID,
            name: 'Sarah Williams',
            email: 'sarah.williams@email.com',
            phone: '+1 (555) 123-4567',
            address: 'New York, NY',
            patientId: 'PT-123456'
        });
        console.log('  ✓ 1 user added\n');

        // --- Appointments ---
        console.log('Adding appointments...');
        const appointments = [
            { appointmentId: 'APT001', userId: USER_UID, doctorId: 'doctor_1', doctorName: 'Dr. Sarah Johnson', doctorSpecialization: 'Cardiologist', doctorImage: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400', hospital: 'City Heart Hospital', date: '2026-03-08', time: '03:00 PM', status: 'upcoming', type: 'Specialist Consultation', symptoms: 'Chest pain, irregular heartbeat', qrCode: 'QR-APT001', queueNumber: 5, queueStatus: 'waiting' },
            { appointmentId: 'APT002', userId: USER_UID, doctorId: 'doctor_5', doctorName: 'Dr. Lisa Anderson', doctorSpecialization: 'General Practitioner', doctorImage: 'https://images.unsplash.com/photo-1638202993928-7267aad84c31?w=400', hospital: 'Family Health Clinic', date: '2026-03-10', time: '10:00 AM', status: 'upcoming', type: 'General Checkup', symptoms: null, qrCode: 'QR-APT002', queueNumber: null, queueStatus: null },
            { appointmentId: 'APT003', userId: USER_UID, doctorId: 'doctor_2', doctorName: 'Dr. Michael Chen', doctorSpecialization: 'Pediatrician', doctorImage: 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400', hospital: "Children's Medical Center", date: '2026-02-28', time: '02:00 PM', status: 'completed', type: 'Vaccination', symptoms: null, qrCode: null, queueNumber: null, queueStatus: null },
            { appointmentId: 'APT004', userId: USER_UID, doctorId: 'doctor_3', doctorName: 'Dr. Emily Rodriguez', doctorSpecialization: 'Dermatologist', doctorImage: 'https://images.unsplash.com/photo-1594824476967-48c8b964273f?w=400', hospital: 'Skin Care Clinic', date: '2026-03-01', time: '11:00 AM', status: 'cancelled', type: 'Dermatology Consultation', symptoms: null, qrCode: null, queueNumber: null, queueStatus: null },
        ];
        for (const apt of appointments) { await db.collection('appointments').add(apt); }
        console.log(`  ✓ ${appointments.length} appointments added\n`);

        // --- Medical Records ---
        console.log('Adding medical records...');
        const records = [
            { recordId: 'REC001', userId: USER_UID, date: '2026-02-28', type: 'appointment', title: 'Pediatric Checkup', doctor: 'Dr. Michael Chen', hospital: "Children's Medical Center", details: 'Regular health checkup. Patient is healthy. Received MMR vaccination.', attachments: null },
            { recordId: 'REC002', userId: USER_UID, date: '2026-02-25', type: 'prescription', title: 'Antibiotic Prescription', doctor: 'Dr. Lisa Anderson', hospital: 'Family Health Clinic', details: 'Amoxicillin 500mg - Take 3 times daily for 7 days', attachments: null },
            { recordId: 'REC003', userId: USER_UID, date: '2026-02-20', type: 'lab-result', title: 'Blood Test Results', doctor: null, hospital: 'Advanced Diagnostic Center', details: 'Complete Blood Count - All values within normal range', attachments: 'blood-test.pdf' },
            { recordId: 'REC004', userId: USER_UID, date: '2026-02-15', type: 'note', title: 'Follow-up Required', doctor: 'Dr. Sarah Johnson', hospital: 'City Heart Hospital', details: 'Patient advised to return in 3 months for cardiac follow-up', attachments: null },
        ];
        for (const rec of records) { await db.collection('medical_records').add(rec); }
        console.log(`  ✓ ${records.length} medical records added\n`);

        // --- Notifications ---
        console.log('Adding notifications...');
        const notifications = [
            { notificationId: 'NOT001', userId: USER_UID, type: 'reminder', title: 'Appointment Reminder', message: 'You have an appointment with Dr. Sarah Johnson tomorrow at 3:00 PM', time: '2 hours ago', isRead: false },
            { notificationId: 'NOT002', userId: USER_UID, type: 'payment', title: 'Payment Confirmed', message: 'Your payment of $150 has been successfully processed', time: '1 day ago', isRead: false },
            { notificationId: 'NOT003', userId: USER_UID, type: 'schedule', title: 'Schedule Changed', message: "Dr. Michael Chen's appointment has been rescheduled to Mar 10", time: '2 days ago', isRead: true },
            { notificationId: 'NOT004', userId: USER_UID, type: 'medication', title: 'Medication Reminder', message: 'Time to take your medication - Amoxicillin 500mg', time: '3 days ago', isRead: true },
            { notificationId: 'NOT005', userId: USER_UID, type: 'follow-up', title: 'Follow-up Appointment', message: 'Schedule your follow-up appointment with Dr. Sarah Johnson', time: '5 days ago', isRead: true },
        ];
        for (const notif of notifications) { await db.collection('notifications').add(notif); }
        console.log(`  ✓ ${notifications.length} notifications added\n`);

        // --- Insurance ---
        console.log('Adding insurance...');
        await db.collection('insurance').add({
            userId: USER_UID, provider: 'HealthCare Plus', policyNumber: 'HC-123456789',
            coverage: 'Family Plan', validUntil: 'Dec 2026', status: 'active'
        });
        console.log('  ✓ 1 insurance record added\n');
    } else {
        console.log('\n⚠ Skipped user-related data (appointments, records, notifications, insurance).');
        console.log('  To add user data:');
        console.log('  1. Create user in Firebase Auth: sarah.williams@email.com / password123');
        console.log('  2. Copy the UID from Firebase Auth console');
        console.log('  3. Replace REPLACE_WITH_ACTUAL_UID in this script');
        console.log('  4. Run the script again\n');
    }

    console.log('✅ Seed data complete!');
    process.exit(0);
}

seedData().catch(err => {
    console.error('Error seeding data:', err);
    process.exit(1);
});
