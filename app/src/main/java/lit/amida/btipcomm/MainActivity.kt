package lit.amida.btipcomm

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

const val STR_UUID: String = "5E7B99D0-F404-4425-8125-98A2265B4333"
const val PORT: Int = 55913

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parent: ConstraintLayout = findViewById(R.id.constraint_layout)
        val buttonServer: Button = findViewById(R.id.button_server)
        val buttonClient: Button = findViewById(R.id.button_client)

        val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val wifiManager: WifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        var checkIf: Boolean = true

        if(!btAdapter.isEnabled){
            Snackbar.make(parent, "Bluetoothが無効", Snackbar.LENGTH_SHORT).show()
            checkIf = false
        }

        if(!wifiManager.isWifiEnabled){
            Snackbar.make(parent, "Wi-Fiが無効", Snackbar.LENGTH_SHORT).show()
            checkIf = false
        }

        if(wifiManager.connectionInfo.ipAddress == 0){
            Snackbar.make(parent, "IPアドレスが無効", Snackbar.LENGTH_SHORT).show()
            checkIf = false
        }

        if(!checkIf) {
            buttonServer.isEnabled = false
            buttonClient.isEnabled = false
        }

        buttonServer.setOnClickListener {
            val intent: Intent = Intent(this, ServerActivity::class.java)
            startActivity(intent)
        }

        buttonClient.setOnClickListener {
            val intent: Intent = Intent(this, ClientActivity::class.java)
            startActivity(intent)
        }
    }
}