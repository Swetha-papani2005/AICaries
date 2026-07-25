<?php
require_once 'config.php';
$conn = getConnection();

// Check if column exists first (MySQL 8.0 compatible)
$check = $conn->query("SHOW COLUMNS FROM `results` LIKE 'image'");
if ($check && $check->num_rows == 0) {
    $sql = "ALTER TABLE `results` ADD COLUMN `image` VARCHAR(255) DEFAULT NULL AFTER `created_at`";
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["success" => true, "message" => "Column 'image' added successfully!"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error adding column: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => true, "message" => "Column 'image' already exists!"]);
}
?>
