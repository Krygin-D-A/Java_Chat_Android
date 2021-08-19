import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server implements AutoCloseable {
    private List<ListenerClient> listenerClients;
    private PrintWriter logPrintWriter;
    private FileIOService fileIOService;
    private Timer timerClearHistory;
    private TimerTask timerClearHistoryTask;
    private List<String> nickNames;

    public Server() {
        listenerClients = new ArrayList<>();
        nickNames = new ArrayList<>();

        try {
            logPrintWriter = new PrintWriter(new FileOutputStream(Constants.LOG_FILE, true), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fileIOService = new FileIOService(Constants.HISTORY_FILE);

        timerClearHistoryTask = new ClearHistoryTimerTask(fileIOService);
        timerClearHistory = new Timer();
        timerClearHistory.schedule(timerClearHistoryTask, 1000, Constants.CLEAR_HISTORY_DELAY_MS);
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(Constants.PORT);
            System.out.println(getCurrentTime() + " server started");

            while (true) {
                Socket socket = serverSocket.accept();
                ListenerClient listenerClient = new ListenerClient(socket, this);
                listenerClients.add(listenerClient);
                new Thread(listenerClient).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date dateTime = new Date();

        return dateFormat.format(dateTime);
    }

    public void sendMessageToAll(String message) {
        listenerClients.forEach(v -> v.sendMessage(message));
    }

    public void sendNickNamesToAll() {
        StringBuilder result = new StringBuilder();
        nickNames.forEach((s) -> result.append("_NickName_ " + s + System.lineSeparator()));
        listenerClients.forEach(v -> v.sendMessage(result.toString()));
    }

    public void addNickName(String newNickName) {
        nickNames.add(newNickName);
    }

    public void deleteNickName(String nickName) {
        nickNames.remove(nickName);
    }

    public void writeToHistory(String message) {
        //FileIO uses synchronized()
        fileIOService.fileAppendString(message);
    }

    public void writeToLog(String message) {
        //PrintWriter uses "synchronized(lock)"
        logPrintWriter.println(message);
    }

    public String getAllHistory() {
        StringBuilder result = new StringBuilder();
        List<String> strings = fileIOService.fileRead();
        strings.forEach((s) -> result.append(s + System.lineSeparator()));

        return result.toString();
    }

    @Override
    public void close() throws Exception {
        if (logPrintWriter != null) {
            logPrintWriter.close();
        }

        if (fileIOService != null) {
            fileIOService.close();
        }
    }
}
