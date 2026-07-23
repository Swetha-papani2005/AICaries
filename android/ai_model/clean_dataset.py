from PIL import Image
import os

dataset_path = "C:/XAMP/htdocs/aicaries/ai_model/dataset"

deleted = 0

for root, dirs, files in os.walk(dataset_path):
    for file in files:
        path = os.path.join(root, file)

        try:
            img = Image.open(path)
            img.verify()

        except Exception:
            print("Deleting corrupted file:", path)
            os.remove(path)
            deleted += 1

print(f"\nDone! Deleted {deleted} corrupted images.")