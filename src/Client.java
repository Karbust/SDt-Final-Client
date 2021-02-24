import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {
        List<Thread> threadList = new ArrayList<>();

        Thread t = (new Thread(() -> {
            RMIStorage.main(new String[]{});
            RMIMaster.main(new String[]{});
        }));
        t.start();
        threadList.add(t);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread t1 = (new Thread(() -> {
            try {
                saveFilesStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        t1.start();
        threadList.add(t1);

        MasterClientInterface mci = (MasterClientInterface) Naming.lookup("rmi://localhost:2024/master");

        Thread t2 = (new Thread(() -> {
            try {
                while(true) {
                    int result = mci.checkTaskResult();
                    if(result != -1) {
                        System.out.println("Resultado: " + result);
                        break;
                    }
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        t2.start();
        threadList.add(t2);

        for (Thread thread : threadList) {
            thread.join();
        }

        System.exit(1);
    }

    private static void saveFilesStorage() throws Exception {
        StorageClientInterface sci = (StorageClientInterface) Naming.lookup("rmi://localhost:2025/storage");

        String defaultFileName = "www_nytimes_com";
        String filesDirectory = "D:\\OneDrive - ESTGV\\Fichas\\3 Ano\\SDt\\Projetos\\Examples\\";

        File f = new File(filesDirectory);
        File[] files = f.listFiles();

        int counter = 0;

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.exists()) {
                    byte[] mydata;
                    if (i == 0) {
                        mydata = Files.readAllBytes(Paths.get(filesDirectory + defaultFileName + ".har"));
                    } else {
                        mydata = Files.readAllBytes(Paths.get(filesDirectory + defaultFileName + "_" + i + ".har"));
                    }
                    if (sci.uploadFile(file.getName(), mydata, mydata.length, i))
                        counter++;
                }
            }
        }

        MasterClientInterface mci = (MasterClientInterface) Naming.lookup("rmi://localhost:2024/master");
        mci.task(counter);
    }
}
