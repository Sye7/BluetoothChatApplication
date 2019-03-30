package com.google.syed.flash.bluetoothtransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {


    SendRecieve sendRecieve;

    int REQUEST_ENABLE_BT = 1;
    Button availableDevices;
    ListView listView;
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter myListAdapter;
    ArrayList<String> devices;
    ImageView visibility;
    ImageView send;
    BluetoothDevice[] bluetoothDevices;
    TextView msg_Box;
    Switch switchOnOff;
    TextView msgScreen;
    ImageView lang;
    String language;
    ImageView sayed;
    Thread2 thread2;
    Intent mSpeechRecognizerIntent;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    private static final String APP_NAME = "BluetoothTransfer";
    private static final UUID uuid = UUID.fromString("afe7e208-191a-11e9-ab14-d663bd873d93");
    static int backPressed = 0;

    private TextToSpeech textToSpeechSystem;


    @Override
    public void onBackPressed() {

        textToSpeechSystem.shutdown();
        backPressed++;
        if (backPressed == 1)
            Toast.makeText(this, "Back again to Main Screen", Toast.LENGTH_SHORT).show();

        if (backPressed == 2) {
            hideChat();
        }

        if (backPressed >= 3) {
            System.exit(0);
        }

    }

    @Override
    protected void onPause() {

       textToSpeechSystem.shutdown();
        super.onPause();
    }

    public void hideChat() {

        sayed.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        msgScreen.setVisibility(View.INVISIBLE);
        lang.setVisibility(View.INVISIBLE);


        listView.setVisibility(View.VISIBLE);
        availableDevices.setVisibility(View.VISIBLE);
        visibility.setVisibility(View.VISIBLE);
        switchOnOff.setVisibility(View.VISIBLE);
        msg_Box.setVisibility(View.VISIBLE);


    }

    public void showChat() {


        sayed.animate().alpha(0.5f).setDuration(500);
        send.animate().alpha(0.5f).setDuration(500);
        msgScreen.animate().alpha(0.5f).setDuration(500);
        lang.animate().alpha(0.5f).setDuration(500);

        sayed.setVisibility(View.VISIBLE);
        send.setVisibility(View.VISIBLE);
        lang.setVisibility(View.VISIBLE);
        msgScreen.setVisibility(View.VISIBLE);


        listView.setVisibility(View.INVISIBLE);
        availableDevices.setVisibility(View.INVISIBLE);
        visibility.setVisibility(View.INVISIBLE);
        switchOnOff.setVisibility(View.INVISIBLE);
        msg_Box.setVisibility(View.INVISIBLE);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final String[] bhasha = {"US", "UK", "ITALY", "CHINA", "KOREA", "FRANCE", "JAPAN","FRANCE","HINDI"};
        language = "";
        lang = (ImageView) findViewById(R.id.language);
        sayed = (ImageView) findViewById(R.id.say);
        msgScreen = (TextView) findViewById(R.id.msgScreen);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        switchOnOff = (Switch) findViewById(R.id.onOff);
        msg_Box = (TextView) findViewById(R.id.msg_Box);
        send = (ImageView) findViewById(R.id.send);
        visibility = (ImageView) findViewById(R.id.Visibility);
        availableDevices = (Button) findViewById(R.id.showAvilable);
        listView = (ListView) findViewById(R.id.listview);
        devices = new ArrayList<>();
         thread2 = new Thread2();

        //  myListAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, devices);

   //     say("Hello sir");
        hideChat();
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);


        initailzeSpeechRecognizer();




        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    switchOnOff.setText("Off");

                    bluetoothOnMethod();
                } else {
                    switchOnOff.setText("On");
                    visibility.setBackgroundResource(R.drawable.invisible);
                    bluetoothOfMethod();
                }

            }
        });

        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //       String[] bhasha = {"US","UK","ITALY","CHINA","KOREA","FRANCE","JAPAN"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Pick a Country for Language");
                builder.setItems(bhasha, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // user click on colour 'which'
                        language = bhasha[which];
                    }
                });
                builder.show();

            }
        });

        msgScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                limit = 1;
            }
        });


        sayed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                limit = 0;
                mySpeechRecognizer.startListening(mSpeechRecognizerIntent);

            }
        });


    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {

                case STATE_LISTENING:
                    msg_Box.setText("Listening");
                    break;

                case STATE_CONNECTING:
                    msg_Box.setText("Connecting");
                    break;

                case STATE_CONNECTED:
                    msg_Box.setText("Connected");
                    thread2.run("Successfully Connected");
                    showChat();
                    break;

                case STATE_CONNECTION_FAILED:
                    msg_Box.setText("Connection Failed");
                    break;

                case STATE_MESSAGE_RECEIVED:
                    //WE WILL WRITE IT LATER DO YOU HEAR ME
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
                    msgScreen.setText(tempMsg);                                                 // Just remove
                    thread2.run(tempMsg);   // yeah

                    break;
            }
            return true;
        }
    });

    // visibility to check status

    private class ServerClass extends Thread {

        private BluetoothServerSocket serverSocket;

        public ServerClass() throws IOException {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuid);

        }

        public void run() {

            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    // write code to send and recieve
                    sendRecieve = new SendRecieve(socket);
                    sendRecieve.start();

                    break;

                }
            }
        }
    }


    public void discoverable(View view) {

        visibility.setBackgroundResource(R.drawable.visible);
        switchOnOff.setChecked(true);
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 50);
        Toast.makeText(getApplicationContext(), "Visible for 60 sec", Toast.LENGTH_SHORT).show();
        startActivity(intent);

    }


    public void exeButtonToSearchAvailableDevices(View view) {


        Set<BluetoothDevice> bluetooth = bluetoothAdapter.getBondedDevices();
        bluetoothDevices = new BluetoothDevice[bluetooth.size()];
        String[] str = new String[bluetooth.size()];


        int index = 0;

        if (bluetooth.size() > 0) {

            for (BluetoothDevice device : bluetooth) {

                bluetoothDevices[index] = device;
                //     devices.add(device.getName());
                str[index] = device.getName();
                index++;
            }

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, str);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        //         IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //       registerReceiver(myReciever,intentFilter);

        visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ServerClass serverClass = null;
                try {
                    serverClass = new ServerClass();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverClass.start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ClientClass clientClass = new ClientClass(bluetoothDevices[position]);
                clientClass.start();
                msg_Box.setText("Connecting");

            }
        });




    }



    /*BroadcastReceiver myReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(dev.getName());
                myListAdapter.notifyDataSetChanged();
            }
        }
    };
    */


    public void walky(String s){

        sendRecieve.write(s.getBytes());

    }


    public void bluetoothOfMethod() {


        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public void bluetoothOnMethod() {


        if (bluetoothAdapter == null) {

            // Device doesn't Support Bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
        } else {

            if (!bluetoothAdapter.isEnabled()) {

                //Code foe enabling
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == RESULT_OK) {

                // Bluetooth is enabled
                thread2.run("Bluetooth is enable ");
            } else if (resultCode == RESULT_CANCELED) {

                switchOnOff.setChecked(false);

                // bluetooth enabling is cancelled
            }
        }
    }


    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {

            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);


                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

    }

    private class SendRecieve extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve(BluetoothSocket socket) {

            bluetoothSocket = socket;
            InputStream streamTempIn = null;
            OutputStream streamTempOut = null;

            try {
                streamTempIn = bluetoothSocket.getInputStream();
                streamTempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = streamTempIn;
            outputStream = streamTempOut;
        }


        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes) {

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



   /* public void say(String textToSay)
    {
        super.onStart();

        textToSpeechSystem = new TextToSpeech(this, ttsInitResult -> {
            if (TextToSpeech.SUCCESS == ttsInitResult) {

                language = language.toLowerCase();
                switch(language)
                {
                    case "us":
                        textToSpeechSystem.setLanguage(Locale.US);
                        break;

                    case "uk":
                        textToSpeechSystem.setLanguage(Locale.UK);
                        break;

                    case "italy":
                        textToSpeechSystem.setLanguage(Locale.ITALIAN);
                        break;

                    case "china":
                        textToSpeechSystem.setLanguage(Locale.CHINESE);
                        break;

                    case "france":
                        textToSpeechSystem.setLanguage(Locale.FRANCE);
                        break;

                    case "japan":
                        textToSpeechSystem.setLanguage(Locale.JAPANESE);
                        break;

                    case "korea":
                        textToSpeechSystem.setLanguage(Locale.KOREAN);
                        break;
                    case "hindi":
                        textToSpeechSystem.setLanguage(new Locale("hin"));


                    default:
                        textToSpeechSystem.setLanguage(Locale.ENGLISH);
                        break;

                }


                textToSpeechSystem.speak(textToSay, TextToSpeech.QUEUE_ADD,null, null);

            }

        });



    }
    */



    class Thread2 extends Thread{
        public  void run(String s){

           // speak(s);
            //say(s);
        }
    }



    // speech recog
    int limit = 0;

    private SpeechRecognizer mySpeechRecognizer;


    private void initailzeSpeechRecognizer() {

        // check speech recognizer is availabe

        if(SpeechRecognizer.isRecognitionAvailable(this)){

            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {



                }

                @Override
                public void onError(int error) {

                    mySpeechRecognizer.startListening(mSpeechRecognizerIntent);


                }

                @Override
                public void onResults(Bundle results) {

                    ArrayList<String> result = results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );


                    // output string;

                 //   processResult(result.get(0));
                    walky(result.get(0));

                    if(limit ==0)
                    mySpeechRecognizer.startListening(mSpeechRecognizerIntent);


                    String temp = result.get(0).toString().toLowerCase();

                    if(temp.contains("stop")) {

                        if (temp.contains("ok")) {

                            mySpeechRecognizer.stopListening();
                            mySpeechRecognizer.destroy();
                            limit = 1;
                        }
                    }



                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });

        }
    }


}

