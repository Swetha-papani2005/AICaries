<?php
require_once 'config.php';

$input   = json_decode(file_get_contents("php://input"), true);
$user_id = intval($input['user_id'] ?? 0);

if ($user_id <= 0) {
    sendResponse(false, "Invalid user ID");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id, overall_score, risk_level, result_type, created_at 
                        FROM results WHERE user_id = ? 
                        ORDER BY created_at DESC");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$results = [];
while ($row = $result->fetch_assoc()) {
    $results[] = $row;
}

sendResponse(true, "Results fetched", $results);
$conn->close();
?>