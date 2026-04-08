from fastapi import FastAPI, HTTPException
from app.schemas import TextRequest, PredictionResponse
from app.predictor import predict_emotion

app = FastAPI(
    title="Emotion Text API",
    version="1.0.0",
    description="API de reconnaissance d'émotion à partir de texte"
)

@app.get("/")
def root():
    return {"message": "Emotion Text API is running"}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/predict-text", response_model=PredictionResponse)
def predict_text(payload: TextRequest):
    text = payload.text.strip()

    if not text:
        raise HTTPException(status_code=400, detail="Le texte ne peut pas être vide.")

    return predict_emotion(text)
