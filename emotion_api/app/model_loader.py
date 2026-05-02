from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

MODEL_PATH = "models/distilbert_goemotions_single_label"
BASE_TOKENIZER = "distilbert-base-uncased"

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

tokenizer = AutoTokenizer.from_pretrained(BASE_TOKENIZER)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_PATH)

model.to(device)
model.eval()
