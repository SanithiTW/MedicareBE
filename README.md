   🩺 Medicare – Backend API

📌 Project Overview
**Medicare** is a healthcare management system that allows patients to store, manage, and access their medical information digitally.

This repository contains the **backend REST API services** of the Medicare system. The backend manages authentication, patient data, medical reports, and secure communication with the database.

The API ensures that all data operations are performed securely and efficiently.

---

 🚀 Key Features

 🔐 Authentication System
- User registration and login
- Google authentication support
- Secure user validation

 👤 Profile Management
- Create and update patient profiles
- Store personal health information
- Retrieve profile data through APIs

 👨‍👩‍👧 Family Member Management
- Add family members to an account
- Store health profiles for dependents
- Retrieve family health information

 📄 Medical Report Management
- Upload medical reports
- Retrieve stored reports
- Secure storage of medical documents

 🔒 Secure API Handling
- Request validation
- Secure database communication
- Error handling and response management



 🛠 Technologies Used

- **Node.js**
- **Express.js**
- **Firebase Realtime Database**
- **Firebase Authentication**
- **REST API**
- **JavaScript**


 📂 Project Structure

src/
│
├── controllers/ # Business logic
├── routes/ # API endpoints
├── services/ # Database operations
├── middlewares/ # Authentication and validation
├── config/ # Project configuration
└── server.js # Application entry point


