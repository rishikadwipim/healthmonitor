import android.util.Log
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.*


class MqttService(
    private val serverUri: String = System.getenv("MQTT_SERVER_URI") ?: "default_server_uri",
    private val clientId: String = MqttClient.generateClientId(),
    private val username: String = System.getenv("MQTT_USERNAME") ?: "default_username",
    private val password: String = System.getenv("MQTT_PASSWORD") ?: "default_password"
)
 {
    private val mqttClient: MqttAsyncClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())

    private val options = MqttConnectOptions().apply {
        isCleanSession = true
        isAutomaticReconnect = true
        userName = username
        password = this@MqttService.password.toCharArray()
        connectionTimeout = 1000
        keepAliveInterval = 3600
    }

    init {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.w("MQTT", "Connection lost: ${cause?.message}")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message received: $topic -> ${message.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "Delivery complete")
            }
        })
    }

    fun connect(onConnected: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
        if (mqttClient.isConnected) {
            Log.d("MQTT", "Already connected.")
            onConnected()
            return
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connected to HiveMQ (AsyncClient)")
                    onConnected()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Connection failed: ${exception?.message}")
                    exception?.let { onFailure(it) }
                }
            })
        } catch (e: MqttException) {
            Log.e("MQTT", "Exception while connecting: ${e.message}")
            onFailure(e)
        }
    }

    fun publish(topic: String, message: String) {
        if (!mqttClient.isConnected) {
            Log.e("MQTT", "Publish failed: Client is not connected")
            return
        }

        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                qos = 1
                isRetained = false
            }
            mqttClient.publish(topic, mqttMessage)
            Log.d("MQTT", "Published to $topic: $message")
        } catch (e: MqttException) {
            Log.e("MQTT", "Publish exception: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (mqttClient.isConnected) {
                mqttClient.disconnect()
                Log.d("MQTT", "Disconnected")
            }
        } catch (e: MqttException) {
            Log.e("MQTT", "Disconnect failed: ${e.message}")
        }
    }
}
