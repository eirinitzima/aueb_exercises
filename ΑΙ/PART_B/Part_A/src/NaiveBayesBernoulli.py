"""
Implementation of the Bernoulli Naive Bayes classifier for binary classification.
It provides methods for training the model using a binary feature matrix and making
predictions on new data.

"""

import numpy as np

class NaiveBayesBernoulli:
    def __init__(self):
        """
        Initializes the Bernoulli Naive Bayes classifier.
        """
        self.prior_positive = None
        self.prior_negative = None
        self.prob_word_given_positive = None
        self.prob_word_given_negative = None
    
    def train(self, feature_matrix, labels):
        """
        Trains the Bernoulli Naive Bayes model.

        Parameters:
        - feature_matrix (numpy.ndarray): Binary feature matrix of shape (samples x features).
        - labels (numpy.ndarray): Binary array of class labels (0 or 1).
        """
        num_samples, vocabulary_size = feature_matrix.shape
        positive_count = np.sum(labels)
        negative_count = num_samples - positive_count
        
        self.prior_positive = positive_count / num_samples
        self.prior_negative = negative_count / num_samples
        
        smoothing_factor = 1.0  # Laplace smoothing
        
        word_count_positive = np.sum(feature_matrix[labels == 1], axis=0)
        word_count_negative = np.sum(feature_matrix[labels == 0], axis=0)
        
        self.prob_word_given_positive = (word_count_positive + smoothing_factor) / (positive_count + 2 * smoothing_factor)
        self.prob_word_given_negative = (word_count_negative + smoothing_factor) / (negative_count + 2 * smoothing_factor)
    
    def predict(self, feature_vector):
        """
        Predicts class labels for one or more input samples.

        Parameters:
        - feature_matrix (numpy.ndarray): Feature matrix (samples x features) or a single feature vector.

        Returns:
        - numpy.ndarray: Predicted class labels (0 or 1).
        """

        log_prob_positive = np.log(self.prior_positive)
        log_prob_negative = np.log(self.prior_negative)
        min_prob = 1e-9  #  log(0)
        
        # Compute log probabilities for both classes
        log_prob_positive += np.sum(feature_vector * np.log(np.maximum(self.prob_word_given_positive, min_prob)))
        log_prob_positive += np.sum((1 - feature_vector) * np.log(np.maximum(1 - self.prob_word_given_positive, min_prob)))
        
        log_prob_negative += np.sum(feature_vector * np.log(np.maximum(self.prob_word_given_negative, min_prob)))
        log_prob_negative += np.sum((1 - feature_vector) * np.log(np.maximum(1 - self.prob_word_given_negative, min_prob)))
        
        return 1 if log_prob_positive > log_prob_negative else 0
    """""
    def predict_batch(self, feature_matrix):
       
        //Βελτιστοποιημένη μαζική πρόβλεψη για πολλά δείγματα.
        //:param feature_matrix: Μήτρα χαρακτηριστικών (samples x features).
        //:return: Διανυσμα προβλέψεων (0 ή 1).
        
        log_prob_positive = np.log(self.prior_positive) + feature_matrix @ np.log(np.maximum(self.prob_word_given_positive, 1e-9))
        log_prob_negative = np.log(self.prior_negative) + feature_matrix @ np.log(np.maximum(self.prob_word_given_negative, 1e-9))
        
        return (log_prob_positive > log_prob_negative).astype(int)
    """