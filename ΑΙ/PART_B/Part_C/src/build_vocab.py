import torch
import nltk
from collections import Counter
from torchtext.datasets import IMDB
from torchtext.data.utils import get_tokenizer

nltk.download('punkt')

# Load the training dataset
train_data = list(IMDB(split="train"))

# Define the tokenizer
tokenizer = get_tokenizer("basic_english")

# Count the frequency of words in the training dataset
word_counts = Counter()
for label, text in train_data:
    tokens = set(tokenizer(text))  # Using a set to ensure unique words per document
    word_counts.update(tokens)

# **Hyperparameters for vocabulary filtering**
n = 5000  # Number of most frequent words to remove
k = 1000  # Number of least frequent words to remove
m = 5000   # Number of selected words with the highest information gain (placeholder)

# Identify the most and least common words
most_common_words = set([word for word, _ in word_counts.most_common(n)])
least_common_words = set([word for word, _ in word_counts.most_common()[-k:]])

# Create the vocabulary by excluding the most and least frequent words
vocab = [word for word, count in word_counts.items() if word not in most_common_words and word not in least_common_words]

# Limit the vocabulary size to the top 'm' words (based on a placeholder criterion)
vocab = vocab[:m]

# Save the vocabulary to a text file
with open("vocab.txt", "w", encoding="utf-8") as f:
    for word in vocab:
        f.write(word + "\n")

print(f"Vocabulary successfully saved with {len(vocab)} words!")

# **Convert the vocabulary into a dictionary for PyTorch usage**
word_to_idx = {word: idx for idx, word in enumerate(vocab)}

# **Save the vocabulary mapping as a PyTorch tensor**
torch.save(word_to_idx, "word_to_idx.pt")
print("word_to_idx.pt has been successfully saved!")
