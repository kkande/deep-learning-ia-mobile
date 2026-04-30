import os
import copy
import time
import json
import numpy as np
import pandas as pd
from PIL import Image

import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms

from sklearn.metrics import accuracy_score, f1_score


# =========================
# Configuration
# =========================
DATASET_PATH = "/kaggle/input/datasets/hcmushqhuy/representation-learning-fer-challenge/challenges-in-representation-learning-facial-expression-recognition-challenge/fer2013/fer2013/fer2013.csv"
OUTPUT_DIR = "outputs/fer_run"
MODEL_DIR = "models/fer_cnn"
BATCH_SIZE = 64
NUM_EPOCHS = 15
LEARNING_RATE = 1e-3
IMAGE_SIZE = 48
NUM_CLASSES = 7
SEED = 42

LABELS = {
    0: "angry",
    1: "disgust",
    2: "fear",
    3: "happy",
    4: "sad",
    5: "surprise",
    6: "neutral"
}


# =========================
# Utils
# =========================
def ensure_dir(path: str):
    os.makedirs(path, exist_ok=True)


def save_json(data, filepath: str):
    ensure_dir(os.path.dirname(filepath) or ".")
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)


def set_seed(seed: int = 42):
    torch.manual_seed(seed)
    np.random.seed(seed)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)


# =========================
# Dataset
# =========================
class FER2013Dataset(Dataset):
    def __init__(self, df: pd.DataFrame, transform=None):
        self.df = df.reset_index(drop=True)
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        row = self.df.iloc[idx]

        emotion = int(row["emotion"])
        pixels = np.array(list(map(int, row["pixels"].split())), dtype=np.uint8).reshape(48, 48)

        image = Image.fromarray(pixels, mode="L")

        if self.transform:
            image = self.transform(image)

        return image, emotion


def load_fer2013(csv_path: str):
    df = pd.read_csv(csv_path)

    # Nettoyage des noms de colonnes (IMPORTANT)
    df.columns = df.columns.str.strip()

    print("Colonnes détectées :", df.columns)

    # Adapter selon le nom réel
    usage_col = None
    for col in df.columns:
        if col.lower() == "usage":
            usage_col = col

    if usage_col is None:
        raise ValueError("Colonne 'usage' introuvable dans le dataset")

    train_df = df[df[usage_col] == "Training"]
    val_df = df[df[usage_col] == "PublicTest"]
    test_df = df[df[usage_col] == "PrivateTest"]

    return train_df, val_df, test_df


# =========================
# Model
# =========================
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
        x = self.features(x)
        x = self.classifier(x)
        return x


# =========================
# Training / Evaluation
# =========================
def train_one_epoch(model, loader, criterion, optimizer, device):
    model.train()
    running_loss = 0.0
    all_preds = []
    all_labels = []

    for images, labels in loader:
        images = images.to(device)
        labels = labels.to(device)

        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()

        running_loss += loss.item() * images.size(0)

        preds = torch.argmax(outputs, dim=1)
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())

    epoch_loss = running_loss / len(loader.dataset)
    epoch_acc = accuracy_score(all_labels, all_preds)
    epoch_f1 = f1_score(all_labels, all_preds, average="macro")

    return epoch_loss, epoch_acc, epoch_f1


@torch.no_grad()
def evaluate(model, loader, criterion, device):
    model.eval()
    running_loss = 0.0
    all_preds = []
    all_labels = []

    for images, labels in loader:
        images = images.to(device)
        labels = labels.to(device)

        outputs = model(images)
        loss = criterion(outputs, labels)

        running_loss += loss.item() * images.size(0)

        preds = torch.argmax(outputs, dim=1)
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())

    epoch_loss = running_loss / len(loader.dataset)
    epoch_acc = accuracy_score(all_labels, all_preds)
    epoch_f1 = f1_score(all_labels, all_preds, average="macro")

    return epoch_loss, epoch_acc, epoch_f1, all_labels, all_preds


def main():
    set_seed(SEED)
    ensure_dir(OUTPUT_DIR)
    ensure_dir(MODEL_DIR)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print("Device:", device)

    print("Chargement du dataset FER2013...")
    train_df, val_df, test_df = load_fer2013(DATASET_PATH)
    print(f"Train: {len(train_df)} | Val: {len(val_df)} | Test: {len(test_df)}")

    train_transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.RandomHorizontalFlip(),
        transforms.RandomRotation(10),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.5], std=[0.5])
    ])

    eval_transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.5], std=[0.5])
    ])

    train_dataset = FER2013Dataset(train_df, transform=train_transform)
    val_dataset = FER2013Dataset(val_df, transform=eval_transform)
    test_dataset = FER2013Dataset(test_df, transform=eval_transform)

    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=2)
    val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=2)
    test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=2)

    model = FERCNN(num_classes=NUM_CLASSES).to(device)
    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=LEARNING_RATE)

    history = []
    best_val_f1 = -1.0
    best_state = None

    print("Début de l'entraînement FER...")
    start_time = time.time()

    for epoch in range(NUM_EPOCHS):
        train_loss, train_acc, train_f1 = train_one_epoch(
            model, train_loader, criterion, optimizer, device
        )

        val_loss, val_acc, val_f1, _, _ = evaluate(
            model, val_loader, criterion, device
        )

        epoch_info = {
            "epoch": epoch + 1,
            "train_loss": train_loss,
            "train_acc": train_acc,
            "train_f1": train_f1,
            "val_loss": val_loss,
            "val_acc": val_acc,
            "val_f1": val_f1
        }
        history.append(epoch_info)

        print(
            f"Epoch [{epoch+1}/{NUM_EPOCHS}] | "
            f"Train Loss: {train_loss:.4f} Acc: {train_acc:.4f} F1: {train_f1:.4f} | "
            f"Val Loss: {val_loss:.4f} Acc: {val_acc:.4f} F1: {val_f1:.4f}"
        )

        if val_f1 > best_val_f1:
            best_val_f1 = val_f1
            best_state = copy.deepcopy(model.state_dict())

    elapsed = time.time() - start_time
    print(f"Temps total d'entraînement: {elapsed/60:.2f} min")

    print("Chargement du meilleur modèle...")
    model.load_state_dict(best_state)

    print("Évaluation finale sur le test...")
    test_loss, test_acc, test_f1, y_true, y_pred = evaluate(
        model, test_loader, criterion, device
    )

    print(f"Test Loss: {test_loss:.4f}")
    print(f"Test Accuracy: {test_acc:.4f}")
    print(f"Test Macro F1: {test_f1:.4f}")

    print("Sauvegarde du modèle...")
    torch.save(model.state_dict(), os.path.join(MODEL_DIR, "fer_cnn_best.pth"))

    metadata = {
        "labels": LABELS,
        "image_size": IMAGE_SIZE,
        "num_classes": NUM_CLASSES
    }
    save_json(metadata, os.path.join(MODEL_DIR, "metadata.json"))

    results = {
        "best_val_f1": best_val_f1,
        "test_loss": test_loss,
        "test_accuracy": test_acc,
        "test_macro_f1": test_f1,
        "epochs": NUM_EPOCHS,
        "batch_size": BATCH_SIZE,
        "learning_rate": LEARNING_RATE
    }

    save_json(results, os.path.join(OUTPUT_DIR, "metrics.json"))
    save_json(history, os.path.join(OUTPUT_DIR, "history.json"))

    print(f"Modèle sauvegardé dans : {MODEL_DIR}")
    print(f"Résultats sauvegardés dans : {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
