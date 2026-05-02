import json
import torch
import torch.nn as nn

MODEL_PATH = "models/fer_cnn/fer_cnn_best.pth"
METADATA_PATH = "models/fer_cnn/metadata.json"

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


class FERCNN(nn.Module):
    def __init__(self, num_classes=7):
        super().__init__()

        self.features = nn.Sequential(
            nn.Conv2d(1, 32, kernel_size=3, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d(2),

            nn.Conv2d(32, 64, kernel_size=3, padding=1),
            nn.BatchNorm2d(64),
            nn.ReLU(),
            nn.MaxPool2d(2),

            nn.Conv2d(64, 128, kernel_size=3, padding=1),
            nn.BatchNorm2d(128),
            nn.ReLU(),
            nn.MaxPool2d(2),

            nn.Conv2d(128, 256, kernel_size=3, padding=1),
            nn.BatchNorm2d(256),
            nn.ReLU(),
            nn.MaxPool2d(2)
        )

        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Linear(256 * 3 * 3, 256),
            nn.ReLU(),
            nn.Dropout(0.4),
            nn.Linear(256, num_classes)
        )

    def forward(self, x):
        return self.classifier(self.features(x))


with open(METADATA_PATH, "r", encoding="utf-8") as f:
    metadata = json.load(f)

labels = {int(k): v for k, v in metadata["labels"].items()}

face_model = FERCNN(num_classes=len(labels))
face_model.load_state_dict(torch.load(MODEL_PATH, map_location=device))
face_model.to(device)
face_model.eval()
