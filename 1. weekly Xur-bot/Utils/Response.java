package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.json.JSONObject;

public class Response extends FileIO {
    public Response(String access, String refresh, String log) {
      super(access, refresh, log);
    }
    
    private HttpURLConnection openUrl(String url) {
      try {
        URL connection = new URL(url);
        return (HttpURLConnection)connection.openConnection();
      } catch (MalformedURLException e) {
        log(e.getMessage());
        return null;
      } catch (IOException e) {
        log(e.getMessage());
        return null;
      } 
    }
    
    public JSONObject post(Set<Pair<String, String>> set, String url, String body) {
      int count = 0;
      while (++count < 50) {
        try {
          HttpURLConnection conn = openUrl(url);
          conn.setRequestMethod("POST");
          for (Pair<String, String> map : set)
            conn.setRequestProperty((String)map.k, (String)map.v); 
          conn.setConnectTimeout(3000);
          conn.setReadTimeout(3000);
          conn.setDoInput(true);
          conn.setDoOutput(true);
          conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
          conn.connect();
          if (conn.getResponseCode() == 200)
            return getQuery(conn, true); 
          else
              continue;
        } catch (ProtocolException e) {
          log(e.getMessage());
        } catch (IOException e) {
          log(e.getMessage());
        } 
      } 
      return null;
    }
    
    public JSONObject get(Set<Pair<String, String>> set, String url) {
      int count = 0;
      while (++count < 50) {
        try {
          HttpURLConnection conn = openUrl(url);
          conn.setRequestMethod("GET");
          for (Pair<String, String> map : set)
            conn.setRequestProperty((String)map.k, (String)map.v); 
          conn.setConnectTimeout(3000);
          conn.setReadTimeout(3000);
          conn.setDoInput(true);
          conn.setDoOutput(false);
          conn.connect();
          if (conn.getResponseCode() == 200)
            return getQuery(conn, true);
          else
              continue;
        } catch (ProtocolException e) {
          log(e.getMessage());
        } catch (IOException e) {
          log(e.getMessage());
        } 
      } 
      return null;
    }
    
    private JSONObject getQuery(HttpURLConnection conn, boolean check) {
      try {
        JSONObject json = null;
        StringBuilder sb = new StringBuilder();
        String input = "";
        if (check) {
          BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          for (; (input = br.readLine()) != null; sb.append(input));
          json = new JSONObject(sb.toString());
          br.close();
        } else {
          BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
          for (; (input = br.readLine()) != null; sb.append(input));
          json = new JSONObject(sb.toString());
          br.close();
        } 
        return json;
      } catch (IOException e) {
        log(e.getMessage());
        return null;
      } finally {
        conn.disconnect();
      } 
    }
    
    public void log(String message) {
      printLog(message);
    }
  }
  