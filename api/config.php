<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Database Configuration with Cloud Environment fallback
$host = getenv('MYSQL_ADDON_HOST') ?: 'localhost';
$user = getenv('MYSQL_ADDON_USER') ?: 'root';
$pass = getenv('MYSQL_ADDON_PASSWORD') ?: '';
$db = getenv('MYSQL_ADDON_DB') ?: 'aicaries';
$port = intval(getenv('MYSQL_ADDON_PORT') ?: 3307);

// General environment variables fallback
if (getenv('DB_HOST')) {
    $host = getenv('DB_HOST');
    $user = getenv('DB_USER') ?: 'root';
    $pass = getenv('DB_PASS') ?: '';
    $db = getenv('DB_NAME') ?: 'aicaries';
    $port = intval(getenv('DB_PORT') ?: 3306);
}

define('DB_HOST', $host);
define('DB_USER', $user);
define('DB_PASS', $pass);
define('DB_NAME', $db);
define('DB_PORT', $port);

function getConnection() {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME, DB_PORT);
    if ($conn->connect_error) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Database connection failed"]);
        exit();
    }
    $conn->set_charset("utf8");
    return $conn;
}

function sendResponse($success, $message, $data = null) {
    $response = ["success" => $success, "message" => $message];
    if ($data !== null) {
        $response["data"] = $data;
    }
    echo json_encode($response);
    exit();
}
?>