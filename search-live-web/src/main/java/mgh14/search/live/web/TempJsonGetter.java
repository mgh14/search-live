package mgh14.search.live.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TempJsonGetter {

  public String getFile() {
    BufferedReader br = null;
    String fileIn = "";
    try {

      String sCurrentLine;

      br = new BufferedReader(new FileReader("C:\\Users\\mgh14\\Desktop\\results-json-windows-azure.txt"));

      while ((sCurrentLine = br.readLine()) != null) {
        fileIn += sCurrentLine;
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (br != null)br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    return fileIn;
  }
}
