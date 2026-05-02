import sys
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent))

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression

from data_loader import load_goemotions, get_label_names
from preprocess import preprocess_split
from split_data import prepare_datasets
from utils import compute_metrics, print_metrics, print_classification_details


def main():
    print("Chargement du dataset GoEmotions...")
    raw_dataset = load_goemotions()

    label_names = get_label_names(raw_dataset)

    print("Prétraitement des splits...")
    processed_dataset = {
        "train": preprocess_split(raw_dataset["train"]),
        "validation": preprocess_split(raw_dataset["validation"]),
        "test": preprocess_split(raw_dataset["test"]),
    }

    data = prepare_datasets(processed_dataset)

    print(f"Train size: {len(data['x_train'])}")
    print(f"Validation size: {len(data['x_val'])}")
    print(f"Test size: {len(data['x_test'])}")

    print("\nVectorisation TF-IDF...")
    vectorizer = TfidfVectorizer(
        max_features=20000,
        ngram_range=(1, 2),
        min_df=2
    )

    x_train_vec = vectorizer.fit_transform(data["x_train"])
    x_val_vec = vectorizer.transform(data["x_val"])
    x_test_vec = vectorizer.transform(data["x_test"])

    print("Entraînement du modèle baseline...")
    model = LogisticRegression(
        max_iter=1000,
        class_weight="balanced",
        random_state=42
    )
    model.fit(x_train_vec, data["y_train"])

    print("\nÉvaluation sur validation...")
    val_preds = model.predict(x_val_vec)
    val_metrics = compute_metrics(data["y_val"], val_preds)
    print_metrics(val_metrics, title="Validation Metrics")

    print("\nÉvaluation sur test...")
    test_preds = model.predict(x_test_vec)
    test_metrics = compute_metrics(data["y_test"], test_preds)
    print_metrics(test_metrics, title="Test Metrics")

    # Adapter les noms de labels uniquement aux labels présents
    used_labels = sorted(set(data["y_test"]) | set(test_preds))
    used_label_names = [label_names[i] for i in used_labels]

    print_classification_details(
        data["y_test"],
        test_preds,
        label_names=used_label_names if len(used_label_names) == len(used_labels) else None
    )


if __name__ == "__main__":
    main()

