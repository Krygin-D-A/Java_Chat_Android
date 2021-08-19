import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ListenerClient implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter socketPrintWriter;
    private Scanner socketScanner;
    private String nickname;


    public ListenerClient(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            socketScanner = new Scanner(socket.getInputStream());
            socketPrintWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        listenMessage();
    }

    private void listenMessage() {
        while (socketScanner.hasNextLine()) {
            String messageFromClient;
            String time = server.getCurrentTime();

            if(nickname == null) {
                nickname = socketScanner.nextLine();
                messageFromClient = time + " " + nickname + " connected";
                sendHistoryMessages();
                server.addNickName(nickname);
                server.sendNickNamesToAll();
            }
            else {
                messageFromClient = time + " " + nickname + ": " + socketScanner.nextLine();
            }

            server.sendMessageToAll(messageFromClient);
            System.out.println(messageFromClient);
            server.writeToHistory(messageFromClient);
            server.writeToLog(messageFromClient);
        }

        server.sendMessageToAll(server.getCurrentTime() + " " + nickname + " disconnected");
        server.deleteNickName(nickname);
        server.sendNickNamesToAll();
        System.out.println(server.getCurrentTime() + " " + nickname + " disconnected");
        server.writeToHistory(server.getCurrentTime() + " " + nickname + " disconnected");
    }

//    private void sendNickNames(String newNickName) {
//        sendMessage(server.getAllNickNames(nickname));
//    }

    private void sendHistoryMessages() {
        sendMessage(server.getAllHistory());
    }

    public void sendMessage(String message) {
        socketPrintWriter.println(message);
        socketPrintWriter.flush();
    }

}
