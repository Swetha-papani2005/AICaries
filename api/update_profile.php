<?php
require_once 'config.php';

$input    = json_decode(file_get_contents("php://input"), true);
$user_id  = intval($input['user_id'] ?? 0);
$name     = trim($input['name'] ?? '');
$phone    = trim($input['phone'] ?? '');
$language = trim($input['language'] ?? 'en');

if ($user_id <= 0) {
    sendResponse(false, "Invalid user ID");
}

$conn = getConnection();

$stmt = $conn->prepare("UPDATE users SET name = ?, phone = ?, language = ? WHERE id = ?");
$stmt->bind_param("sssi", $name, $phone, $language, $user_id);

if ($stmt->execute()) {
    sendResponse(true, "Profile updated successfully");
} else {
    sendResponse(false, "Update failed");
}

$conn->close();
?>