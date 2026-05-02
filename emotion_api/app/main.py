from fastapi import FastAPI, HTTPException, UploadFile, File
from PIL import Image
import io

from app.face_predictor import predict_face_emotion

app = FastAPI(
    title="Emotion API",
    version="1.0.0",
    description="API de reconnaissance d'émotions texte et visage"
)

@app.get("/")
def root():
    return {"message": "Emotion API is running"}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/predict-face")
async def predict_face(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        return predict_face_emotion(image)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
