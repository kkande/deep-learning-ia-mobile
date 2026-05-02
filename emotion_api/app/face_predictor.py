import time
import torch
from PIL import Image
from torchvision import transforms
from .face_model_loader import face_model, labels, device

transform = transforms.Compose([
    transforms.Grayscale(num_output_channels=1),
    transforms.Resize((48, 48)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.5], std=[0.5])
])


@torch.no_grad()
def predict_face_emotion(image: Image.Image):
    start = time.time()

    image_tensor = transform(image).unsqueeze(0).to(device)

    outputs = face_model(image_tensor)
    probs = torch.softmax(outputs, dim=1).squeeze(0)

    best_idx = int(torch.argmax(probs).item())

    scores = {
        labels[i]: float(probs[i].item())
        for i in range(len(labels))
    }

    return {
        "label": labels[best_idx],
        "scores": scores,
        "confidence": float(probs[best_idx].item()),
        "inference_time_ms": round((time.time() - start) * 1000, 2)
    }
