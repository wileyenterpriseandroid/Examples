package com.enterpriseandroid.syncadaptercontacts.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;


public class InstallationId {
    private static final String INSTALLATION = "INSTALLATION";
    private static final String DEFAULT_ID = "SyncContacts";

    private static String installId;


    private final Context ctxt;

    public InstallationId(Context ctxt) { this.ctxt = ctxt; }

    // you won't run this from the UI thread, of course...
    public String getInstallationId() {
        synchronized (InstallationId.class) {
            if (null == installId) {
                try {
                    File f = new File(ctxt.getFilesDir(), INSTALLATION);
                    if (f.exists()) { installId = readInstallationFile(f); }
                    else {
                        String id = UUID.randomUUID().toString();
                        writeInstallationFile(f, id);
                        installId = id;
                    }
                }
                catch (IOException e) { }
            }
            if (null == installId) { installId = DEFAULT_ID; }
        }

        return installId;
    }

    private String readInstallationFile(File f) throws IOException {
        RandomAccessFile in = null;
        byte[] bytes;
        try {
            in = new RandomAccessFile(f, "r");
            bytes = new byte[(int) in.length()];
            in.readFully(bytes);
        }
        finally {
            if (null != in) {
                try { in.close(); } catch (Exception e) { }
            }
        }

        return new String(bytes);
    }

    private void writeInstallationFile(File f, String uid) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            out.write(uid.getBytes());
        }
        finally {
            if (null != out) {
                try { out.close(); } catch (Exception e) { }
            }
        }
    }
}
