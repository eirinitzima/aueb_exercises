"""
Provides functionality for writing performance metrics into CSV files. 
It supports both training and evaluation metrics, ensuring that results
can be stored in order to be analyzed efficiently.

"""

import csv

class MetricsCSVWriter:
    """
    A class to write training and testing metrics into CSV files.
    
    Methods:
    - write_training_metrics: Writes training metrics to a CSV file.
    - write_testing_metrics: Writes evaluation metrics to a CSV file.
    """
    
    def _initialize_csv(self, filename, headers):
        """
        Creates a CSV file with the provided headers if it does not already exist.
        
        Parameters:
        filename (str): The name of the CSV file.
        headers (list): A list of column headers for the CSV file.
        """
        try:
            with open(filename, 'x', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(headers)
        except FileExistsError:
            pass  # If the file exists, do nothing
    
    def write_training_metrics(self, filename, size, train_metrics, dev_metrics):
        """
        Writes training metrics into a CSV file.
        
        Parameters:
        filename (str): The name of the CSV file.
        size (int): The number of training examples used.
        train_metrics (tuple): Precision, recall, and F1-score for the training set.
        dev_metrics (tuple): Precision, recall, and F1-score for the development set.
        """
        self._initialize_csv(filename, ["Training Size", "Train Precision (class 1)", "Train Recall (class 1)", "Train F1 (class 1)", "Dev Precision (class 1)", "Dev Recall (class 1)", "Dev F1 (class 1)"])
        with open(filename, 'a', newline='') as file:
            writer = csv.writer(file)
            writer.writerow([size, train_metrics[0], train_metrics[1], train_metrics[2], dev_metrics[0], dev_metrics[1], dev_metrics[2]])
    
    def write_testing_metrics(self, filename, test_metrics):
        """
        Writes testing evaluation metrics into a CSV file.
        
        Parameters:
        filename (str): The name of the CSV file.
        test_metrics (tuple): A tuple containing class-wise and macro/micro averaged precision, recall, and F1-score.
        """
        self._initialize_csv(filename, ["Test Class 0 Precision", "Test Class 0 Recall", "Test Class 0 F1", "Test Class 1 Precision", "Test Class 1 Recall", "Test Class 1 F1", "Test Macro Precision", "Test Macro Recall", "Test Macro F1", "Test Micro Precision", "Test Micro Recall", "Test Micro F1"])
        with open(filename, 'a', newline='') as file:
            writer = csv.writer(file)
            writer.writerow([test_metrics[0][0], test_metrics[0][1], test_metrics[0][2],
                             test_metrics[1][0], test_metrics[1][1], test_metrics[1][2],
                             test_metrics[2], test_metrics[3], test_metrics[4],
                             test_metrics[5], test_metrics[6], test_metrics[7]])
