import numpy as np
from sklearn.naive_bayes import BernoulliNB
from IMDBPreprocessor import IMDBPreprocessor
from EvaluationMetrics import calculate_macro_micro_metrics, calculate_class_metrics
from MetricsVisualizer import MetricsVisualizer
from MetricsCSVWriter import MetricsCSVWriter

# Define dataset paths
train_path = "aclImdb/train"
test_path = "aclImdb/test"

# Set hyperparameters for preprocessing
n = 5000  # Number of most frequent words to remove
k = 1000  # Number of least frequent words to remove
m = 5000  # Number of top words to select based on information gain

# Load and preprocess the IMDB dataset
print("Loading and processing IMDB dataset...")
preprocessor = IMDBPreprocessor(train_path, test_path, n=n, k=k, m=m)
preprocessor.preprocess()

# Extract processed features and labels
training_data = preprocessor.feature_matrix[:20000]
training_labels = preprocessor.labels[:20000]
development_data = preprocessor.feature_matrix[20000:25000]
development_labels = preprocessor.labels[20000:25000]
testing_data = preprocessor.feature_matrix[25000:]
testing_labels = preprocessor.labels[25000:]

# Ensure labels are binary integers
training_labels = np.asarray(training_labels).astype(int)
development_labels = np.asarray(development_labels).astype(int)
testing_labels = np.asarray(testing_labels).astype(int)

# Train Bernoulli Naive Bayes using Scikit-learn
classifier = BernoulliNB(alpha=1.0)
metrics_writer = MetricsCSVWriter()
training_metrics_csv = "training_metrics_bayes_sklearn.csv"
evaluation_metrics_csv = "evaluation_metrics_bayes_sklearn.csv"

# Incremental training and evaluation
print("Training Naive Bayes incrementally...")
training_sizes = [5000, 10000, 15000, 20000]

for size in training_sizes:
    subset_train = training_data[:size]
    subset_labels = training_labels[:size]
    
    classifier.fit(subset_train, subset_labels)
    y_train_pred = classifier.predict(subset_train)
    y_dev_pred = classifier.predict(development_data)

    class_metrics_train = calculate_class_metrics(subset_labels, y_train_pred)
    class_metrics_dev = calculate_class_metrics(development_labels, y_dev_pred)
    
    print(f"Training Size: {size}, Train Precision (class 1): {class_metrics_train[1][0]:.4f}, Train Recall (class 1): {class_metrics_train[1][1]:.4f}, Train F1 (class 1): {class_metrics_train[1][2]:.4f}, "
          f"Dev Precision (class 1): {class_metrics_dev[1][0]:.4f}, Dev Recall (class 1): {class_metrics_dev[1][1]:.4f}, Dev F1 (class 1): {class_metrics_dev[1][2]:.4f}")
    
    # Store training metrics in CSV
    metrics_writer.write_training_metrics(training_metrics_csv, size, class_metrics_train[1], class_metrics_dev[1])

# Final training and testing
print("Final training and testing...")
classifier.fit(training_data, training_labels)
y_test_pred = classifier.predict(testing_data)

class_metrics_test, precision_macro, recall_macro, f1_macro, precision_micro, recall_micro, f1_micro = calculate_macro_micro_metrics(testing_labels, y_test_pred)

print(f"Test Class 0 Precision: {class_metrics_test[0][0]:.4f}, Test Class 0 Recall: {class_metrics_test[0][1]:.4f}, Test Class 0 F1: {class_metrics_test[0][2]:.4f}")
print(f"Test Class 1 Precision: {class_metrics_test[1][0]:.4f}, Test Class 1 Recall: {class_metrics_test[1][1]:.4f}, Test Class 1 F1: {class_metrics_test[1][2]:.4f}")
print(f"Test Macro Precision: {precision_macro:.4f}, Test Macro Recall: {recall_macro:.4f}, Test Macro F1: {f1_macro:.4f}")
print(f"Test Micro Precision: {precision_micro:.4f}, Test Micro Recall: {recall_micro:.4f}, Test Micro F1: {f1_micro:.4f}")

# Store test evaluation metrics in CSV
metrics_writer.write_testing_metrics(evaluation_metrics_csv, (class_metrics_test[0], class_metrics_test[1], precision_macro, recall_macro, f1_macro, precision_micro, recall_micro, f1_micro))

# Generate visualizations
visualizer = MetricsVisualizer(training_metrics_csv, evaluation_metrics_csv)
visualizer.plot_learning_curves("learning_curves_bayes_sklearn")
visualizer.generate_evaluation_table("evaluation_metrics_bayes_sklearn.png")
