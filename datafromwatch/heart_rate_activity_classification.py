import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import matplotlib.pyplot as plt
import seaborn as sns

# Load data
file_path = r"C:\Data Science\Spring 2025\datafromwatch\cleaned_combined.csv"
df = pd.read_csv(file_path)

# Select relevant columns and drop missing values
df = df[['timestamp', 'value']].dropna(subset=['value'])

# Create activity labels based on heart rate
df['activity'] = 'Normal'
df.loc[df['value'] > 100, 'activity'] = 'High Intensity'
df.loc[(df['value'] >= 80) & (df['value'] <= 90), 'activity'] = 'Normal'
df.loc[df['value'] < 60, 'activity'] = 'Low Intensity'

# Prepare features and target
X = df[['value']]
y = df['activity']

# Split and scale data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)

# Train classifier
model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train_scaled, y_train)

# Evaluate
y_pred = model.predict(X_test_scaled)
print("Accuracy:", accuracy_score(y_test, y_pred))
print("\nClassification Report:\n", classification_report(y_test, y_pred))

# Confusion matrix
cm = confusion_matrix(y_test, y_pred)
plt.figure(figsize=(8, 6))
sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', xticklabels=model.classes_, yticklabels=model.classes_)
plt.xlabel('Predicted')
plt.ylabel('Actual')
plt.title('Confusion Matrix')
plt.tight_layout()
plt.show()
