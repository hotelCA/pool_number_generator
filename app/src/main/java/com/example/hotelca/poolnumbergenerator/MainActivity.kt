package com.example.hotelca.poolnumbergenerator

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.Toast
import java.util.Random
import android.util.Log
import android.support.v4.app.ActivityCompat
import com.google.android.gms.nearby.connection.Strategy.P2P_STAR
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import kotlin.text.Charsets.UTF_8


class MainActivity : AppCompatActivity() {

    private var playButton: Button? = null
    private var opponentEndpointId: String = ""

    val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 5
    val STRATEGY = P2P_STAR
    val TAG = "ETM"
    val USERNAME = "Anh's OnePlus3"
    var connectionsClient: ConnectionsClient? = null
    var numOfPlayers = 2
    var numbersPerPlayer = 3
    var possibleNumbers: IntArray = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                               11, 12, 13, 14, 15)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForPermission()

        connectionsClient = Nearby.getConnectionsClient(this)

        playButton = findViewById(R.id.playButton)

        playButton!!.setOnClickListener {
            generateNumbers()
        }
    }

    private fun checkForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    toast("Permission Granted!")

                } else {

                    toast("Permission Denied!")
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun startAdvertising() {
        connectionsClient!!.startAdvertising(
                USERNAME,
                applicationContext.packageName,
                connectionLifecycleCallback,
                AdvertisingOptions(STRATEGY))
                .addOnSuccessListener(
                        OnSuccessListener<Void>() {

                        })
                .addOnFailureListener(
                         OnFailureListener() {

                        })
    }

    private fun startDiscovery() {
        connectionsClient!!.startDiscovery(
                USERNAME,
                endpointDiscoveryCallBack,
                DiscoveryOptions(STRATEGY))
                .addOnSuccessListener(
                        OnSuccessListener<Void> {
                            // We're discovering!
                        })
                .addOnFailureListener(
                        OnFailureListener {
                            // We were unable to start discovering.
                        })
    }

    private val endpointDiscoveryCallBack = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(
                endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {

            Log.i(TAG, "onEndpointFound: endpoint found, connecting");
            connectionsClient!!.requestConnection(USERNAME, endpointId, connectionLifecycleCallback);
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: accepting connection")
            connectionsClient!!.acceptConnection(endpointId, payloadCallback)
            //  opponentName = connectionInfo.endpointName // Don't need this
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(TAG, "onConnectionResult: connection successful")
                connectionsClient!!.stopDiscovery()
                connectionsClient!!.stopAdvertising()
                opponentEndpointId = endpointId
            } else {
                Log.i(TAG, "onConnectionResult: connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: disconnected from the opponent")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            var selectedNumbers = String(payload.asBytes()!!, UTF_8)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                // Nothing to do here
            }
        }
    }

    private fun sendPayload(payLoad: IntArray) {
        val bytePayLoad = payLoad.map {x -> x.toByte()}.toByteArray()
        connectionsClient!!.sendPayload(
                opponentEndpointId,
                Payload.fromBytes(bytePayLoad)
        )
    }

    private fun generateNumbers() {

        var totalNumbers = possibleNumbers.size
        val numbersNeeded = numOfPlayers * numbersPerPlayer
        if (numbersNeeded > totalNumbers) {
            toast("Too many numbers")
        }
        var pickedNumbers = IntArray(numbersNeeded)
        fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) +  start

        for (i in 0 until numbersNeeded) {
            var randomNumber = (0..totalNumbers).random()
            pickedNumbers[i] = possibleNumbers[randomNumber]
            swapNumbers(randomNumber, totalNumbers-1)
            totalNumbers--
        }

        for (i in 0 until pickedNumbers.size) {
            Log.i("pickedNumbers", pickedNumbers[i].toString())
        }

        var intArray = intArrayOf(50,50,50)
        var byteArray = intArray.map {x -> x.toByte()}.toByteArray()
        var sum = byteArray[0] + byteArray[1] + byteArray[2]
        Log.i(TAG, sum.toString())
        Log.i(TAG, "----------------")
    }

    private fun swapNumbers(i: Int, j: Int) {
        val temp = possibleNumbers[i]
        possibleNumbers[i] = possibleNumbers[j]
        possibleNumbers[j] = temp
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }


}
