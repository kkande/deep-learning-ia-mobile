from datasets import load_dataset
from typing import Dict, Any


def load_goemotions(simplified: bool = False) -> Dict[str, Any]:
    """
    Charge le dataset GoEmotions depuis Hugging Face.

    Args:
        simplified (bool): si True, charge la version simplifiée.

    Returns:
        DatasetDict: dataset contenant train, validation et test.
    """
    if simplified:
        dataset = load_dataset("go_emotions", "simplified")
    else:
        dataset = load_dataset("go_emotions")

    return dataset


def get_label_names(dataset) -> list[str]:
    """
    Récupère les noms des labels depuis les features du dataset.
    """
    return dataset["train"].features["labels"].feature.names


def preview_samples(dataset, split: str = "train", n: int = 5) -> None:
    """
    Affiche quelques exemples du dataset.
    """
    print(f"\nAperçu du split: {split} (affichage de {n} exemples) : ")
    for i in range(min(n, len(dataset[split]))):
        print(f"\nExemple {i + 1}")
        print("Text :", dataset[split][i]["text"])
        print("Labels :", dataset[split][i]["labels"])

