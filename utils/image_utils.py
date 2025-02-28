import cv2
import numpy as np

def make_gray_scaled(image):
    """Convert image to grayscale."""
    return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

def add_salt_and_pepper_noise(image, salt_prob=0.01, pepper_prob=0.01):
    """Add salt and pepper noise to the image."""
    noisy = image.copy()
    total_pixels = image.shape[0] * image.shape[1]
    salt_pixels = int(total_pixels * salt_prob)
    salt_coords = [np.random.randint(0, i - 1, salt_pixels) for i in image.shape[:2]]
    noisy[salt_coords[0], salt_coords[1]] = 255 if len(image.shape) == 2 else [255, 255, 255]

    pepper_pixels = int(total_pixels * pepper_prob)
    pepper_coords = [np.random.randint(0, i - 1, pepper_pixels) for i in image.shape[:2]]
    noisy[pepper_coords[0], pepper_coords[1]] = 0 if len(image.shape) == 2 else [0, 0, 0]

    return noisy

def adjust_brightness(image, brightness=50):
    """Adjust brightness of the image."""
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    h, s, v = cv2.split(hsv)
    v = cv2.add(v, brightness)
    final_hsv = cv2.merge((h, s, v))
    bright_image = cv2.cvtColor(final_hsv, cv2.COLOR_HSV2BGR)
    return bright_image
