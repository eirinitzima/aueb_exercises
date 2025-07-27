"""
Trains and evaluates the Bernoulli Naive Bayes classifier on the IMDB dataset.
It preprocesses the dataset, trains the model incrementally on increasing amounts of data,
and evaluates performance using precision, recall, and F1-score.

Steps:
1. Load and preprocess the IMDB dataset with configurable parameters.
2. Train Naive Bayes incrementally and evaluate on development data.
3. Perform final training and evaluate on test data.
4. Store metrics in CSV files and generate learning curve plots.
"""

import numpy as np
from IMDBPreprocessor import IMDBPreprocessor
from NaiveBayesBernoulli import NaiveBayesBernoulli
from EvaluationMetrics import calculate_macro_micro_metrics, calculate_class_metrics
from MetricsCSVWriter import *
from MetricsVisualizer import *

# Define dataset paths
train_path = "aclImdb/train"
test_path = "aclImdb/test"

# Set hyperparameters for preprocessing
n = 5000  # Number of most frequent words to remove
k = 1000  # Number of least frequent words to remove
m = 5000  # Number of top words to select based on information gain

#Initialize the csv writer and set the csv file names
metrics_writer = MetricsCSVWriter()
training_metrics_csv = "training_metrics_bayes_custom.csv"
evaluation_metrics_csv = "evaluation_metrics_bayes_custom.csv"

# Load and preprocess the IMDB dataset
print("Loading and processing IMDB dataset...")
preprocessor = IMDBPreprocessor(train_path, test_path, n=n, k=k, m=m)
preprocessor.preprocess()

# Extract processed features and labels
train_size = 20000
dev_size = 5000
training_data = preprocessor.feature_matrix[:train_size]
development_data = preprocessor.feature_matrix[train_size:train_size + dev_size]
training_labels = preprocessor.labels[:train_size]
development_labels = preprocessor.labels[train_size:train_size + dev_size]

#Initialize the Bernoulli Naive Bayes
classifier = NaiveBayesBernoulli()

# Incremental training and evaluation
print("Training Naive Bayes incrementally...")
training_sizes = [5000, 10000, 15000, 20000] 

for size in training_sizes:
    subset_train = training_data[:size]
    subset_labels = training_labels[:size]
    
    classifier.train(subset_train, subset_labels)
    y_train_pred = np.array([classifier.predict(x) for x in subset_train])
    y_dev_pred = np.array([classifier.predict(x) for x in development_data])
    
    class_metrics_train = calculate_class_metrics(subset_labels, y_train_pred)
    class_metrics_dev = calculate_class_metrics(development_labels, y_dev_pred)
    
    print(f"Training Size: {size}, Train Precision (class 1): {class_metrics_train[1][0]:.4f}, Train Recall (class 1): {class_metrics_train[1][1]:.4f}, Train F1 (class 1): {class_metrics_train[1][2]:.4f}, \
          Dev Precision (class 1): {class_metrics_dev[1][0]:.4f}, Dev Recall (class 1): {class_metrics_dev[1][1]:.4f}, Dev F1 (class 1): {class_metrics_dev[1][2]:.4f}")

    # Store training metrics in CSV
    metrics_writer.write_training_metrics(training_metrics_csv, size, class_metrics_train[1], class_metrics_dev[1])


# Final training and testing
print("Final training and testing...")
classifier.train(preprocessor.feature_matrix, preprocessor.labels)
test_data = preprocessor.feature_matrix[-25000:]
test_labels = preprocessor.labels[-25000:]
y_test_pred = np.array([classifier.predict(x) for x in test_data])

class_metrics_test, precision_macro, recall_macro, f1_macro, precision_micro, recall_micro, f1_micro = calculate_macro_micro_metrics(test_labels, y_test_pred)

print(f"Test Class 0 Precision: {class_metrics_test[0][0]:.4f}, Test Class 0 Recall: {class_metrics_test[0][1]:.4f}, Test Class 0 F1: {class_metrics_test[0][2]:.4f}")
print(f"Test Class 1 Precision: {class_metrics_test[1][0]:.4f}, Test Class 1 Recall: {class_metrics_test[1][1]:.4f}, Test Class 1 F1: {class_metrics_test[1][2]:.4f}")
print(f"Test Macro Precision: {precision_macro:.4f}, Test Macro Recall: {recall_macro:.4f}, Test Macro F1: {f1_macro:.4f}")
print(f"Test Micro Precision: {precision_micro:.4f}, Test Micro Recall: {recall_micro:.4f}, Test Micro F1: {f1_micro:.4f}")

# Store test evaluation metrics in CSV
metrics_writer.write_testing_metrics(evaluation_metrics_csv, (class_metrics_test[0], class_metrics_test[1], precision_macro, recall_macro, f1_macro, precision_micro, recall_micro, f1_micro))

# Generate visualizations
visualizer = MetricsVisualizer(training_metrics_csv, evaluation_metrics_csv)
visualizer.plot_learning_curves("learning_curves_bayes_custom")
visualizer.generate_evaluation_table("evaluation_metrics_bayes_custom.png")