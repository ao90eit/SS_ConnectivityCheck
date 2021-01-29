package com.aoinc.ss_connectivitycheck

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private val networkRequestBuilder = NetworkRequest.Builder()
    private var deviceHasConnectivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        deviceHasConnectivity = isConnected()
        
        Log.d("TAG_X", deviceHasConnectivity.toString())
    }

    // register network event callback for
    override fun onResume() {
        super.onResume()

        /* This callback is only *kind of* useful.
        *
        * It's great to register network change events so we can respond to them,
        * but it's a simple, reactive system that does not correctly explain
        * the state of connectivity. For example, onLost is called when any type of network is
        * lost - e.g. wifi or cellular - which means that one can be lost, but the other still
        * available. So in onLost (at least) we can't assume there is no connection and must check
        * the connectivity status directly. Perhaps same for onAvailable. (Also gets the
        * type of connectivity this way.)
        *
        * Also worth noting - onCapabilitiesChanged does not fire when a network is lost.
        * Not sure what makes this useful.
        */
        connectivityManager.registerNetworkCallback(
            networkRequestBuilder.build(),
            object: ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("TAG_X", "network has become available")
                    deviceHasConnectivity = isConnected()
                    Log.d("TAG_X", deviceHasConnectivity.toString())
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    Log.d("TAG_X", "network capabilities changed")
                    deviceHasConnectivity = isConnected()
                    Log.d("TAG_X", deviceHasConnectivity.toString())
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("TAG_X", "network has been lost")
                    deviceHasConnectivity = isConnected()
                    Log.d("TAG_X", deviceHasConnectivity.toString())
                }
            }
        )
    }

    // unregister network event callback for garbage collection
    override fun onPause() {
        super.onPause()
    }

    // This is perfect for a direct check of the **current state** of connection
    private fun isConnected(): Boolean {
        // if running on newer API...
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val activeNetwork =  connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {    // if running on older API...
            // if null, return false as fallback
            return connectivityManager.activeNetworkInfo?.isConnected ?: return false
        }
    }
}