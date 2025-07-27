"""
Provides functions for calculating precision, recall, and F1-score metrics
for binary classification tasks. It includes functions to compute per-class metrics as well
as macro and micro averages.

"""

import numpy as np
from sklearn.metrics import precision_score, recall_score, f1_score

def calculate_class_metrics(y_true, y_pred):
    """
    Computes precision, recall, and F1-score for each class (0 and 1).

    Parameters:
    y_true (array-like): True labels.
    y_pred (array-like): Predicted labels.

    Returns:
    dict: Dictionary with class labels as keys and (precision, recall, F1-score) tuples as values.
    """
    class_metrics = {}
    for label in [0, 1]:
        precision = precision_score(y_true, y_pred, labels=[label], average=None, zero_division=0)[0]
        recall = recall_score(y_true, y_pred, labels=[label], average=None, zero_division=0)[0]
        f1 = f1_score(y_true, y_pred, labels=[label], average=None, zero_division=0)[0]
        class_metrics[label] = (precision, recall, f1)
    return class_metrics

def calculate_macro_micro_metrics(y_true, y_pred):
    """
    Computes macro and micro precision, recall, and F1-score.

    Parameters:
    y_true (array-like): True labels.
    y_pred (array-like): Predicted labels.

    Returns:
    tuple: (class_metrics dict, macro precision, macro recall, macro F1-score, micro precision, micro recall, micro F1-score)
    """
    class_metrics = calculate_class_metrics(y_true, y_pred)
    precision_macro = precision_score(y_true, y_pred, average='macro', zero_division=0)
    recall_macro = recall_score(y_true, y_pred, average='macro', zero_division=0)
    f1_macro = f1_score(y_true, y_pred, average='macro', zero_division=0)
    precision_micro = precision_score(y_true, y_pred, average='micro', zero_division=0)
    recall_micro = recall_score(y_true, y_pred, average='micro', zero_division=0)
    f1_micro = f1_score(y_true, y_pred, average='micro', zero_division=0)
    return class_metrics, precision_macro, recall_macro, f1_macro, precision_micro, recall_micro, f1_micro
