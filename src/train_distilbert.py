import os
import torch

NUM_THREADS = 8  # à tester : 4, 8, 12, 16 selon ta machine

torch.set_num_threads(NUM_THREADS)
torch.set_num_interop_threads(2)

os.environ["OMP_NUM_THREADS"] = str(NUM_THREADS)
os.environ["MKL_NUM_THREADS"] = str(NUM_THREADS)
import numpy as np
import evaluate

from datasets import DatasetDict
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    DataCollatorWithPadding,
    TrainingArguments,
    Trainer,
)

from data_loader import load_goemotions, get_label_names
from preprocess import preprocess_for_single_label
from utils import save_json, ensure_dir

MODEL_NAME = "distilbert-base-uncased"
MAX_LENGTH = 128
OUTPUT_DIR = "outputs/distilbert_run"
MODEL_DIR = "models/distilbert_goemotions_single_label"


def prepare_dataset() -> tuple[DatasetDict, list[str]]:
    raw_dataset = load_goemotions()
    label_names = get_label_names(raw_dataset)

    processed = DatasetDict({
        "train": preprocess_for_single_label(raw_dataset["train"]),
        "validation": preprocess_for_single_label(raw_dataset["validation"]),
        "test": preprocess_for_single_label(raw_dataset["test"]),
    })

    return processed, label_names


def tokenize_dataset(dataset: DatasetDict, tokenizer):
    def tokenize_function(examples):
        return tokenizer(
            examples["text"],
            truncation=True,
            max_length=MAX_LENGTH,
        )

    tokenized = dataset.map(tokenize_function, batched=True)
    tokenized = tokenized.remove_columns(["text"])
    return tokenized


def build_id2label(label_names: list[str]) -> tuple[dict[int, str], dict[str, int]]:
    id2label = {i: name for i, name in enumerate(label_names)}
    label2id = {name: i for i, name in enumerate(label_names)}
    return id2label, label2id


def main():
    ensure_dir("outputs")
    ensure_dir("models")

    print("Préparation du dataset...")
    dataset, label_names = prepare_dataset()
    print(dataset)

    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

    print("Tokenisation...")
    tokenized_dataset = tokenize_dataset(dataset, tokenizer)

    id2label, label2id = build_id2label(label_names)

    model = AutoModelForSequenceClassification.from_pretrained(
        MODEL_NAME,
        num_labels=len(label_names),
        id2label=id2label,
        label2id=label2id,
    )

    accuracy_metric = evaluate.load("accuracy")
    precision_metric = evaluate.load("precision")
    recall_metric = evaluate.load("recall")
    f1_metric = evaluate.load("f1")

    def compute_metrics(eval_pred):
        logits, labels = eval_pred
        preds = np.argmax(logits, axis=-1)

        accuracy = accuracy_metric.compute(predictions=preds, references=labels)
        precision = precision_metric.compute(
            predictions=preds, references=labels, average="macro", zero_division=0
        )
        recall = recall_metric.compute(
            predictions=preds, references=labels, average="macro", zero_division=0
        )
        f1 = f1_metric.compute(
            predictions=preds, references=labels, average="macro", zero_division=0
        )

        return {
            "accuracy": accuracy["accuracy"],
            "macro_precision": precision["precision"],
            "macro_recall": recall["recall"],
            "macro_f1": f1["f1"],
        }

    training_args = TrainingArguments(
    output_dir=OUTPUT_DIR,
    eval_strategy="epoch",
    save_strategy="epoch",
    logging_strategy="epoch",
    learning_rate=2e-5,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=16,
    num_train_epochs=3,
    weight_decay=0.01,
    load_best_model_at_end=True,
    metric_for_best_model="macro_f1",
    greater_is_better=True,
    report_to="none",
    save_total_limit=2,
    dataloader_num_workers=4,
)

    data_collator = DataCollatorWithPadding(tokenizer=tokenizer)

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=tokenized_dataset["train"],
        eval_dataset=tokenized_dataset["validation"],
        processing_class=tokenizer,
        data_collator=data_collator,
        compute_metrics=compute_metrics,
    )

    print("Début de l'entraînement DistilBERT...")
    trainer.train()

    print("\nÉvaluation finale sur validation...")
    val_results = trainer.evaluate(tokenized_dataset["validation"])
    print(val_results)

    print("\nÉvaluation finale sur test...")
    test_results = trainer.evaluate(tokenized_dataset["test"])
    print(test_results)

    print("\nSauvegarde du modèle et du tokenizer...")
    trainer.save_model(MODEL_DIR)
    tokenizer.save_pretrained(MODEL_DIR)

    metrics_to_save = {
        "validation": val_results,
        "test": test_results,
        "model_name": MODEL_NAME,
        "max_length": MAX_LENGTH,
        "num_labels": len(label_names),
    }
    save_json(metrics_to_save, os.path.join(OUTPUT_DIR, "metrics.json"))

    print(f"\nModèle sauvegardé dans : {MODEL_DIR}")
    print(f"Métriques sauvegardées dans : {os.path.join(OUTPUT_DIR, 'metrics.json')}")


if __name__ == "__main__":
    main()