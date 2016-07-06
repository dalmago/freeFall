package instrum.freefall_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView outputTextView;
    private final int BLTHREQUESTCODE = 1;
    private BluetoothAdapter myBluetoothAdapter;
    private boolean bluetoothInitialized = false;
    private static List<BluetoothDevice> devices;
    private ConnectBluetooth connection;
    private ProgressBar connectLoading;
    private SpeedometerGauge speedometer;

    public static Handler myHandler;
    public final static int MSG_ERROR_BLTH=1;
    public final static int MSG_BLTH_CONNECTED=2;
    public final static int MSG_BLTH_DISCONNECTED=3;
    public final static int MSG_BLTH_RCVD=4;
    public final static double MAX_SPEED = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectLoading = (ProgressBar) findViewById(R.id.connectProgress);
        connectLoading.setVisibility(View.INVISIBLE);
        outputTextView = (TextView) findViewById(R.id.outputTextView);
        outputTextView.setText("");
        findViewById(R.id.disconnectButton).setVisibility(View.GONE);
        findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);

        // configure value range and ticks
        speedometer.setMaxSpeed(MAX_SPEED);
        speedometer.setMajorTickStep(30);
        speedometer.setMinorTicks(2);

        // Configure value range colors
        speedometer.addColoredRange(0, MAX_SPEED/3, Color.GREEN);
        speedometer.addColoredRange(MAX_SPEED/3, MAX_SPEED*2/3, Color.YELLOW);
        speedometer.addColoredRange(MAX_SPEED*2/3, MAX_SPEED, Color.RED);

        configureBluetooth();
        new DefaultDevice(getApplicationContext());

        myHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case MSG_BLTH_RCVD:
                        byte[] x = ((byte[])msg.obj);

                        //int y = (((x[3] & 0x00FF)<<24) | ((x[2] & 0x00FF)<<16) | ((x[1] & 0x00FF)<<8) | (x[0] & 0x00FF));
                        int y = (x[0] & 0x00FF);

                        outputTextView.setText(Integer.valueOf(y).toString().concat(" km/h"));
                        speedometer.setSpeed(y);
                        break;

                    case MSG_ERROR_BLTH:
                        connectLoading.setVisibility(View.INVISIBLE);
                        String s = (String)msg.obj;
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                        break;

                    case MSG_BLTH_CONNECTED:
                        connectLoading.setVisibility(View.INVISIBLE);
                        findViewById(R.id.disconnectButton).setVisibility(View.VISIBLE);
                        findViewById(R.id.connectButton).setVisibility(View.GONE);
                        outputTextView.setText(R.string.blth_connected);
                        break;

                    case MSG_BLTH_DISCONNECTED:
                        findViewById(R.id.disconnectButton).setVisibility(View.GONE);
                        findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
                        outputTextView.setText(R.string.blth_disconnected);
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! bluetoothInitialized)
            return;

        Object[] pairedDevices = myBluetoothAdapter.getBondedDevices().toArray();

        devices = new ArrayList<>();

        if(pairedDevices.length == 0){
            Toast.makeText(getApplicationContext(), R.string.blth_error_pair,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        for (Object dev : pairedDevices){
            devices.add((BluetoothDevice)dev);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (connection != null)
            connection.cancel();

        outputTextView.setText(R.string.blth_disconnected);
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
                        Toast.makeText(getApplicationContext(), R.string.blth_error_access,
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

    // Initialize the Bluetooth configuration
    private void configureBluetooth(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), R.string.blth_error_access,
                    Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), R.string.blth_error_turnon,
                                Toast.LENGTH_SHORT).show();
                    break;
            }
    }

    public void connectBlth(View v){
        if (!bluetoothInitialized){
            if (myBluetoothAdapter != null && !myBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BLTHREQUESTCODE); // Request number 1
            } else{
                Toast.makeText(getApplicationContext(), R.string.blth_error_access,
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (!SettingsActivity.isDeviceSelected() && !DefaultDevice.isDefaultDeviceAvailable()){
            if (devices.size() == 0)
                Toast.makeText(getApplicationContext(), R.string.blth_error_pair,
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), R.string.blth_error_chooseSettings,
                        Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice dev = null;
        if (DefaultDevice.isDefaultDeviceAvailable()){
            String s = DefaultDevice.getDefaultDevice();
            for (BluetoothDevice d : devices){
                if (d.getName().equals(s)) {
                    dev = d;
                    break;
                }
            }
        }
        if (dev == null)
            dev = SettingsActivity.getSelectedDevice();
        connection =  new ConnectBluetooth(dev, dev.getUuids()[0].getUuid(), getApplicationContext());

        connection.start();
        connectLoading.setVisibility(View.VISIBLE);
    }

    public void disconnectBluetooth(View v){
        connection.cancel();
    }

    public static List<BluetoothDevice> getDevicesList(){
        return devices;
    }
}
