package lit.amida.btipcomm

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerActivity : AppCompatActivity() {
    var textMessage: TextView? = null
    var btSrvSoc: BluetoothServerSocket? = null
    var btSoc: BluetoothSocket? = null
    var btDis: DataInputStream? = null
    var ipSrvSoc: ServerSocket? = null
    var ipSoc: Socket? = null
    var ipDis: DataInputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        textMessage = findViewById(R.id.text_message)
        val textIp: TextView = findViewById(R.id.text_ip)

        textIp.text = ipToString( (applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress )

        lifecycleScope.launchWhenResumed { startBtSrv() }
        lifecycleScope.launchWhenResumed { startIpSrv() }
    }

    private suspend fun startBtSrv() = withContext(Dispatchers.IO) {
        try {
            val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            btSrvSoc = btAdapter.listenUsingRfcommWithServiceRecord("BtIpComm", UUID.fromString(STR_UUID))

            while (true) {
                btSoc = btSrvSoc?.accept()
                btDis = DataInputStream(BufferedInputStream(btSoc?.inputStream))
                try {
                    while (true) {
                        val msg = btDis?.readUTF()
                        withContext(Dispatchers.Main) {
                            textMessage?.text = "$msg from Bluetooth"
                        }
                    }
                } catch (e: Exception) {
                } finally {
                    btDis?.close()
                    btSoc?.close()
                }
            }
        }catch (e: Exception){}
    }

    private suspend fun startIpSrv() = withContext(Dispatchers.IO){
        try {
            ipSrvSoc = ServerSocket(PORT)
            ipSrvSoc?.reuseAddress = true

            while (true) {
                ipSoc = ipSrvSoc?.accept()
                ipDis = DataInputStream(BufferedInputStream(ipSoc?.inputStream))

                try {
                    while (true) {
                        val msg = ipDis?.readUTF()
                        withContext(Dispatchers.Main) {
                            textMessage?.text = "$msg from Wi-Fi"
                        }
                    }
                } catch (e: Exception) {
                } finally {
                    ipDis?.close()
                    ipSoc?.close()
                }
            }
        }catch (e: Exception){}
    }

    private fun ipToString(i: Int): String {
        return "${i and 0xFF}.${i shr 8 and 0xFF}.${i shr 16 and 0xFF}.${i shr 24 and 0xFF}"
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            btDis?.close()
            btSoc?.close()
            btSrvSoc?.close()
            ipDis?.close()
            ipSoc?.close()
            ipSrvSoc?.close()
        }catch (e: Exception){}
    }
}