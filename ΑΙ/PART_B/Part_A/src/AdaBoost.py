import numpy as np
from DecisionStump import DecisionStump

class AdaBoost:
    def __init__(self, num_stumps=50):
        """
        Initializes the AdaBoost classifier.
        
        :param num_stumps: Number of weak classifiers (decision stumps) to train.
        """
        self.num_stumps = num_stumps
        self.stumps = []
        self.stump_weights = []
    
    def train(self, feature_matrix, labels):
        """
        Trains the AdaBoost classifier using Decision Stumps.
        
        :param feature_matrix: (numpy.ndarray) Feature matrix (samples x features)
        :param labels: (numpy.ndarray) Binary labels (0 or 1)
        """
        num_samples = feature_matrix.shape[0]
        weights = np.ones(num_samples) / num_samples  # Initialize weights uniformly
        
        for _ in range(self.num_stumps):
            stump = DecisionStump()
            stump.train(feature_matrix, labels, weights)
            
            # Compute weighted error
            predictions = stump.predict(feature_matrix)
            error = np.sum(weights * (predictions != labels)) / np.sum(weights)
            
            # Avoid division by zero and discard weak classifiers with too high error
            if error >= 0.5:
                continue
            
            # Compute stump weight
            alpha = 0.5 * np.log((1 - error) / (error + 1e-10))  # Avoid log(0)
            
            # Update sample weights
            y = 2 * labels - 1  # Convert (0,1) to (-1,1)
            weights *= np.exp(-alpha * y * (2 * predictions - 1))
            weights /= np.sum(weights)  # Normalize weights
            
            self.stumps.append(stump)
            self.stump_weights.append(alpha)
    
    def predict(self, feature_matrix):
        """
        Predicts class labels for given feature vectors using the trained AdaBoost classifier.
        
        :param feature_matrix: (numpy.ndarray) Feature matrix (samples x features)
        :return: Predicted labels (numpy.ndarray)
        """
        num_samples = feature_matrix.shape[0]
        scores = np.zeros(num_samples)
        
        for alpha, stump in zip(self.stump_weights, self.stumps):
            predictions = stump.predict(feature_matrix)
            scores += alpha * (2 * predictions - 1)  # Convert (0,1) to (-1,1)
        
        return (scores >= 0).astype(int)  # if score >= 0, return 1, otherwise 0

