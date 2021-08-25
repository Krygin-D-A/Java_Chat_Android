package com.example.chatclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private final String nickNameKey = "_NickName_";

    private EditText etMessage;
    private EditText etNickName;
    private Button btnSend;
    private Button btnConnect;
    private ProgressBar pbConnect;
    private TextView tvConnected;
    private ListView lvNickNames;
    private ListView lvMessages;

    private Socket socket;
    private PrintWriter printWriter;
    private Scanner scanner;

    private List<String> messages;
    private ArrayAdapter<String> messagesAdapter;
    private List<String> nickNames;
    private ArrayAdapter<String> nickNamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = findViewById(R.id.etMessage);
        etNickName = findViewById(R.id.etNickName);
        btnSend = findViewById(R.id.btnSend);
        btnConnect = findViewById(R.id.btnConnect);
        pbConnect = findViewById(R.id.pbConnect);
        tvConnected = findViewById(R.id.tvConnected);
        lvMessages = findViewById(R.id.lvMessages);
        lvNickNames = findViewById(R.id.lvNickNames);

        btnConnect.setOnClickListener(this::connect);
        btnSend.setOnClickListener(this::send);

        messages = new ArrayList<>();

        messagesAdapter = new ArrayAdapter<String>(
                getBaseContext(),
                android.R.layout.simple_list_item_1,
                messages);

        lvMessages.setAdapter(messagesAdapter);

        nickNames = new ArrayList<>();

        nickNamesAdapter = new ArrayAdapter<String>(
                getBaseContext(),
                android.R.layout.simple_list_item_1,
                nickNames);

        lvNickNames.setAdapter(nickNamesAdapter);
    }

    private void connect(View view) {
        String nickName = etNickName.getText().toString();

        if (!nickName.equals("")) {
            pbConnect.setVisibility(View.VISIBLE);

            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(Constants.HOSTNAME, Constants.PORT), Constants.CONNECTION_TIMEOUT);
                    printWriter = new PrintWriter(socket.getOutputStream());
                    scanner = new Scanner(socket.getInputStream());

                    runOnUiThread(() -> {
                        //не рекомендуется во второстепенных потоках обращаться к UI
                        //т.к. поведение не предсказуемо. Используем runOnUiThread()
                        pbConnect.setVisibility(View.INVISIBLE);
                        tvConnected.setText("Connected");
                    });

                    SendMessage(nickName);

                    while (scanner.hasNextLine()) {
                        String nextLine = scanner.nextLine();

                        String[] message = nextLine.split(" ");

                        if (message[0].equals(nickNameKey)) {
                            //nicknames
                            nickNames.clear();

                            for (int i = 1; i < message.length; i++) {
                                nickNames.add(message[i]);
                            }

                            runOnUiThread(() -> {
                                nickNamesAdapter.notifyDataSetChanged();
                            });
                        } else {
                            //messages
                            messages.add(nextLine);
                        }

                        runOnUiThread(() -> {
                            messagesAdapter.notifyDataSetChanged();
                            lvMessages.smoothScrollToPosition(messages.size() - 1);
                        });
                    }
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(() -> {
                        pbConnect.setVisibility(View.INVISIBLE);
                    });
                }
            }).start();
        }
    }

    private void send(View view) {
        SendMessage(etMessage.getText().toString());
        etMessage.setText("");
    }

    private void SendMessage(String message) {
        new Thread(() -> {
            printWriter.println(message);
            printWriter.flush();
        }).start();
    }
}