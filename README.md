# 🦷 AICaries — Smart AI Dental Diagnostic Platform

Welcome to **AICaries**, a comprehensive digital dental diagnostic platform designed to detect teeth caries, evaluate oral hygiene risks, provide dental advice, and support interactive AI consultations. 

This repository contains the complete cross-platform codebase, including a responsive web app, native Android app, backend REST APIs, a deep learning AI model, and an E2E QA automation test suite.

---

## 🔗 Live Deployments

* **💻 Live Web Application**: [https://swetha-papani2005.github.io/AICaries/](https://swetha-papani2005.github.io/AICaries/)
* **⚡ Live Backend API Endpoint**: `https://app-17b106e1-724c-43db-bcc5-196c5d9e4f65.cleverapps.io/api/`
* **🗄️ Live Database Host**: Clever Cloud MySQL (`aicaries-db`)

---

## 🚀 Key Features

* **AI-Powered Caries Scanning**: Upload or capture teeth images to analyze cavity severity levels (Low, Moderate, High) with confidence metrics.
* **Smart Dental Questionnaire**: Complete risk assessment checklists for personalized dental diagnostics.
* **Dental AI Chatbot**: Chat with an interactive assistant for oral hygiene tips, cavity symptoms, and dental recommendations (powered by Groq Llama-3).
* **Real-time Password Reset**: Real, dynamically generated password recovery emails sent straight to your inbox (configured via Gmail SMTP).
* **PDF Report Generation**: Download/print diagnostic summaries and reports directly from the app.
* **Cross-Device Database Synchronization**: Log in simultaneously across phones and laptops with real-time cloud data sync.

---

## 📂 Repository Structure

The project is structured into modular component directories:

```
├── web/                 # React.js web frontend application
├── api/                 # PHP backend APIs (deployed to Clever Cloud)
├── android/             # Kotlin & Jetpack Compose Android source code
├── ai_model/            # Python Flask server for Deep Learning predictions
├── automated_tests/     # Selenium & Python E2E automation test suite
├── Dockerfile           # Docker configuration for Apache/PHP container deployment
└── README.md            # Project overview & documentation
```

---

## 🛠️ Technology Stack

* **Frontend**: React (Vite), JavaScript, Vanilla CSS, Lucide Icons
* **Backend API**: PHP (Apache), cURL integrations
* **Database**: MySQL (hosted on Clever Cloud)
* **Mobile App**: Kotlin, Android Jetpack Compose
* **AI Model Server**: Python, Flask, OpenCV / PyTorch
* **CI/CD & Automation**: GitHub Actions, Python, Selenium WebDriver

---

## 💻 Local Installation & Setup

### 1. Database Setup
1. Open XAMPP and start Apache and MySQL (configured to port `3307` or standard `3306`).
2. Import the database schema file **`schema.sql`** (located inside the **`database/`** directory of this repository) into phpMyAdmin at `http://localhost/phpmyadmin`.

### 2. Run the Backend API (XAMPP)
1. Place the repository folder in your local XAMPP server path (e.g., `C:\xampp\htdocs\aicaries`).
2. Ensure your SMTP email credentials are set up inside `api/mail_config.json`.

### 3. Run the AI Model Server
1. Navigate to the `ai_model` directory:
   ```bash
   cd ai_model
   ```
2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Start the Flask application:
   ```bash
   python predict_api.py
   ```

### 4. Run the React Web App
1. Navigate to the `web` directory:
   ```bash
   cd web
   ```
2. Install Node dependencies:
   ```bash
   npm install
   ```
3. Start the Vite dev server:
   ```bash
   npm run dev
   ```

---

## 🧪 E2E Automation Testing

The E2E testing suite is designed to validate web and Android functionalities. It generates comprehensive Excel reports containing exactly **1,200 test cases** across UI and functional testing.

1. Navigate to `automated_tests` folder:
   ```bash
   cd automated_tests
   ```
2. Run the automation suite:
   ```bash
   python generate_all_reports.py
   ```
3. **Reports Generated**:
   * `web_ui_test_report.xlsx` (300 cases)
   * `web_functional_test_report.xlsx` (300 cases)
   * `android_ui_test_report.xlsx` (300 cases)
   * `android_functional_test_report.xlsx` (300 cases)
