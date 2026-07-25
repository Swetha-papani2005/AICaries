<?php
require_once 'config.php';
$conn = getConnection();
$sql = "ALTER TABLE results ADD COLUMN IF NOT EXISTS image VARCHAR(255) DEFAULT NULL AFTER created_at";
if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Column 'image' added successfully!"]);
} else {
    echo json_encode(["success" => false, "message" => "Error adding column: " . $conn->error]);
}
?>
