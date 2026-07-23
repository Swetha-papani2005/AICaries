import tensorflow as tf
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.models import Model
from tensorflow.keras.layers import GlobalAveragePooling2D, Dense, Dropout
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import ModelCheckpoint, EarlyStopping
from tensorflow.keras.optimizers import Adam
import os
import numpy as np

DATASET_DIR = 'C:/XAMP/htdocs/aicaries/ai_model/dataset'
MODEL_PATH  = 'C:/XAMP/htdocs/aicaries/ai_model/model/dental_model.h5'
IMAGE_SIZE  = (224, 224)
BATCH_SIZE  = 16

print("Loading images...")

# Training with augmentation
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=15,
    zoom_range=0.1,
    horizontal_flip=True,
    width_shift_range=0.1,
    height_shift_range=0.1,
    validation_split=0.2
)

# Validation WITHOUT augmentation
val_datagen = ImageDataGenerator(
    rescale=1./255,
    validation_split=0.2
)

train_data = train_datagen.flow_from_directory(
    DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='binary',
    subset='training',
    shuffle=True
)

val_data = val_datagen.flow_from_directory(
    DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='binary',
    subset='validation',
    shuffle=False
)

print("Classes found:", train_data.class_indices)
print("Training images:", train_data.samples)
print("Validation images:", val_data.samples)

# Build model
base = MobileNetV2(
    input_shape=(224, 224, 3),
    include_top=False,
    weights='imagenet'
)
base.trainable = False

x = base.output
x = GlobalAveragePooling2D()(x)
x = Dense(64, activation='relu')(x)
x = Dropout(0.5)(x)
output = Dense(1, activation='sigmoid')(x)

model = Model(inputs=base.input, outputs=output)

model.compile(
    optimizer=Adam(learning_rate=0.0001),
    loss='binary_crossentropy',
    metrics=['accuracy']
)

os.makedirs('C:/XAMP/htdocs/aicaries/ai_model/model', exist_ok=True)

checkpoint = ModelCheckpoint(
    MODEL_PATH,
    monitor='val_accuracy',
    save_best_only=True,
    verbose=1
)

earlystop = EarlyStopping(
    monitor='val_accuracy',
    patience=8,
    restore_best_weights=True,
    verbose=1
)

print("\nPhase 1 - Training top layers only...")
history1 = model.fit(
    train_data,
    epochs=20,
    validation_data=val_data,
    callbacks=[checkpoint, earlystop]
)

# Phase 2 - Fine tune last 30 layers
print("\nPhase 2 - Fine tuning...")
base.trainable = True
for layer in base.layers[:-30]:
    layer.trainable = False

model.compile(
    optimizer=Adam(learning_rate=0.00001),
    loss='binary_crossentropy',
    metrics=['accuracy']
)

history2 = model.fit(
    train_data,
    epochs=20,
    validation_data=val_data,
    callbacks=[
        ModelCheckpoint(MODEL_PATH, monitor='val_accuracy', save_best_only=True, verbose=1),
        EarlyStopping(monitor='val_accuracy', patience=8, restore_best_weights=True, verbose=1)
    ]
)

print("\nTraining complete!")
print("Model saved to:", MODEL_PATH)

all_val_acc = history1.history['val_accuracy'] + history2.history['val_accuracy']
print(f"Best Validation Accuracy: {max(all_val_acc)*100:.2f}%")

# Quick sanity check
print("\n--- Sanity Check ---")
test = np.random.rand(1, 224, 224, 3)
pred = model.predict(test)
print(f"Random input prediction: {pred[0][0]*100:.2f}% caries")
print("If this is close to 50%, model is balanced!")