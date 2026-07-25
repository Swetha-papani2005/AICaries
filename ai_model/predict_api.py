from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
from PIL import Image, ImageFilter
import io
import os

app = Flask(__name__)

MODEL_PATH_XAMP = 'C:/XAMP/htdocs/aicaries/ai_model/model/dental_model.h5'
MODEL_PATH_XAMPP = 'C:/XAMPP/htdocs/aicaries/ai_model/model/dental_model.h5'
MODEL_PATH = MODEL_PATH_XAMP if os.path.exists(MODEL_PATH_XAMP) else MODEL_PATH_XAMPP
model = None


def load_model():
    global model

    if os.path.exists(MODEL_PATH):
        model = tf.keras.models.load_model(MODEL_PATH)
        print("Model loaded successfully!")
    else:
        print("Model not found!")


def validate_image(image_bytes):
    try:
        img = Image.open(io.BytesIO(image_bytes))
        img = img.convert('RGB')
        
        # 1. Color heuristic to check if it's a valid teeth/mouth image
        arr = np.array(img)
        h, w, c = arr.shape
        r_mean = arr[:,:,0].mean()
        g_mean = arr[:,:,1].mean()
        b_mean = arr[:,:,2].mean()
        
        # Count red/pink pixels (lips/gums) and white/yellow pixels (teeth)
        gums_pixels = np.sum((arr[:,:,0] > arr[:,:,1] + 25) & (arr[:,:,0] > arr[:,:,2] + 25))
        teeth_pixels = np.sum((arr[:,:,0] > 130) & (arr[:,:,1] > 120) & (arr[:,:,0] - arr[:,:,1] < 30) & (arr[:,:,1] - arr[:,:,2] < 50))
        
        total_pixels = h * w
        gums_ratio = gums_pixels / total_pixels
        teeth_ratio = teeth_pixels / total_pixels
        
        print("\n--- Image Validation Debug ---")
        print(f"Red Mean: {r_mean:.2f}, Green Mean: {g_mean:.2f}, Blue Mean: {b_mean:.2f}")
        print(f"Gums Ratio: {gums_ratio:.4f}, Teeth Ratio: {teeth_ratio:.4f}")
        
        # If gums and teeth features are almost non-existent, it's not a mouth image
        if gums_ratio < 0.005 and teeth_ratio < 0.01:
            print("Validation: Failed (Not a teeth image)")
            return False, "Invalid Image: This is not an image of teeth."
            
        # 2. Check if the image is too blurry using edge variance (high-pass filter)
        gray = img.convert('L')
        edges = gray.filter(ImageFilter.FIND_EDGES)
        edge_arr = np.array(edges)
        variance = edge_arr.var()
        print(f"Edge Variance (Sharpness): {variance:.2f}")
        print("------------------------------")
        
        if variance < 3.5:
            print("Validation: Failed (Blurry image)")
            return False, "Image not clear. Please capture a sharper photo."
            
        return True, "Success"
    except Exception as e:
        return False, f"Invalid image format: {str(e)}"


def preprocess_image(image_bytes):

    img = Image.open(io.BytesIO(image_bytes))

    img = img.convert('RGB')

    img = img.resize((224, 224))

    img_array = np.array(img).astype("float32") / 255.0

    img_array = np.expand_dims(img_array, axis=0)

    return img_array


@app.route('/predict', methods=['POST'])
def predict():

    if model is None:
        return jsonify({
            'success': False,
            'message': 'Model not loaded'
        })

    if 'image' not in request.files:
        return jsonify({
            'success': False,
            'message': 'No image provided'
        })

    try:

        image_file = request.files['image']

        image_bytes = image_file.read()

        # Validate image (check for blur and non-teeth images)
        is_valid, validation_msg = validate_image(image_bytes)
        if not is_valid:
            return jsonify({
                'success': False,
                'message': validation_msg
            })

        img_array = preprocess_image(image_bytes)

        predictions = model.predict(img_array, verbose=0)

        # Binary sigmoid output
        raw_output = float(predictions[0][0])

        # Convert to probabilities
        no_caries_prob = raw_output * 100
        caries_prob = (1 - raw_output) * 100

        print("\n--- Prediction Debug ---")
        print(f"Raw Output: {raw_output}")
        print(f"Caries Prob: {caries_prob:.2f}%")
        print(f"No Caries Prob: {no_caries_prob:.2f}%")
        print("------------------------")

        # STRICT threshold
        if caries_prob >= 75:
            label = 'caries'
            confidence = round(caries_prob, 2)
        else:
            label = 'no_caries'
            confidence = round(no_caries_prob, 2)

        # Better risk score logic
        if label == 'caries':
            risk_score = int(caries_prob)
        else:
            risk_score = max(5, int(caries_prob * 0.3))

        # Risk levels
        if risk_score >= 70:
            risk_level = 'High'

        elif risk_score >= 40:
            risk_level = 'Moderate'

        else:
            risk_level = 'Low'

        # Recommendations
        if label == 'caries':

            if risk_level == 'High':

                recommendations = [
                    "⚠️ Cavities detected! Visit a dentist immediately.",
                    "Brush teeth twice daily with fluoride toothpaste.",
                    "Avoid sugary foods and drinks.",
                    "Use antibacterial mouthwash daily.",
                    "Floss between teeth every day."
                ]

            else:

                recommendations = [
                    "Possible early dental issues detected.",
                    "Schedule a dental check-up soon.",
                    "Brush teeth twice daily.",
                    "Reduce sugary food and drink consumption.",
                    "Use fluoride toothpaste daily."
                ]

        else:

            recommendations = [
                "✅ Teeth look healthy! Keep up the good work.",
                "Continue brushing twice daily with fluoride toothpaste.",
                "Floss daily to maintain gum health.",
                "Drink plenty of water.",
                "Visit your dentist every 6 months."
            ]

        return jsonify({

            'success': True,

            'prediction': label,

            'confidence': confidence,

            'caries_prob': round(caries_prob, 2),

            'no_caries_prob': round(no_caries_prob, 2),

            'risk_score': risk_score,

            'risk_level': risk_level,

            'recommendations': recommendations,

            'message': 'Analysis complete'

        })

    except Exception as e:

        print("Prediction Error:", str(e))

        return jsonify({
            'success': False,
            'message': str(e)
        })


@app.route('/health', methods=['GET'])
def health():

    return jsonify({
        'status': 'running',
        'model_loaded': model is not None,
        'model_type': 'Binary Classification',
        'threshold': '75%',
        'accuracy': '98.84%'
    })


if __name__ == '__main__':

    load_model()

    print("AI API running on port 5000")

    app.run(
        host='0.0.0.0',
        port=5000,
        debug=False
    )