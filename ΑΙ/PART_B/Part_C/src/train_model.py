import os
import torch
import torch.nn as nn
import torch.optim as optim
import matplotlib.pyplot as plt
import random
from torch.utils.data import Dataset, DataLoader, random_split
from torchtext.data.utils import get_tokenizer
from torchtext.datasets import IMDB
from sklearn.metrics import precision_score, recall_score, f1_score
import pandas as pd
from sklearn.metrics import classification_report

# **class Focal Loss**
class FocalLoss(nn.Module):
    def __init__(self, alpha=0.25, gamma=2.0):
        super(FocalLoss, self).__init__()
        self.alpha = alpha
        self.gamma = gamma
        self.bce = nn.BCEWithLogitsLoss(reduction='none')

    def forward(self, inputs, targets):
        bce_loss = self.bce(inputs, targets)
        pt = torch.exp(-bce_loss)  
        focal_loss = self.alpha * (1 - pt) ** self.gamma * bce_loss
        return focal_loss.mean()

# **class IMDBBinaryDataset**
class IMDBBinaryDataset(Dataset):
    def __init__(self, dataset, word_to_idx):
        self.tokenizer = get_tokenizer("basic_english")
        self.word_to_idx = word_to_idx
        self.data = []
        self.labels = []
        
        for label, text in dataset:
            tokens = set(self.tokenizer(text))
            vector = torch.zeros(len(word_to_idx))
            for word in tokens:
                if word in word_to_idx:
                    vector[word_to_idx[word]] = 1
            self.data.append(vector)

            label = int(label)
            self.labels.append(1 if label == 2 else 0)

        self.data = torch.stack(self.data)
        self.labels = torch.tensor(self.labels, dtype=torch.float32)
    
    def __len__(self):
        return len(self.data)
    
    def __getitem__(self, idx):
        return self.data[idx], self.labels[idx]

# **create or load test dataset**
if not os.path.exists("test_dataset.pt"):
    print("create test dataset...")

    word_to_idx = torch.load("word_to_idx.pt")

    test_data = list(IMDB(split="test"))  
    random.shuffle(test_data)  

    test_dataset = IMDBBinaryDataset(test_data, word_to_idx)

    torch.save((test_dataset.data, test_dataset.labels), "test_dataset.pt")
    print(" Test dataset saved successfully!")
else:
    print(" Test dataset already exists.")

# **load the test dataset**
test_data, test_labels = torch.load("test_dataset.pt")
test_dataset = list(zip(test_data, test_labels))  
print(" The test dataset loaded successfully!")

# **load training dataset**
full_dataset = torch.load("train_dataset.pt")
train_size = int(0.8 * len(full_dataset))  
val_size = len(full_dataset) - train_size
train_dataset, val_dataset = random_split(full_dataset, [train_size, val_size])

train_loader = DataLoader(train_dataset, batch_size=64, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=64)

# **define the Stacked BiRNN**
class StackedBiRNN(nn.Module):
    def __init__(self, vocab_size, hidden_dim=256, num_layers=2, bidirectional=True, dropout=0.3):
        super(StackedBiRNN, self).__init__()
        self.rnn = nn.LSTM(input_size=vocab_size,  
                           hidden_size=hidden_dim,
                           num_layers=num_layers,
                           bidirectional=bidirectional,
                           batch_first=True,
                           dropout=dropout)
        self.global_max_pooling = nn.AdaptiveMaxPool1d(1)
        self.fc = nn.Linear(hidden_dim * 2 if bidirectional else hidden_dim, 1)
    
    def forward(self, x):
        x = x.unsqueeze(1)  
        rnn_out, _ = self.rnn(x)
        rnn_out = self.global_max_pooling(rnn_out.permute(0, 2, 1)).squeeze(2)
        out = self.fc(rnn_out)
        return out  

# **define optimizer and loss function**
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = StackedBiRNN(len(full_dataset[0][0])).to(device)

pos_weight = torch.tensor([1.3]).to(device) 
criterion = nn.BCEWithLogitsLoss(pos_weight=pos_weight)
 
optimizer = optim.Adam(model.parameters(), lr=0.0005, weight_decay=1e-5) 

# **train the model **
EPOCHS = 10
train_losses, val_losses = [], []

for epoch in range(EPOCHS):
    total_train_loss = 0.0
    total_val_loss = 0.0
    model.train()
    
    for inputs, labels in train_loader:
        inputs, labels = inputs.to(device), labels.to(device)
        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, labels.unsqueeze(1))
        loss.backward()
        optimizer.step()
        total_train_loss += loss.item()
    
    avg_train_loss = total_train_loss / len(train_loader)
    train_losses.append(avg_train_loss)

    model.eval()
    with torch.no_grad():
        for inputs, labels in val_loader:
            inputs, labels = inputs.to(device), labels.to(device)
            outputs = model(inputs)
            loss = criterion(outputs, labels.unsqueeze(1))
            total_val_loss += loss.item()

    avg_val_loss = total_val_loss / len(val_loader)
    val_losses.append(avg_val_loss)

    print(f"Epoch [{epoch+1}/{EPOCHS}], Training Loss: {avg_train_loss:.4f}, Validation Loss: {avg_val_loss:.4f}")

# **Evaluate the Test Set**
test_loader = DataLoader(test_dataset, batch_size=64, shuffle=False)

y_true, y_pred = [], []

model.eval()
with torch.no_grad():
    for inputs, labels in test_loader:
        inputs, labels = inputs.to(device), labels.to(device)
        outputs = model(inputs)
        predictions = (outputs.squeeze() > 0.5).float()  
        y_true.extend(labels.cpu().numpy())
        y_pred.extend(predictions.cpu().numpy())


report = classification_report(y_true, y_pred, target_names=["Negative (0)", "Positive (1)"], output_dict=True)

metrics_df = pd.DataFrame(report).transpose()

metrics_df = metrics_df.drop(index="accuracy", errors="ignore")
metrics_df = metrics_df.drop(columns=["support"], errors="ignore")

print("\nClassification Report:")
print(metrics_df)


# **diagrams**
plt.figure(figsize=(10, 5))
plt.subplot(1, 2, 1)
plt.plot(range(1, EPOCHS + 1), train_losses, marker='o', label='Training Loss')
plt.plot(range(1, EPOCHS + 1), val_losses, marker='o', label='Validation Loss')
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Training & Validation Loss')
plt.legend()


plt.tight_layout()
plt.show()
