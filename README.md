HealthMonitor – Wear OS IoT App

Is a lightweight Wear OS app that turns your smartwatch into an IoT device. It tracks heart rate and steps, stores data offline, and syncs it via MQTT to a cloud backend for real-time analysis and visualization.

Features

- Collects heart rate & step count using Wear OS sensors  
- Syncs data via MQTT to a cloud server  
- Stores data locally with Room when offline  
- Exports data as JSON & uploads to PostgreSQL  
- Visualizes metrics in Grafana dashboards  
- Triggers alerts on threshold breaches  

Tech Stack

- Jetpack Compose (Wear OS)  
- Room DB (SQLite)  
- MQTT (HiveMQ/Mosquitto)  
- PostgreSQL  
- Grafana + InfluxDB (optional)  
- Python (data upload scripts)

Structure
healthmonitor/
├── app/ (Wear OS code)
├── datafromwatch/ (JSON, Python scripts)
