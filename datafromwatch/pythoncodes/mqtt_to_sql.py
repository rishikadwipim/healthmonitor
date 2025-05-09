import json
import datetime
import psycopg2
import paho.mqtt.client as mqtt

# Load credentials from config.json
with open("config.json", "r") as f:
    config = json.load(f)

pg_conf = config["postgres"]
mqtt_conf = config["mqtt"]

# PostgreSQL connection
conn = psycopg2.connect(
    dbname=pg_conf["dbname"],
    user=pg_conf["user"],
    password=pg_conf["password"],
    host=pg_conf["host"],
    port=pg_conf["port"]
)

cursor = conn.cursor()

# Create table if not exists
cursor.execute("""
CREATE TABLE IF NOT EXISTS sensor_data (
    id SERIAL PRIMARY KEY,
    type TEXT NOT NULL,
    value FLOAT NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
""")
conn.commit()

# MQTT setup
mqtt_client = mqtt.Client()
mqtt_client.username_pw_set(mqtt_conf["username"], mqtt_conf["password"])
mqtt_client.tls_set()

def on_message(client, userdata, msg):
    try:
        payload_str = msg.payload.decode()
        data = json.loads(payload_str)

        # Parse timestamp
        timestamp_ms = int(data.get("timestamp", datetime.datetime.utcnow().timestamp() * 1000))
        time_obj = datetime.datetime.utcfromtimestamp(timestamp_ms / 1000)

        if msg.topic == "wearos/heart_rate":
            value = float(data["value"])
            sensor_type = "heart_rate"
            print(f"📡 Heart Rate: {value} bpm at {time_obj.isoformat()}")

        elif msg.topic == "wearos/steps":
            value = float(data["value"])
            sensor_type = "steps"
            print(f"📡 Steps: {value} at {time_obj.isoformat()}")

        else:
            print(f"⚠️ Unknown topic: {msg.topic}")
            return

        # Insert into PostgreSQL
        cursor.execute(
            "INSERT INTO sensor_data (type, value, timestamp) VALUES (%s, %s, %s);",
            (sensor_type, value, time_obj)
        )
        conn.commit()
        print("✅ Data written to PostgreSQL")

    except Exception as e:
        print("❌ Error:", e)

mqtt_client.on_message = on_message
mqtt_client.connect(mqtt_conf["host"], mqtt_conf["port"], 60)
mqtt_client.subscribe("wearos/heart_rate")
mqtt_client.subscribe("wearos/steps")
mqtt_client.loop_forever()
