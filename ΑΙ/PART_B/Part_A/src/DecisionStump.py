import numpy as np
from sklearn.feature_selection import mutual_info_classif

class DecisionStump:
    def __init__(self):
        """
        Initializes the Decision Stump classifier.
        """
        self.selected_feature = None
        self.threshold = None
        self.polarity = None  # 1 if predicting class 1 when feature >= threshold, otherwise -1
        self.alpha = None  # Stump weight in AdaBoost
    
    def train(self, feature_matrix, labels, sample_weights):
        """
        Trains the Decision Stump by selecting the best feature based on Information Gain.
        
        :param feature_matrix: (numpy.ndarray) Feature matrix (samples x features)
        :param labels: (numpy.ndarray) Target labels (0 or 1)
        :param sample_weights: (numpy.ndarray) Weights assigned to each sample by AdaBoost
        """
        num_features = feature_matrix.shape[1]
        
        # Compute Information Gain using sklearn
        ig_scores = mutual_info_classif(feature_matrix, labels, discrete_features=True)
        self.selected_feature = np.argmax(ig_scores)  # Choose the feature with the highest IG
        
        # Determine best threshold and polarity for classification
        feature_values = feature_matrix[:, self.selected_feature]
        sorted_indices = np.argsort(feature_values)
        feature_values, labels, sample_weights = feature_values[sorted_indices], labels[sorted_indices], sample_weights[sorted_indices]
        
        # if all the feature values are the same, ingore it
        if len(np.unique(feature_values)) == 1:
            return 
        
        min_error = float('inf')
        best_threshold = None
        best_polarity = None
        
        #N candidate thresholds 
        num_thresholds = 10
        threshold_candidates = np.linspace(np.min(feature_values), np.max(feature_values), num_thresholds)

        for threshold in threshold_candidates:
            for polarity in [1, -1]:
                predictions = np.ones_like(labels)
                if polarity == 1:
                    predictions[feature_values < threshold] = 0
                else:
                    predictions[feature_values >= threshold] = 0
                
                error = np.sum(sample_weights * (predictions != labels))
                
                if error < min_error:
                    min_error = error
                    best_threshold = threshold
                    best_polarity = polarity
        
        self.threshold = best_threshold
        self.polarity = best_polarity
    
    def predict(self, feature_matrix):
        """
        Makes predictions using the trained Decision Stump.
        
        :param feature_matrix: (numpy.ndarray) Feature matrix (samples x features)
        :return: Predicted labels (numpy.ndarray)
        """
        feature_values = feature_matrix[:, self.selected_feature]
        predictions = np.ones(feature_values.shape)
        if self.polarity == 1:
            predictions[feature_values < self.threshold] = 0
        else:
            predictions[feature_values >= self.threshold] = 0
        return predictions.astype(int)
