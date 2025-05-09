import matplotlib.pyplot as plt
import pandas as pd

# Load and clean data
file_path = r"C:\Data Science\Spring 2025\datafromwatch\cleaned_combined.csv"
df = pd.read_csv(file_path)

# Convert timestamp and filter for heart rate data
df['timestamp'] = pd.to_datetime(df['timestamp'], unit='ms')
heart_rate_df = df[df['type'] == 'heart_rate'].copy()
heart_rate_df = heart_rate_df.dropna(subset=['value'])

# Histogram
plt.figure(figsize=(10, 6))
plt.hist(heart_rate_df['value'], bins=30, color='blue', alpha=0.7)
plt.title('Distribution of Heart Rate')
plt.xlabel('Heart Rate (BPM)')
plt.ylabel('Frequency')
plt.grid(True)
plt.tight_layout()
plt.show()
