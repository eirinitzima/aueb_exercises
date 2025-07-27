import numpy as np
import os
import re
from collections import Counter
from sklearn.feature_selection import mutual_info_classif
from nltk.tokenize import word_tokenize

class IMDBPreprocessor:
    def __init__(self, train_path, test_path, n, k, m):
        self.train_path = train_path
        self.test_path = test_path
        self.n = n  # Number of most frequent words to remove
        self.k = k  # Number of least frequent words to remove
        self.m = m  # Number of features to keep
        self.vocabulary = None
        self.feature_matrix = None
        self.labels = None

    def load_dataset(self):
        dataset = {"train": [], "test": []}
        for split, path in zip(["train", "test"], [self.train_path, self.test_path]):
            for label in ["pos", "neg"]:
                label_dir = os.path.join(path, label)
                for filename in os.listdir(label_dir):
                    with open(os.path.join(label_dir, filename), encoding='utf-8') as f:
                        dataset[split].append((f.read().lower(), 1 if label == "pos" else 0))
        np.random.shuffle(dataset["train"])
        np.random.shuffle(dataset["test"])
        return dataset

    def preprocess(self):
        dataset = self.load_dataset()
        train_data = dataset["train"]
        test_data = dataset["test"]
        
        train_size = 20000
        dev_size = 5000
        training_data = train_data[:train_size]
        development_data = train_data[train_size:train_size + dev_size]

        # Step 1: Vocabulary Construction
        vocab = Counter()
        for text, _ in training_data:
            words = word_tokenize(re.sub(r'[^a-zA-Z]', ' ', text))
            vocab.update(words)

        print(f"Initial vocabulary size: {len(vocab)}")

        sorted_vocab = vocab.most_common()
        filtered_vocab = [word for word, freq in sorted_vocab[self.k: -self.n]]

        print(f"Vocabulary size after filtering: {len(filtered_vocab)}")

        # Step 2:  Vocabulary index creation 
        vocab_index = {word: idx for idx, word in enumerate(filtered_vocab)}

        # Step 3: Converting training set into feature matrix
        train_matrix = np.zeros((len(training_data), len(filtered_vocab)), dtype=np.uint8)
        train_labels = np.zeros(len(training_data), dtype=int)

        for i, (text, label) in enumerate(training_data):
            words = set(word_tokenize(re.sub(r'[^a-zA-Z]', ' ', text)))
            for word in words:
                if word in vocab_index:
                    train_matrix[i, vocab_index[word]] = 1
            train_labels[i] = label

        # Information Gain Calculation
        ig_scores = mutual_info_classif(train_matrix, train_labels, discrete_features=True)

        #Top m feature selection 
        top_indices = np.argsort(ig_scores)[-self.m:]
        self.vocabulary = [filtered_vocab[i] for i in top_indices]

        # Updating vocab_index according to the final vocabulary
        vocab_index = {word: idx for idx, word in enumerate(self.vocabulary)}

        #Rearrange training set with final features τελικά
        train_matrix = train_matrix[:, top_indices]
        self.feature_matrix = train_matrix
        self.labels = train_labels

        # Step 4: Converting development set into feature matrix
        dev_matrix = np.zeros((len(development_data), len(self.vocabulary)), dtype=np.uint8)
        dev_labels = np.zeros(len(development_data), dtype=int)

        for i, (text, label) in enumerate(development_data):
            words = set(word_tokenize(re.sub(r'[^a-zA-Z]', ' ', text)))
            for word in words:
                if word in vocab_index:
                    dev_matrix[i, vocab_index[word]] = 1
            dev_labels[i] = label

        # Step 5: Converting test set into feature matrix
        test_matrix = np.zeros((len(test_data), len(self.vocabulary)), dtype=np.uint8)
        test_labels = np.zeros(len(test_data), dtype=int)

        for i, (text, label) in enumerate(test_data):
            words = set(word_tokenize(re.sub(r'[^a-zA-Z]', ' ', text)))
            for word in words:
                if word in vocab_index:
                    test_matrix[i, vocab_index[word]] = 1
            test_labels[i] = label

        #Concatenate training + development + test sets
        self.feature_matrix = np.vstack((self.feature_matrix, dev_matrix, test_matrix))
        self.labels = np.concatenate((self.labels, dev_labels, test_labels))
