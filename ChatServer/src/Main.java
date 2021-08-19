
public class Main {

    public static void main(String[] args) {
        System.out.println("Chat. Krygin D.A.. 2021");

        try(Server server = new Server()) {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
