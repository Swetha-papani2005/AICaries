<?php
require_once 'config.php';

$input = json_decode(file_get_contents("php://input"), true);

$user_id              = intval($input['user_id'] ?? 0);
$overall_score        = intval($input['overall_score'] ?? 0);
$risk_level           = $input['risk_level'] ?? 'Low';
$demographic_score    = intval($input['demographic_score'] ?? 0);
$socioeconomic_score  = intval($input['socioeconomic_score'] ?? 0);
$dietary_score        = intval($input['dietary_score'] ?? 0);
$hygiene_score        = intval($input['hygiene_score'] ?? 0);
$dental_history_score = intval($input['dental_history_score'] ?? 0);
$answers              = json_encode($input['answers'] ?? []);
$result_type          = $input['result_type'] ?? 'assessment';
$recommendations      = $input['recommendations'] ?? [];

if ($user_id <= 0) {
    sendResponse(false, "Invalid user ID");
}

$conn = getConnection();

$stmt = $conn->prepare("INSERT INTO results 
    (user_id, overall_score, risk_level, demographic_score, socioeconomic_score, 
     dietary_score, hygiene_score, dental_history_score, answers, result_type) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

$stmt->bind_param("iisiiiiiss",
    $user_id, $overall_score, $risk_level,
    $demographic_score, $socioeconomic_score,
    $dietary_score, $hygiene_score, $dental_history_score,
    $answers, $result_type
);

if ($stmt->execute()) {
    $result_id = $conn->insert_id;

    // Save recommendations
    foreach ($recommendations as $rec) {
        $recStmt = $conn->prepare("INSERT INTO recommendations (result_id, recommendation) VALUES (?, ?)");
        $recStmt->bind_param("is", $result_id, $rec);
        $recStmt->execute();
    }

    sendResponse(true, "Result saved", ["result_id" => $result_id]);
} else {
    sendResponse(false, "Failed to save result");
}

$conn->close();
?>