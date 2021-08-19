import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClearHistoryTimerTask extends TimerTask {
    private FileIOService fileIOService;

    public ClearHistoryTimerTask(FileIOService service) {
        fileIOService = service;
    }

    @Override
    public void run() {
        List<String> strings = new ArrayList<>();

        strings = fileIOService.fileRead();

        checkStringsExpired(strings);

        fileIOService.fileRewrite(strings);
    }

    private void checkStringsExpired(List<String> strings) {
        List<Integer> indexesToRemove = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date currentTime = new Date();

        for (int i = strings.size() - 1; i >= 0; i--) {
            try {
                Date stringTime = formatter.parse(strings.get(i).substring(0, 18));

                if(stringTime.getTime() < currentTime.getTime() - Constants.CLEAR_HISTORY_DELAY_MS) {
                    indexesToRemove.add(i);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        indexesToRemove.forEach((i) -> {
//            strings.remove(i);
            int ind = i;
            strings.remove(ind);
        });
    }
}
