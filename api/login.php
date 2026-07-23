<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Method not allowed");
}

$input    = json_decode(file_get_contents("php://input"), true);
$email    = trim($input['email'] ?? '');
$password = $input['password'] ?? '';

if (empty($email) || empty($password)) {
    sendResponse(false, "Email and password are required");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id, name, email, password, phone, language FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, "No account found with this email");
}

$user = $result->fetch_assoc();

if (!password_verify($password, $user['password'])) {
    sendResponse(false, "Incorrect password");
}

// Generate simple token
$token = base64_encode($user['id'] . ':' . time() . ':' . bin2hex(random_bytes(16)));

sendResponse(true, "Login successful", [
    "user_id"  => $user['id'],
    "name"     => $user['name'],
    "email"    => $user['email'],
    "phone"    => $user['phone'],
    "language" => $user['language'],
    "token"    => $token
]);

$conn->close();
?>