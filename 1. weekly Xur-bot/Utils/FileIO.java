package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileIO {
    private File file;
    private InputStream is;
    private OutputStream os;
    private String access;
    private String refresh;
    private String log;

    public FileIO(String access, String refresh, String log){
        this.access = access;
        this.refresh = refresh;
        this.log = log;
        SetFile(log);
        SetOs(false);
    }

    private void SetFile(String Link) {
        this.file = new File(Link);
    }

    private void SetIs() {
        try {
          this.is = new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } 
    }

    private void SetOs(boolean isTrue) {
        try {
          this.os = new FileOutputStream(this.file, isTrue);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } 
    }
      
      private void Setter(String str, boolean isTrue) {
        try {
          SetOs(isTrue);
          this.os.write(str.getBytes());
        } catch (IOException e) {
          e.printStackTrace();
        } 
    }
      
      private void Setter(String str) {
        Setter(str, false);
    }
      
      private String Getter() {
        SetIs();
        StringBuilder sb = new StringBuilder();
        int input = -987654321;
        try {
          while ((input = this.is.read()) != -1)
            sb.append((char)input); 
        } catch (IOException e) {
          e.printStackTrace();
        } 
        return sb.toString();
    }
      
      public void printLog(String message) {
        SetFile(this.log);
        Setter(message + "\n", true);
    }
      
      public String getRefresh_Token() {
        SetFile(this.refresh);
        return Getter();
    }
      
      public void setRefresh_Token(String refresh_token) {
        SetFile(this.refresh);
        Setter(refresh_token);
    }
      
      public String getAccess_Token() {
        SetFile(this.access);
        return Getter();
    }
      
      public void setAccess_Token(String access_token) {
        SetFile(this.access);
        Setter(access_token);
    }
}