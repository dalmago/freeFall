package instrum.freefall_app;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dalmago on 6/28/16.
 */
public class DefaultDevice{

    private static final String cacheFileName = "defaultDevice";
    private static File file  = null;
    private static Context context = null;
    private static boolean defaultDeviceAvailable = false;
    private static String defaultDevice = null;

    public DefaultDevice(Context ctxt){
        context = ctxt;
        file = new File(context.getCacheDir(), cacheFileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException | SecurityException e) {
                Toast.makeText(context, " Error saving preference", Toast.LENGTH_SHORT).show();
                file = null;
                return;
            }
            return;
        }

        if (file.length() == 0)
            return;
        FileInputStream finp = null;
        byte[] b = new byte[(int)file.length()];
        //String s = new String();
        try {
            finp = new FileInputStream(file);
            finp.read(b);
        } catch(SecurityException | IOException e){
            Toast.makeText(context, "Error reading cache file", Toast.LENGTH_SHORT).show();
            file = null;
            return;
        }

        defaultDeviceAvailable = true;
        defaultDevice = new String(b);

        try {
            finp.close();
        } catch(IOException e){
            Toast.makeText(context, "Error closing cache file", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public static void writeDefaultDevice(String s){
        byte[] b = s.getBytes();

        FileOutputStream foutp = null;
        try{
            foutp = new FileOutputStream(file);
            foutp.write(b);
            //foutp.write(0); // represents end of String
            foutp.close();
        } catch(SecurityException | IOException e){
            Toast.makeText(context, "Error writing to cache file", Toast.LENGTH_SHORT).show();
            return;
        }
        if (s.length() == 0)
            defaultDeviceAvailable = false;
         else
            defaultDeviceAvailable = true;

        defaultDevice = s;
    }

    public static boolean isDefaultDeviceAvailable(){
        return defaultDeviceAvailable;
    }

    public static String getDefaultDevice(){
        return defaultDevice;
    }


}