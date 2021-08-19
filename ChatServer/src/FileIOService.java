import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileIOService implements AutoCloseable {
    //this class uses for read and write to the text file
    //and access to it from some different threads.

    File file;
    private RandomAccessFile historyFile;

    public FileIOService(String fileName) {
        file = new File(fileName);
        try {
            historyFile = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> fileRead() {
        List<String> strings = new ArrayList<>();

        synchronized (historyFile) {
            try {
                historyFile.seek(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    if (!(historyFile.getFilePointer() < historyFile.length())) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    strings.add(historyFile.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return strings;
    }

    public void fileAppendString(String string) {
        synchronized (historyFile) {
            try {
                historyFile.seek(historyFile.length());
                historyFile.writeBytes(string + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void fileRewrite(List<String> strings) {
        synchronized (historyFile) {
            try {
                historyFile.seek(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                historyFile.setLength(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            strings.forEach((s) -> {
                try {
                    historyFile.writeBytes(s + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        if (historyFile != null) {
            try {
                historyFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
