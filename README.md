# Smartbraid-AI

## Cloudinary image uploads

This project stores image URLs in Firestore and uploads the actual image files to Cloudinary.

Before running the app in Android Studio, set these values in `gradle.properties`:

- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_UPLOAD_PRESET`

The upload flow runs on `Dispatchers.IO`, so it is safe to call from the existing Compose/ViewModel code.