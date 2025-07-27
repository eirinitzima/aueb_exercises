import numpy as np
import random
from sklearn.tree import DecisionTreeClassifier
from sklearn.utils import resample
from scipy.stats import mode

class RandomForest:
    def __init__(self, n_estimators, max_depth, max_features):
        """
        Initializes the Random Forest classifier using ID3 decision trees.

        Parameters:
        - n_estimators (int): Number of trees in the forest.
        - max_depth (int): Maximum depth of each tree.
        - max_features (float): Percentage of features considered per split.
        """
        self.n_estimators = n_estimators
        self.max_depth = max_depth 
        self.max_features = max_features
        self.trees = []

    def fit_incremental(self, X, y, batch_size):
        """Adds new trees without deleting the old ones."""
        for _ in range(batch_size):
            X_bootstrap, y_bootstrap = resample(X, y)
            tree = DecisionTreeClassifier(criterion='entropy', max_depth=self.max_depth, max_features=self.max_features)
            tree.fit(X_bootstrap, y_bootstrap)
            self.trees.append(tree)

        # saves only the last n_estimators trees
        if len(self.trees) > self.n_estimators:
            self.trees = self.trees[-self.n_estimators:]

    def predict(self, X):
        """Predicts labels using majority voting."""
        predictions = np.array([tree.predict(X) for tree in self.trees])
        majority_votes, _ = mode(predictions, axis=0, keepdims=True)
        return majority_votes.ravel()

