import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    public static void main(String[] args) {
        Registry r = null;

        try {
            r = LocateRegistry.createRegistry(2028);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Client client = new Client();
            assert r != null;
            r.bind("client", client);
            System.out.println("Client server ready");

            StorageClientInterface sci;
            sci = (StorageClientInterface) Naming.lookup("rmi://localhost:2025/storage");
            
            String defaultFileName = "www_nytimes_com";
            String filesDirectory = "D:\\OneDrive - ESTGV\\Fichas\\3 Ano\\SDt\\Projetos\\Examples\\";

            File f = new File(filesDirectory);
            File[] files = f.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.exists()) {
                        byte[] mydata;
                        if(i == 0) {
                            mydata = Files.readAllBytes(Paths.get(filesDirectory + defaultFileName + ".har"));
                        } else {
                            mydata = Files.readAllBytes(Paths.get(filesDirectory + defaultFileName + "_" + i + ".har"));
                        }
                        sci.uploadFile(file.getName(), mydata, mydata.length, i);
                    }
                }
            }

            System.out.println(sci.getTimeHarMap());
        } catch (Exception e) {
            System.out.println("Client server main " + e.getMessage());
        }
    }
}
