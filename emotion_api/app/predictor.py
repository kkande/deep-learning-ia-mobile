import time
import torch
from app.model_loader import tokenizer, model, device
from app.utils import clean_text

# Adapte l'ordre à celui de ton vrai modèle
LABELS = ["neutral", "sadness", "happy", "angry", "surprise", "amusement", "anger", "annoyance", "approval", "caring",
         "confusion", "curiosity", "desire", "disappointment", "disapproval", "disgust", "embarrassment", "excitement",
         "fear", "gratitude", "grief", "joy", "love", "nervousness", "optimism", "pride", "realization", "relief", "remorse",
         ]

@torch.no_grad()
def predict_emotion(text: str):
    start = time.time()

    cleaned = clean_text(text)

    inputs = tokenizer(
        cleaned,
        truncation=True,
        padding=True,
        max_length=128,
        return_tensors="pt"
    )

    inputs = {k: v.to(device) for k, v in inputs.items()}

    outputs = model(**inputs)
    probs = torch.softmax(outputs.logits, dim=-1).squeeze(0)

    scores = {LABELS[i]: float(probs[i].item()) for i in range(len(LABELS))}
    best_idx = int(torch.argmax(probs).item())

    return {
        "label": LABELS[best_idx],
        "scores": scores,
        "confidence": float(probs[best_idx].item()),
        "inference_time_ms": round((time.time() - start) * 1000, 2)
    }
