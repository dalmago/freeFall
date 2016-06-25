package instrum.freefall_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button connectButton;
    private final int BLTHREQUESTCODE = 1;
    private BluetoothAdapter myBluetoothAdapter;
    public static boolean bluetoothInitialized = false;
    public static List<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectButton = (Button) findViewById(R.id.connectButton);

        configureBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                if (!bluetoothInitialized){
                    if (myBluetoothAdapter != null) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, BLTHREQUESTCODE); // Request number 1
                    } else{
                        Toast.makeText(getApplicationContext(), "Couldn't access Bluetooth",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! bluetoothInitialized)
            return;

        Object[] pairedDevices = myBluetoothAdapter.getBondedDevices().toArray();

        devices = new ArrayList<>();

        if(pairedDevices.length == 0){
            Toast.makeText(getApplicationContext(), "You must first pair the bluetooth device",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        for (Object dev : pairedDevices){
            devices.add((BluetoothDevice)dev);
        }
    }

    // Initialize the Bluetooth configuration
    private void configureBluetooth(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Couldn't access Bluetooth",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!myBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLTHREQUESTCODE); // Request number 1
        } else{
            bluetoothInitialized = true;
        }
    }

    // Result of bluetooth asking for permission
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode){
                case BLTHREQUESTCODE:
                    if (resultCode == RESULT_OK)
                        bluetoothInitialized = true;
                    else
                        Toast.makeText(getApplicationContext(), "Please turn the Bluetooth on",
                                Toast.LENGTH_LONG).show();
                    break;
            }
    }

    public void connectBlth(View v){

    }
}
