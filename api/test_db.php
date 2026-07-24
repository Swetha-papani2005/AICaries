<?php
require_once 'config.php';
$conn = getConnection();
echo json_encode(["success" => true, "message" => "Database connected successfully!"]);
?>
