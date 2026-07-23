import tensorflow as tf
import numpy as np

MODEL_PATH = 'C:/XAMP/htdocs/aicaries/ai_model/model/dental_model.h5'

print("Loading model...")
model = tf.keras.models.load_model(MODEL_PATH)
print("Model loaded!")

# Random test image
test = np.random.rand(1, 224, 224, 3)

pred = model.predict(test)

raw = float(pred[0][0])

caries_prob = (1 - raw) * 100
no_caries_prob = raw * 100

print("\n--- Model Test Results ---")
print(f"Caries probability: {caries_prob:.2f}%")
print(f"No caries probability: {no_caries_prob:.2f}%")

if caries_prob > 55:
    print("Predicted class: CARIES")
else:
    print("Predicted class: NO CARIES")

print("--------------------------")