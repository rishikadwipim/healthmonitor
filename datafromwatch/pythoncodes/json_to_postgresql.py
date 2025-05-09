import json
import datetime
import psycopg2
import os

# PostgreSQL connection setup
conn = psycopg2.connect(
    dbname="healthmonitor_ur2t",
    user="healthmonitor_ur2t_user",
    password="c4pev9r7llbKE1u4Lre2QJMm37MKP6EH",
    host="dpg-d0dnsjruibrs73d04d1g-a.oregon-postgres.render.com",
    port="5432"
)
cursor = conn.cursor()

# Folder or list of files
json_files = ["sensor_data4.json"]

for filename in json_files:
    with open(filename, "r") as file:
        try:
            data_list = json.load(file)  # assuming each file contains a list of JSON objects

            for record in data_list:
                # Convert timestamp
                timestamp_ms = int(record["timestamp"])
                time_obj = datetime.datetime.utcfromtimestamp(timestamp_ms / 1000)

                # Prepare values
                sensor_type = record["type"]
                value = float(record["value"])

                # Insert into DB
                cursor.execute(
                    "INSERT INTO sensor_data (type, value, timestamp) VALUES (%s, %s, %s);",
                    (sensor_type, value, time_obj)
                )
        except Exception as e:
            print(f"❌ Error processing {filename}: {e}")

# Commit and close
conn.commit()
cursor.close()
conn.close()

print("✅ All JSON files inserted into PostgreSQL.")
