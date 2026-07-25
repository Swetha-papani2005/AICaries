<?php
require_once 'config.php';
$conn = getConnection();

$url = $_GET['url'] ?? '';
if (empty($url)) {
    sendResponse(false, "URL parameter is required");
}

// Ensure settings table exists
$conn->query("
    CREATE TABLE IF NOT EXISTS settings (
        key_name VARCHAR(50) PRIMARY KEY,
        key_value TEXT
    )
");

// Insert or update the active tunnel URL
$stmt = $conn->prepare("
    INSERT INTO settings (key_name, key_value)
    VALUES ('ai_model_url', ?)
    ON DUPLICATE KEY UPDATE key_value = ?
");

$predict_url = rtrim($url, '/') . '/predict';
$stmt->bind_param("ss", $predict_url, $predict_url);

if ($stmt->execute()) {
    sendResponse(true, "Tunnel URL updated in database successfully", ["url" => $predict_url]);
} else {
    sendResponse(false, "Failed to update tunnel URL: " . $conn->error);
}

$conn->close();
?>
