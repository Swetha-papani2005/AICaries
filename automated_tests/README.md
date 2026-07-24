# AICaries Automation E2E Testing Guide

This directory contains the testing architecture for the **AICaries Dental Care Portal** (React Web App + Kotlin Android App).

We have generated **6 Excel reports** containing **300 test cases each** (1,800 test cases total) to validate the system's end-to-end user flows, user interfaces, constraints, functionalities, vulnerabilities, and load performance thresholds.

---

## 📂 Testing Artifacts Generated

The following files are located in this directory (`C:\XAMP\htdocs\aicaries\automated_tests\`):
1. **[web_ui_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/web_ui_test_report.xlsx)**: 300 test cases validating font sizes, WCAG contrast levels, spacing, and layout responsiveness across 15 web screens.
2. **[web_functional_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/web_functional_test_report.xlsx)**: 300 test cases validating form submissions, validation toasts, database inserts, and chatbot integrations in the browser.
3. **[android_ui_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/android_ui_test_report.xlsx)**: 300 test cases validating XML constraints, margin spacings, card elevations, logo vector scaling, and orientation adaptations.
4. **[android_functional_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/android_functional_test_report.xlsx)**: 300 test cases validating SharedPreferences session tokens, SQLite queries, multipart image uploads, cURL routing, and native printing via PrintManager.
5. **[vulnerability_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/vulnerability_test_report.xlsx)**: 300 test cases validating SQL Injection, XSS defenses, CSRF tokens, session fixation blocks, log injection protection, and directory traversal safeguards.
6. **[load_test_report.xlsx](file:///C:/XAMP/htdocs/aicaries/automated_tests/load_test_report.xlsx)**: 300 test cases validating system behavior under 10 to 500 concurrent virtual users, ramp-up/ramp-down, database pool exhaustion checks, and chatbot peak load thresholds.

---

## 🌐 Web E2E Testing using Selenium

To run automated browser tests locally using Selenium WebDriver:

### 1. Prerequisites
Install Node.js on your machine, then install Selenium WebDriver and Mocha in the web folder:
```bash
cd C:\XAMP\htdocs\aicaries\web
npm install selenium-webdriver mocha chai --save-dev
```

### 2. Run Chrome WebDriver
Ensure you have Google Chrome installed. Selenium will automatically download and start the matching ChromeDriver for your browser version.

### 3. Execution Command
Create a file named `selenium-tests/tests/login.test.js` and execute:
```bash
npx mocha selenium-tests/tests/*.test.js --timeout 30000
```

---

## 📱 Android E2E Testing using Appium

To automate user interactions (clicks, camera usage, assessments) on your Android device:

### 1. Prerequisites
Install Appium globally using npm:
```bash
npm install -g appium
appium driver install uiautomator2
```

### 2. Configure Capabilities
In your Node.js automation script, set the following capabilities to point to your compiled debug APK:
```javascript
const capabilities = {
    platformName: 'Android',
    'appium:automationName': 'UiAutomator2',
    'appium:deviceName': 'Android Emulator',
    'appium:app': 'C:/Users/DELL/Downloads/AICaries/app/build/outputs/apk/debug/app-debug.apk',
    'appium:newCommandTimeout': 3600
};
```

### 3. Execution
Start the Appium server:
```bash
appium
```
Run your WebdriverIO test runner:
```bash
node appium-tests/run_mobile_test.js
```

---

## 🚀 CI/CD Automation (GitHub Actions)

To automatically execute Selenium validation tests every time you push code to GitHub, create the file `.github/workflows/selenium-tests.yml` in your repository:

```yaml
name: Selenium E2E Web Tests
on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Install Node.js
        uses: actions/setup-node@v3
        with:
          node-size: '18'

      - name: Install dependencies
        run: |
          cd web
          npm install

      - name: Run Headless Chrome Selenium Tests
        run: |
          cd web
          npx mocha selenium-tests/tests/*.test.js --timeout 30000
```
