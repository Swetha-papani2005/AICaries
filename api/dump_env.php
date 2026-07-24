<?php
echo json_encode([
    "MYSQL_ADDON_HOST" => getenv('MYSQL_ADDON_HOST'),
    "MYSQL_ADDON_DB" => getenv('MYSQL_ADDON_DB'),
    "MYSQL_ADDON_USER" => getenv('MYSQL_ADDON_USER'),
    "MYSQL_ADDON_PORT" => getenv('MYSQL_ADDON_PORT'),
    "HAS_PASSWORD" => getenv('MYSQL_ADDON_PASSWORD') ? "YES" : "NO"
]);
?>
