<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Method not allowed");
}

$input = json_decode(file_get_contents("php://input"), true);

$name     = trim($input['name'] ?? '');
$email    = trim($input['email'] ?? '');
$password = $input['password'] ?? '';

if (empty($name) || empty($email) || empty($password)) {
    sendResponse(false, "All fields are required");
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse(false, "Invalid email address");
}

if (strlen($password) < 6) {
    sendResponse(false, "Password must be at least 6 characters");
}

$conn = getConnection();

// Check if email already exists
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    sendResponse(false, "Email already registered");
}

// Hash password
$hashedPassword = password_hash($password, PASSWORD_BCRYPT);

// Insert user
$stmt = $conn->prepare("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $name, $email, $hashedPassword);

if ($stmt->execute()) {
    $userId = $conn->insert_id;
    $token = base64_encode($userId . ':' . time() . ':' . bin2hex(random_bytes(16)));
    sendResponse(true, "Account created successfully", [
        "user_id" => $userId,
        "name"    => $name,
        "email"   => $email,
        "token"   => $token
    ]);
} else {
    sendResponse(false, "Registration failed. Please try again.");
}

$conn->close();
?>