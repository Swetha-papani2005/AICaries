<?php
require_once 'config.php';

$input     = json_decode(file_get_contents("php://input"), true);
$result_id = intval($input['result_id'] ?? 0);

if ($result_id <= 0) {
    sendResponse(false, "Invalid result ID");
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT * FROM results WHERE id = ?");
$stmt->bind_param("i", $result_id);
$stmt->execute();
$result = $stmt->get_result()->fetch_assoc();

if (!$result) {
    sendResponse(false, "Result not found");
}

// Get recommendations
$recStmt = $conn->prepare("SELECT recommendation FROM recommendations WHERE result_id = ?");
$recStmt->bind_param("i", $result_id);
$recStmt->execute();
$recResult = $recStmt->get_result();

$recommendations = [];
while ($row = $recResult->fetch_assoc()) {
    $recommendations[] = $row['recommendation'];
}

$result['recommendations'] = $recommendations;
$result['answers'] = json_decode($result['answers'], true);

sendResponse(true, "Result found", $result);
$conn->close();
?>