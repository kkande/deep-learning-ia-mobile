from pydantic import BaseModel, Field
from typing import Dict

class TextRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Texte à analyser")

class PredictionResponse(BaseModel):
    label: str
    scores: Dict[str, float]
    confidence: float
    inference_time_ms: float
