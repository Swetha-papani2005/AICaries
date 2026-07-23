<?php
require_once 'config.php';

$input = json_decode(file_get_contents("php://input"), true);
$email = trim($input['email'] ?? '');

if (empty($email)) {
    sendResponse(false, "Email is required");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, "No account found with this email");
}

// Generate reset token
$token = bin2hex(random_bytes(32));

// Save token
$stmt = $conn->prepare("INSERT INTO password_resets (email, token) VALUES (?, ?)");
$stmt->bind_param("ss", $email, $token);
$stmt->execute();

// In real app: send email with reset link
// For local testing: just return success
sendResponse(true, "Password reset link sent to your email", [
    "reset_token" => $token  // Remove this in production
]);

$conn->close();
?>