import matplotlib.pyplot as plt
import pandas as pd

# Load and clean data
file_path = r"C:\Data Science\Spring 2025\datafromwatch\cleaned_combined.csv"
df = pd.read_csv(file_path)

# Convert timestamp and filter for heart rate data
df['timestamp'] = pd.to_datetime(df['timestamp'], unit='ms')
heart_rate_df = df[df['type'] == 'heart_rate'].copy()
heart_rate_df = heart_rate_df.dropna(subset=['value'])
heart_rate_df = heart_rate_df.sort_values('timestamp')

# Plot
plt.figure(figsize=(12, 6))
plt.plot(heart_rate_df['timestamp'], heart_rate_df['value'], color='red', marker='o', linestyle='-')
plt.title('Heart Rate Over Time')
plt.xlabel('Time')
plt.ylabel('Heart Rate (BPM)')
plt.grid(True)
plt.tight_layout()
plt.show()
