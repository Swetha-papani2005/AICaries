<?php
require_once 'config.php';

$input   = json_decode(file_get_contents("php://input"), true);
$user_id = intval($input['user_id'] ?? 0);

if ($user_id <= 0) {
    sendResponse(false, "Invalid user ID");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id, name, email, phone, language, created_at FROM users WHERE id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, "User not found");
}

$user = $result->fetch_assoc();
sendResponse(true, "User found", $user);

$conn->close();
?>