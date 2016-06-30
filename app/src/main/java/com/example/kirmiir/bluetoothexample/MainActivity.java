package com.example.kirmiir.bluetoothexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Boolean onView;
    Handler h;
    private static final String TAG = "bluetooth1";

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    final int RECIEVE_MESSAGE = 1;

    private StringBuilder sb = new StringBuilder();

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ConnectedThread mConnectedThread;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-адрес Bluetooth модуля
    private static String address = "14:36:C6:2E:AC:2A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Arduino Bluetooth");
        setContentView(R.layout.activity_main);
        onView = true;

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        final Button buttonOn = (Button) findViewById(R.id.onbutton);
        buttonOn.bringToFront();
        buttonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onView = !onView;
                ChangeView(onView);
            }
        });

        final Button buttonOff = (Button) findViewById(R.id.offbutton);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onView = !onView;
                ChangeView(onView);
            }
        });

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // если приняли сообщение в Handler
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);                                                // формируем строку
                        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
                            sb.delete(0, sb.length());                                      // и очищаем sb
                            EditText statusText = (EditText) findViewById(R.id.editText2);
                            statusText.setText(sbprint);
                        }
                        //Log.d(TAG, "...Строка:"+ sb.toString() +  "Байт:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        final Button buttonPtt = (Button) findViewById(R.id.pttbutton);
        buttonPtt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        sendData("T");

                        return false;
                    case MotionEvent.ACTION_UP:
                        sendData("R");
                        return false;
                }
                return false;
            }
        });

        final Button buttonBlackout = (Button) findViewById(R.id.blackoutbutton);
        buttonBlackout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("B");
            }
        });

        final Button buttonOpen = (Button) findViewById(R.id.openbutton);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("O");
            }
        });



        /*// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
        } else {
        }*/
    }

    private void SearchDevice(){

    }

    private void ChangeView(Boolean on){
        Animation showAnim = AnimationUtils.loadAnimation(this,R.anim.show_animation);
        showAnim.setFillBefore(true);
        showAnim.setFillAfter(true);
        Animation hideAnim = AnimationUtils.loadAnimation(this,R.anim.hide_animation);
        hideAnim.setFillAfter(true);
        Button onButton = (Button) findViewById(R.id.onbutton);
        Button pttButton = (Button) findViewById(R.id.pttbutton);
        Button offButton = (Button) findViewById(R.id.offbutton);
        Button openButton = (Button) findViewById(R.id.openbutton);
        Button blackoutButton = (Button) findViewById(R.id.blackoutbutton);
        if (on){
            findViewById(R.id.onbutton).bringToFront();
            Animation scale = AnimationUtils.loadAnimation(this,R.anim.button_scale_reverse);
            scale.setFillBefore(true);
            scale.setFillAfter(true);
            onButton.startAnimation(scale);
            onButton.setClickable(true);
            blackoutButton.startAnimation(showAnim);
            blackoutButton.setClickable(true);
            openButton.startAnimation(showAnim);
            openButton.setClickable(true);
            pttButton.startAnimation(hideAnim);
            pttButton.setClickable(false);
            offButton.startAnimation(hideAnim);
            offButton.setClickable(false);
        }
        else {
            pttButton.bringToFront();
            pttButton.startAnimation(showAnim);
            pttButton.setAlpha(1.0f);
            pttButton.setClickable(true);
            offButton.startAnimation(showAnim);
            offButton.setAlpha(1.0f);
            offButton.setClickable(true);
            Animation scale = AnimationUtils.loadAnimation(this,R.anim.button_scale);
            scale.setFillAfter(true);
            onButton.startAnimation(scale);
            onButton.setClickable(false);
            blackoutButton.startAnimation(hideAnim);
            blackoutButton.setClickable(false);
            openButton.startAnimation(hideAnim);
            openButton.setClickable(false);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - попытка соединения...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Соединяемся...");
        try {
            btSocket.connect();
            EditText editText = (EditText)findViewById(R.id.editText);
            editText.setText("connect");
            Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Создание Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }


        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (btAdapter != null) {
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
            }
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // If we're already discovering, stop it
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        btAdapter.startDiscovery();
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth включен...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Посылаем данные: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
            msg = msg +  ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "No Device";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Отправляем в очередь сообщений Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Данные для отправки: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
