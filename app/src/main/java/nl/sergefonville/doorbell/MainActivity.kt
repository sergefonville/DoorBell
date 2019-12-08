package nl.sergefonville.doorbell

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import org.eclipse.paho.android.service.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast
import java.lang.Exception
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import org.eclipse.paho.client.mqttv3.IMqttToken
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: HistoryAdapter
    private lateinit var  mqttAndroidClient: MqttAndroidClient
    private lateinit var configurationProperties: Properties
    private val clientId: String = "DoorBell" + System.currentTimeMillis()
    private lateinit var  serverUri: String
    private lateinit var subscriptionTopic: String
    private lateinit var username: String
    private lateinit var password: CharArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mLayoutManager: LayoutManager  = LinearLayoutManager(this)
        var mRecyclerView: RecyclerView = findViewById(R.id.rings) as RecyclerView
        configurationProperties = Properties()
        var assetManager: AssetManager = getBaseContext().getAssets()
        var propertiesStream: InputStream = assetManager.open("app.properties")
        configurationProperties.load(propertiesStream)

        username = configurationProperties.getProperty("serveruri")
        subscriptionTopic = configurationProperties.getProperty("subscriptiontopic")
        username = configurationProperties.getProperty("username")
        password = configurationProperties.getProperty("password").toCharArray()
        mRecyclerView.layoutManager = mLayoutManager

        mAdapter = HistoryAdapter(ArrayList<String>())
        mRecyclerView.adapter = mAdapter
        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)

        mqttAndroidClient.setCallback(object: MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) {
                    Toast.makeText(applicationContext, "Reconnected to "+ serverUri, Toast.LENGTH_SHORT).show();
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    showToast("Connected to: " + serverURI);
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                mAdapter.add(String(message!!.payload))
            }

            override fun connectionLost(cause: Throwable?) {

            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val connOpts = MqttConnectOptions()
        connOpts.userName = username
        connOpts.password = password
        try {
            mqttAndroidClient.connect(connOpts, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    try {
                        asyncActionToken!!.sessionPresent

                    }
                    catch(e: Exception){

                    }
                    Toast.makeText(applicationContext, "Connected to "+ serverUri + "using client " + clientId, Toast.LENGTH_SHORT).show();
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic();
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT).show();

                }

            })
        }
        catch (e: Exception){e.printStackTrace();}


    }
    fun subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    showToast("Subscribed")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    showToast("Could not subscribe")
                }
            });

        } catch ( ex: MqttException){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
    }
}
