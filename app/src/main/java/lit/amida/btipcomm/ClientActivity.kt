package lit.amida.btipcomm

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.*

class ClientActivity : AppCompatActivity() {
    var btSoc: BluetoothSocket? = null
    var btDos: DataOutputStream? = null
    var ipSoc: Socket? = null
    var ipDos: DataOutputStream? = null
    var btDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val btDeviceList: List<BluetoothDevice>? = btAdapter?.bondedDevices?.toList()
        val deviceNameList:MutableList<String> = mutableListOf()

        val parent: ConstraintLayout = findViewById(R.id.layout_client)
        val editMessage: EditText = findViewById(R.id.edit_message)
        val buttonSelectBtDevice: Button = findViewById(R.id.button_select_bt_device)
        val buttonSendBt: Button = findViewById(R.id.button_send_bt)
        val editIpAddr: EditText = findViewById(R.id.edit_ip_addr)
        val buttonSendIp: Button = findViewById(R.id.button_send_ip)

        btDeviceList?.forEach { deviceNameList.add(it.name) }

        buttonSelectBtDevice.setOnClickListener {
            try{
                btDos?.close()
                btSoc?.close()
            }catch (e: Exception){}

            btSoc = null

            AlertDialog.Builder(this)
                    .setTitle("接続デバイスを選択")
                    .setItems(deviceNameList.toTypedArray()) { _, which ->
                        btDevice = btDeviceList?.get(which)
                        buttonSelectBtDevice.text = deviceNameList[which]
                    }
                    .show()
        }

        editIpAddr.doOnTextChanged { _, _, _, _ ->
            try {
                ipDos?.close()
                ipSoc?.close()
            }catch (e: Exception){}

            ipSoc = null
        }

        buttonSendBt.setOnClickListener {
            if(btDevice == null){
                Snackbar.make(parent, "接続機器を選択してください", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    try{
                        if(btSoc == null) {
                            btSoc = btDevice?.createRfcommSocketToServiceRecord(UUID.fromString(STR_UUID))
                            btSoc?.connect()
                            btDos = DataOutputStream(BufferedOutputStream(btSoc?.outputStream))
                        }
                        btDos?.writeUTF(editMessage.text.toString())
                        btDos?.flush()
                    }catch (e: Exception){
                        try{
                            btDos?.close()
                            btSoc?.close()
                        }catch (e: Exception){}
                        btDos = null
                        btSoc = null
                        withContext(Dispatchers.Main) {
                            Snackbar.make(parent, "Bluetoothでの通信に失敗", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }

        buttonSendIp.setOnClickListener {
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    try {
                        if(ipSoc == null){
                            ipSoc = Socket(editIpAddr.text.toString(), PORT)
                            ipDos = DataOutputStream(BufferedOutputStream(ipSoc?.outputStream))
                        }
                        ipDos?.writeUTF(editMessage.text.toString())
                        ipDos?.flush()
                    }catch (e: Exception){
                        try{
                            ipDos?.close()
                            ipSoc?.close()
                        }catch (e: Exception){}
                        ipSoc = null
                        withContext(Dispatchers.Main) {
                            Snackbar.make(parent, "Wi-Fiでの通信に失敗", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            ipDos?.close()
            ipSoc?.close()
            btDos?.close()
            btSoc?.close()
        }catch (e: Exception){}
    }
}