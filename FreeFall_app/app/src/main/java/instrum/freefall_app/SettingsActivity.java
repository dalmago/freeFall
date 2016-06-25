package instrum.freefall_app;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static boolean deviceSelected = false;
    public static BluetoothDevice selectedDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner pairedBlthSpinner = (Spinner) findViewById(R.id.pairedBlth);


        // Create list of devices names, to show to the user
        List<String> devicesName = new ArrayList<>();
        for (BluetoothDevice dev: MainActivity.devices)
            devicesName.add(dev.getName());

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, devicesName);


        pairedBlthSpinner.setOnItemSelectedListener(this);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pairedBlthSpinner.setAdapter(adapterSpinner);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedDevice = MainActivity.devices.get(position);
        deviceSelected = true;
        Toast.makeText(getApplicationContext(), selectedDevice.getName() + " selected",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedDevice = null;
        deviceSelected = false;
    }
}
