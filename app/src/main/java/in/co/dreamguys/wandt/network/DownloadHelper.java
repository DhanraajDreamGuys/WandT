package in.co.dreamguys.wandt.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gopi.s on 06-Jan-16.
 */
public class DownloadHelper {
    // constructor
    public DownloadHelper() {

    }
    public String getImageFromUrl(File path, String filename, String ImgURL){
        String filepath = null;
        try
        {
            URL url = new URL(ImgURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            File file = new File(path,filename);
            if(file.createNewFile())
            {
                file.createNewFile();
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) > 0 )
            {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            if(downloadedSize==totalSize) filepath=file.getPath();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            filepath=null;
            e.printStackTrace();
        }
        return filepath;
    }

}
