package instrum.freefall_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private BluetoothAdapter myBluetoothAdapter;
    private Spinner pairedBlthSpinner;
    private List<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pairedBlthSpinner = (Spinner) findViewById(R.id.pairedBlth);
        pairedBlthSpinner.setOnItemSelectedListener(this);

        configureBluetooth();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Object[] pairedDevices = myBluetoothAdapter.getBondedDevices().toArray();

        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() == 0)
            return;

        devices = new ArrayList<>();
        for (Object obj : pairedDevices){
            devices.add((BluetoothDevice) obj);
        }

        List<String> devicesName = new ArrayList<>();

        for (BluetoothDevice dev : devices){
            devicesName.add(dev.getName());
        }

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, devicesName);

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pairedBlthSpinner.setAdapter(adapterSpinner);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice dev = devices.get(position);
        Toast.makeText(getApplicationContext(), dev.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void configureBluetooth(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Couldn't access Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        if (!myBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1); // Request number 1
        }
    }
}
