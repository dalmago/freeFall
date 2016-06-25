package instrum.freefall_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import instrum.freefall_app.MainActivity;

/**
 * Created by dalmago on 6/25/16.
 */
public class ConnectBluetooth extends Thread{

    private final String handshaking = "ready!";
    private final String handshakingAns = "start!";
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Context context = null;

    public ConnectBluetooth(BluetoothDevice device, UUID uuid, Context context) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.context = context;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Toast.makeText(context, "--Error creating socket--", Toast.LENGTH_LONG).show();
        }
        mmSocket = tmp;
    }

    public void run() {
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                    "--Error connecting socket--").sendToTarget();
            try {
                mmSocket.close();
            } catch (IOException closeException) {}
            this.cancel();
            return;
        }

        // Get the input and output streams
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                    "--Error getting socket streams--").sendToTarget();
            this.cancel();
            return;
        }
        MainActivity.myHandler.obtainMessage(MainActivity.MSG_BLTH_CONNECTED).sendToTarget();

        byte[] buffer = new byte[64];  // buffer store for the stream

        // Handshake with bluetooth device
        try {
            String s = new String();
            int count;

            mmOutStream.write(handshaking.getBytes());

            while (! s.contains(handshakingAns)){
                if (mmInStream.available()>0){
                    count = mmInStream.read(buffer, 0, 64);
                    s = s.concat(new String(buffer).substring(0, count));
                }

                if (s.length() > 64){
                    MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                            "--Error handshaking-- ").sendToTarget();
                    this.cancel();
                    return;
                }

            }

        } catch(IOException e){
            MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                "--Error sending/receiving handshake--").sendToTarget();
            this.cancel();
            return;
        }

        MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                "--Success handshaking--").sendToTarget();

        int count;
        while (true) {
            try {
                if (mmInStream.available()>0) {
                    // Read from the InputStream
                    count = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    MainActivity.myHandler.obtainMessage(MainActivity.MSG_BLTH_RCVD, count, -1, buffer)
                            .sendToTarget();
                }

            } catch (IOException e) {
                MainActivity.myHandler.obtainMessage(MainActivity.MSG_ERROR_BLTH,
                        "--Error reading stream--").sendToTarget();
                this.cancel();
                break;
            }
        }

    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Toast.makeText(context, "--Error closing socket--", Toast.LENGTH_LONG).show();
        }
    }

}
