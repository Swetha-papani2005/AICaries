<?php
require_once 'config.php';

$input = json_decode(file_get_contents("php://input"), true);

$result_id = intval($input['result_id'] ?? 0);
$user_id   = intval($input['user_id'] ?? 0);

if ($result_id <= 0 && $user_id <= 0) {
    sendResponse(false, "Invalid parameters. Provide result_id or user_id.");
}

$conn = getConnection();

if ($result_id > 0) {
    // 1. Delete recommendations first to avoid constraint issues
    $recStmt = $conn->prepare("DELETE FROM recommendations WHERE result_id = ?");
    $recStmt->bind_param("i", $result_id);
    $recStmt->execute();
    $recStmt->close();

    // 2. Delete the result row itself
    $stmt = $conn->prepare("DELETE FROM results WHERE id = ?");
    $stmt->bind_param("i", $result_id);
    
    if ($stmt->execute()) {
        sendResponse(true, "Result deleted successfully");
    } else {
        sendResponse(false, "Failed to delete result");
    }
    $stmt->close();
} else if ($user_id > 0) {
    // 1. Delete recommendations for all of this user's results
    $recStmt = $conn->prepare("DELETE FROM recommendations WHERE result_id IN (SELECT id FROM results WHERE user_id = ?)");
    $recStmt->bind_param("i", $user_id);
    $recStmt->execute();
    $recStmt->close();

    // 2. Delete all results for this user
    $stmt = $conn->prepare("DELETE FROM results WHERE user_id = ?");
    $stmt->bind_param("i", $user_id);
    
    if ($stmt->execute()) {
        sendResponse(true, "All results cleared successfully");
    } else {
        sendResponse(false, "Failed to clear results");
    }
    $stmt->close();
}

$conn->close();
?>