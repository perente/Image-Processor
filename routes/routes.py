from flask import Blueprint, request, jsonify
from utils.image_utils import make_gray_scaled, add_salt_and_pepper_noise, adjust_brightness
from models.models import db, ProcessedFile
import cv2
import numpy as np
import io
import zipfile
import base64
import os

image_bp = Blueprint('image_bp', __name__)

@image_bp.route('/process-image', methods=['POST'])
def process_image():
    try:
        uploaded_file = request.files.get('file')
        if not uploaded_file:
            return jsonify({"error": "No file uploaded"}), 400

        filename = uploaded_file.filename
        base_name, ext = os.path.splitext(filename)
        file = uploaded_file.read()

        np_img = np.frombuffer(file, np.uint8)
        img = cv2.imdecode(np_img, cv2.IMREAD_COLOR)
        if img is None:
            return jsonify({"error": "Invalid image format"}), 400

        actions = request.args.getlist('action')
        processed_images = []

        zip_buffer = io.BytesIO()
        with zipfile.ZipFile(zip_buffer, 'w') as zf:
            for action in actions:
                processed_img = img.copy()

                if action == 'grayscale':
                    processed_img = make_gray_scaled(processed_img)
                elif action == 'noise':
                    salt_prob = float(request.args.get('salt_prob', 0.01))
                    pepper_prob = float(request.args.get('pepper_prob', 0.01))
                    processed_img = add_salt_and_pepper_noise(processed_img, salt_prob, pepper_prob)
                elif action == 'brightness':
                    brightness = int(request.args.get('brightness', 50))
                    processed_img = adjust_brightness(processed_img, brightness)

                # Encode image for preview
                _, img_encoded = cv2.imencode('.png', processed_img)
                image_base64 = base64.b64encode(img_encoded.tobytes()).decode('utf-8')
                processed_images.append({
                    "action": action,
                    "image": image_base64
                })

                # Add image to ZIP
                zf.writestr(f"{base_name}_{action}.png", img_encoded.tobytes())

        # Encode ZIP for download
        zip_buffer.seek(0)
        zip_base64 = base64.b64encode(zip_buffer.read()).decode('utf-8')

        return jsonify({
            "processed_images": processed_images,
            "zip_file": zip_base64
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500
