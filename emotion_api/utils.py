import json
import os
from typing import Any
from sklearn.metrics import accuracy_score, precision_recall_fscore_support, classification_report


def ensure_dir(path: str) -> None:
    """
    Crée un dossier s'il n'existe pas.
    """
    os.makedirs(path, exist_ok=True)


def save_json(data: Any, filepath: str) -> None:
    """
    Sauvegarde un objet Python en JSON.
    """
    ensure_dir(os.path.dirname(filepath) or ".")
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)


def compute_metrics(y_true, y_pred) -> dict:
    """
    Calcule les métriques principales.
    """
    accuracy = accuracy_score(y_true, y_pred)
    precision, recall, f1, _ = precision_recall_fscore_support(
        y_true,
        y_pred,
        average="macro",
        zero_division=0
    )

    return {
        "accuracy": accuracy,
        "macro_precision": precision,
        "macro_recall": recall,
        "macro_f1": f1,
    }


def print_metrics(metrics: dict, title: str = "Metrics") -> None:
    """
    Affiche les métriques.
    """
    print(f"\n=== {title} ===")
    for key, value in metrics.items():
        print(f"{key}: {value:.4f}")


def print_classification_details(y_true, y_pred, label_names=None) -> None:
    """
    Affiche le classification report détaillé.
    """
    print("\n=== Classification Report ===")
    print(
        classification_report(
            y_true,
            y_pred,
            target_names=label_names,
            zero_division=0
        )
    )