import re
from typing import Optional


URL_PATTERN = re.compile(r"https?://\S+|www\.\S+")
MENTION_PATTERN = re.compile(r"@\w+")
MULTISPACE_PATTERN = re.compile(r"\s+")


def clean_text(text: Optional[str]) -> str:
    """
    Nettoie un texte de manière simple.
    """
    if text is None:
        return ""

    text = text.strip().lower()
    text = URL_PATTERN.sub("", text)
    text = MENTION_PATTERN.sub("", text)
    text = MULTISPACE_PATTERN.sub(" ", text)

    return text.strip()


def is_single_label(example: dict) -> bool:
    """
    Vérifie si un exemple possède un seul label.
    """
    return len(example["labels"]) == 1


def extract_single_label(example: dict) -> dict:
    """
    Convertit la liste labels -> label unique
    pour les exemples mono-label.
    """
    return {
        "text": clean_text(example["text"]),
        "label": example["labels"][0],
    }


def preprocess_for_single_label(split_dataset):
    """
    Garde uniquement les exemples mono-label et
    retourne une version simplifiée.
    """
    filtered = split_dataset.filter(is_single_label)
    processed = filtered.map(extract_single_label)
    return processed
