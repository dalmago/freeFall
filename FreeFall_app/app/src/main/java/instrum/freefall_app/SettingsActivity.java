package instrum.freefall_app;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static boolean deviceSelected = false;
    private static BluetoothDevice selectedDevice = null;

    private CheckBox chck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        chck = (CheckBox)findViewById(R.id.settingsDefault);

        List<BluetoothDevice> devices = MainActivity.getDevicesList();
        BluetoothDevice defDev = null;
        if (DefaultDevice.isDefaultDeviceAvailable()){
            chck.setChecked(true);
            String s = DefaultDevice.getDefaultDevice();
            for (BluetoothDevice dev : devices) {
                if (dev.getName().equals(s)) {
                    defDev = dev;
                    break;
                }
            }
            if (defDev != null){
                devices.remove(defDev);
                devices.add(0, defDev);
            }
        } else{
            chck.setChecked(false);
        }

        Spinner pairedBlthSpinner = (Spinner) findViewById(R.id.pairedBlth);

        // Create list of devices names, to show to the user
        List<String> devicesName = new ArrayList<>();
        for (BluetoothDevice dev: devices)
            devicesName.add(dev.getName());

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, devicesName);


        pairedBlthSpinner.setOnItemSelectedListener(this);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pairedBlthSpinner.setAdapter(adapterSpinner);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedDevice = MainActivity.getDevicesList().get(position);
        deviceSelected = true;

        if (chck.isChecked())
            DefaultDevice.writeDefaultDevice(selectedDevice.getName());

        Toast.makeText(getApplicationContext(), selectedDevice.getName() + " " +
                getResources().getString(R.string.settings_selected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedDevice = null;
        deviceSelected = false;
        DefaultDevice.writeDefaultDevice("");
    }

    public void defaultDeviceCheckBox(View v){
        if (! deviceSelected)
            return;

        if (chck.isChecked())
            DefaultDevice.writeDefaultDevice(selectedDevice.getName());
        else
            DefaultDevice.writeDefaultDevice("");
    }

    public static boolean isDeviceSelected(){
        return deviceSelected;
    }

    public static BluetoothDevice getSelectedDevice(){
        return selectedDevice;
    }
}
