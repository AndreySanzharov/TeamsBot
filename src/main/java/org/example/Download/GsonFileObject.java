package org.example.Download;


public class GsonFileObject {
    private String filename;
    private int size;
    private String type;
    private String url;
    private boolean ok;


    public String getFilename() {
        return filename;
    }


    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }


    public int getSize() {
        return size;
    }

    public boolean isOk() {
        return ok;
    }

}
