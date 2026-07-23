<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Method not allowed");
}

$user_id = intval($_POST['user_id'] ?? 0);

if (!isset($_FILES['image'])) {
    sendResponse(false, "No image uploaded");
}

$image = $_FILES['image'];

$allowed_types = ['image/jpeg', 'image/png', 'image/jpg'];

if (!in_array($image['type'], $allowed_types)) {
    sendResponse(false, "Only JPG/PNG images allowed");
}

// FLASK AI API URL

$ai_api_url = "http://127.0.0.1:5000/predict";

// SAVE IMAGE FILE PERMANENTLY
$uploads_dir = __DIR__ . '/uploads';
if (!file_exists($uploads_dir)) {
    mkdir($uploads_dir, 0777, true);
}
$image_filename = time() . '_' . basename($image['name']);
$image_path = 'uploads/' . $image_filename;
$destination = $uploads_dir . '/' . $image_filename;

if (move_uploaded_file($image['tmp_name'], $destination)) {
    $cfile = new CURLFile($destination, $image['type'], $image['name']);
} else {
    $cfile = new CURLFile($image['tmp_name'], $image['type'], $image['name']);
    $image_path = '';
}

$curl = curl_init();

curl_setopt_array($curl, [

    CURLOPT_URL => $ai_api_url,

    CURLOPT_POST => true,

    CURLOPT_POSTFIELDS => [
        'image' => $cfile
    ],

    CURLOPT_RETURNTRANSFER => true,

    CURLOPT_TIMEOUT => 120,

    CURLOPT_CONNECTTIMEOUT => 120
]);

// EXECUTE REQUEST

$response = curl_exec($curl);

// HANDLE CURL ERROR

if ($response === false) {

    $error = curl_error($curl);

    curl_close($curl);

    sendResponse(false, "CURL ERROR: " . $error);
}

// CHECK HTTP STATUS

$http_code = curl_getinfo($curl, CURLINFO_HTTP_CODE);

curl_close($curl);

if ($http_code != 200) {

    sendResponse(
        false,
        "Flask returned HTTP code: " . $http_code
    );
}

// EMPTY RESPONSE

if (empty($response)) {

    sendResponse(
        false,
        "Empty response from AI. Make sure Flask is running!"
    );
}

// DECODE AI RESPONSE

$ai_result = json_decode($response, true);

// VALIDATE AI RESPONSE

if (!$ai_result || !$ai_result['success']) {

    sendResponse(
        false,
        "AI analysis failed: " . $response
    );
}

// EXTRACT AI DATA

$overall_score   = intval($ai_result['risk_score']);

$risk_level      = $ai_result['risk_level'];

$confidence      = $ai_result['confidence'];

$recommendations = $ai_result['recommendations'];

$prediction      = $ai_result['prediction'];

// SAVE TO DATABASE

$conn = getConnection();

$stmt = $conn->prepare("
    INSERT INTO results
    (
        user_id,
        overall_score,
        risk_level,
        result_type,
        answers
    )
    VALUES (?, ?, ?, ?, ?)
");

$answers_json = json_encode([

    'type'       => 'ai_scan',

    'confidence' => $confidence,

    'prediction' => $prediction,

    'image'      => $image_path
]);

$result_type = 'scan';

$stmt->bind_param(
    "iisss",
    $user_id,
    $overall_score,
    $risk_level,
    $result_type,
    $answers_json
);

$stmt->execute();

$result_id = $conn->insert_id;

// SAVE RECOMMENDATIONS

foreach ($recommendations as $rec) {

    $recStmt = $conn->prepare(
        "INSERT INTO recommendations
        (result_id, recommendation)
        VALUES (?, ?)"
    );

    $recStmt->bind_param("is", $result_id, $rec);

    $recStmt->execute();
}

// SUCCESS RESPONSE

sendResponse(true, "Analysis complete", [

    'result_id'       => $result_id,

    'overall_score'   => $overall_score,

    'risk_level'      => $risk_level,

    'confidence'      => $confidence,

    'prediction'      => $prediction,

    'recommendations' => $recommendations,

    'image_path'      => $image_path
]);

$conn->close();

?>