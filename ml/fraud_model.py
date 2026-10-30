"""Partie H - Pipeline de détection de fraude (code fourni, NON exécuté : l'entraînement n'a pas été lancé,
la détection en production passe par le LLM Groq dans chat-bot-service /api/fraud).

Usage (si on veut entraîner) : python3 ml/fraud_model.py bank_transactions.csv
"""
import sys

import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.metrics import average_precision_score, classification_report, confusion_matrix, precision_recall_curve
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

NUM = ["amount", "hour", "transactions_last_24h", "average_amount_last_7d"]
CAT = ["country", "merchant_category"]


def build_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    # ratio montant / moyenne 7 jours (variables historiques déjà calculées sans fuite)
    df["amount_ratio_7d"] = df["amount"] / (df["average_amount_last_7d"] + 1e-6)
    df["is_night"] = df["hour"].between(0, 5).astype(int)
    return df


def main(path: str) -> None:
    df = build_features(pd.read_csv(path))
    X, y = df[NUM + CAT + ["amount_ratio_7d", "is_night"]], df["is_fraud"]
    # split stratifié : les classes sont déséquilibrées
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, stratify=y, random_state=42)

    model = Pipeline([
        ("prep", ColumnTransformer([
            ("num", StandardScaler(), NUM + ["amount_ratio_7d", "is_night"]),
            ("cat", OneHotEncoder(handle_unknown="ignore"), CAT),
        ])),
        ("clf", HistGradientBoostingClassifier(class_weight="balanced", random_state=42)),
    ])
    model.fit(X_train, y_train)

    # seuil choisi UNIQUEMENT sur les données d'entraînement (maximise le F1), jamais sur le test
    p_train = model.predict_proba(X_train)[:, 1]
    prec, rec, thr = precision_recall_curve(y_train, p_train)
    f1 = 2 * prec * rec / (prec + rec + 1e-9)
    threshold = thr[f1[:-1].argmax()]

    p_test = model.predict_proba(X_test)[:, 1]
    y_pred = (p_test >= threshold).astype(int)
    print(f"Seuil retenu : {threshold:.3f}")
    print(classification_report(y_test, y_pred, digits=3))
    print("Matrice de confusion :\n", confusion_matrix(y_test, y_pred))
    print(f"Average precision (PR-AUC) : {average_precision_score(y_test, p_test):.3f}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "bank_transactions.csv")
