import pandas as pd
import matplotlib.pyplot as plt

class MetricsVisualizer:
    """
    A class to visualize training and evaluation metrics from CSV files.

    Methods:
    - plot_learning_curves: Generates and saves separate learning curves for precision, recall, and F1-score.
    - generate_evaluation_table: Saves evaluation metrics as a PNG image.
    """

    def __init__(self, training_csv, evaluation_csv):
        """
        Initializes the visualizer with file paths for training and evaluation CSVs.
        
        Parameters:
        training_csv (str): Path to the CSV file containing training metrics.
        evaluation_csv (str): Path to the CSV file containing evaluation metrics.
        """
        self.training_csv = training_csv
        self.evaluation_csv = evaluation_csv

    def plot_learning_curves(self, base_filename):
        """
        Reads the training metrics CSV and generates three separate learning curves
        for precision, recall, and F1-score on training and development data.
        Saves each plot as a PNG file with the given base filename.

        Parameters:
        base_filename (str): The base filename to use for saving the plots.
        """
        df = pd.read_csv(self.training_csv)

        # Precision plot
        plt.figure(figsize=(10, 6))
        plt.plot(df["Training Size"], df["Train Precision (class 1)"], label="Train Precision", marker='o')
        plt.plot(df["Training Size"], df["Dev Precision (class 1)"], label="Dev Precision", marker='s')
        plt.xlabel("Training Size")
        plt.ylabel("Precision")
        plt.title("Precision Learning Curve")
        plt.legend()
        plt.grid(True)
        plt.savefig(f"{base_filename}_precision.png", dpi=300)
        plt.close()

        # Recall plot
        plt.figure(figsize=(10, 6))
        plt.plot(df["Training Size"], df["Train Recall (class 1)"], label="Train Recall", marker='o')
        plt.plot(df["Training Size"], df["Dev Recall (class 1)"], label="Dev Recall", marker='s')
        plt.xlabel("Training Size")
        plt.ylabel("Recall")
        plt.title("Recall Learning Curve")
        plt.legend()
        plt.grid(True)
        plt.savefig(f"{base_filename}_recall.png", dpi=300)
        plt.close()

        # F1-score plot
        plt.figure(figsize=(10, 6))
        plt.plot(df["Training Size"], df["Train F1 (class 1)"], label="Train F1", marker='o')
        plt.plot(df["Training Size"], df["Dev F1 (class 1)"], label="Dev F1", marker='s')
        plt.xlabel("Training Size")
        plt.ylabel("F1-score")
        plt.title("F1-score Learning Curve")
        plt.legend()
        plt.grid(True)
        plt.savefig(f"{base_filename}_f1.png", dpi=300)
        plt.close()

    def generate_evaluation_table(self, output_file="evaluation_metrics.png"):
        """
        Reads the evaluation metrics CSV and saves a formatted table
        of precision, recall, and F1-score for each class and macro/micro averages as a PNG image.

        Parameters:
        output_file (str): Filename to save the evaluation table as an image.
        """
        df = pd.read_csv(self.evaluation_csv)
        fig, ax = plt.subplots(figsize=(10, 4))
        ax.axis("tight")
        ax.axis("off")
        ax.table(cellText=df.values, colLabels=df.columns, cellLoc='center', loc='center')
        plt.savefig(output_file, dpi=300)
        plt.close()