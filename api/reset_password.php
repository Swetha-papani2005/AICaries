<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Method not allowed");
}

$input = json_decode(file_get_contents("php://input"), true);
$email = trim($input['email'] ?? '');
$token = trim($input['token'] ?? '');
$newPassword = $input['new_password'] ?? '';

if (empty($email) || empty($token) || empty($newPassword)) {
    sendResponse(false, "All fields are required");
}

if (strlen($newPassword) < 6) {
    sendResponse(false, "Password must be at least 6 characters");
}

$conn = getConnection();

// Verify token
$stmt = $conn->prepare("SELECT id FROM password_resets WHERE email = ? AND token = ?");
$stmt->bind_param("ss", $email, $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, "Invalid or expired reset token");
}

// Hash password
$hashedPassword = password_hash($newPassword, PASSWORD_BCRYPT);

// Update user password
$stmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
$stmt->bind_param("ss", $hashedPassword, $email);
$stmt->execute();

// Delete reset token
$stmt = $conn->prepare("DELETE FROM password_resets WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();

sendResponse(true, "Password has been reset successfully. You can now login with your new password.");
$conn->close();
?>
