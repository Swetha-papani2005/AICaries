<?php
require_once 'config.php';

$input   = json_decode(file_get_contents("php://input"), true);
$user_id = intval($input['user_id'] ?? 0);

if ($user_id <= 0) {
    sendResponse(false, "Invalid user ID");
}

$conn = getConnection();

$stmt = $conn->prepare("DELETE FROM results WHERE user_id = ?");
$stmt->bind_param("i", $user_id);

if ($stmt->execute()) {
    sendResponse(true, "All results deleted");
} else {
    sendResponse(false, "Delete failed");
}

$conn->close();
?>