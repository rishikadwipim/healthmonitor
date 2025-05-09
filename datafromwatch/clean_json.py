import json
import pandas as pd

input_files = ['sensor_data1.json', 'sensor_data2.json', 'sensor_data3.json', 'sensor_data4.json']
cleaned_data = []

# Read and clean the data
for file in input_files:
    with open(file, 'r') as f:
        data = json.load(f)
        
        # Filter out entries where value == 0.0
        cleaned = [record for record in data if record.get('value', 1) != 0.0]
        
        print(f"{file}: Removed {len(data) - len(cleaned)} records with value == 0.0")
        cleaned_data.extend(cleaned)

# Convert to DataFrame for visualization or ML
df = pd.DataFrame(cleaned_data)
print(f"\nTotal cleaned records: {len(df)}")
print(df.head())

# Save cleaned data as CSV
df.to_csv('cleaned_combined.csv', index=False)

print("\nData saved as 'cleaned_combined.csv'")
