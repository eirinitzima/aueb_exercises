import torch
from torchtext.datasets import IMDB
from torchtext.data.utils import get_tokenizer
from torch.utils.data import Dataset, DataLoader
import random

# Load vocabulary from file
with open("vocab.txt", "r", encoding="utf-8") as f:
    vocab = [line.strip() for line in f.readlines()]

# Create a word-to-index mapping
word_to_idx = {word: idx for idx, word in enumerate(vocab)}

# Define the IMDB binary classification dataset
class IMDBBinaryDataset(Dataset):
    def __init__(self, dataset, word_to_idx):
        self.tokenizer = get_tokenizer("basic_english")
        self.word_to_idx = word_to_idx
        self.data = []
        self.labels = []
        
        for label, text in dataset[:5000]:  # Limit to the first 5000 samples
            
            # Ensure correct label transformation (convert to 0 or 1)
            label = str(label).strip().lower()
            label = int(label)  # Ensure integer format
            if label == 2:
                self.labels.append(1)  # Convert label 2 to 1 (positive sample)
            elif label == 1:
                self.labels.append(0)  # Convert label 1 to 0 (negative sample)
            else:
                print(f" Unexpected label value: {label}")  # Debugging message

            # Convert text into a binary vector representation
            tokens = set(self.tokenizer(text))
            vector = torch.zeros(len(word_to_idx))
            for word in tokens:
                if word in word_to_idx:
                    vector[word_to_idx[word]] = 1
            self.data.append(vector)

        
        self.data = torch.stack(self.data)
        self.labels = torch.tensor(self.labels, dtype=torch.float32)
    
    def __len__(self):
        return len(self.data)
    
    def __getitem__(self, idx):
        return self.data[idx], self.labels[idx]

# Load and shuffle the dataset
train_data = list(IMDB(split="train"))  # Load entire dataset
random.shuffle(train_data)  # Shuffle for balanced distribution
train_data = train_data[:5000]  # Select 5000 random samples

# Create the dataset
train_dataset = IMDBBinaryDataset(train_data, word_to_idx)



# Save the processed dataset
torch.save(train_dataset, "train_dataset.pt")
print(f" Successfully saved {len(train_dataset)} samples in binary format.")
