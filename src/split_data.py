
from typing import Tuple, List


def extract_texts_and_labels(split_dataset) -> Tuple[List[str], List[int]]:
    """
    Extrait les textes et labels d'un split déjà prétraité.
    """
    texts = split_dataset["text"]
    labels = split_dataset["label"]
    return texts, labels


def prepare_datasets(dataset):
    """
    Prépare train/validation/test à partir du dataset prétraité.
    """
    x_train, y_train = extract_texts_and_labels(dataset["train"])
    x_val, y_val = extract_texts_and_labels(dataset["validation"])
    x_test, y_test = extract_texts_and_labels(dataset["test"])

    return {
        "x_train": x_train,
        "y_train": y_train,
        "x_val": x_val,
        "y_val": y_val,
        "x_test": x_test,
        "y_test": y_test,
    }
