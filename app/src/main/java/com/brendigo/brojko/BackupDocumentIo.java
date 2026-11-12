package com.brendigo.brojko;

import android.content.ContentResolver;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SAF document I/O ONLY. File streaming is executed on the background
 * executor; JSON validation and preference mutations are separate concerns.
 */
final class BackupDocumentIo {
    static final int MAX_BYTES=512000;
    private BackupDocumentIo(){}

    static byte[] read(ContentResolver resolver,Uri uri) throws IOException{
        try(InputStream stream=resolver.openInputStream(uri)){
            if(stream==null)throw new IOException("Datoteka nije dostupna.");
            return readBounded(stream);
        }
    }

    static void write(ContentResolver resolver,Uri uri,byte[] bytes)
            throws IOException{
        if(bytes==null||bytes.length>MAX_BYTES)
            throw new IOException("Sigurnosna kopija je prevelika.");
        // Explicit truncate avoids trailing stale bytes when overwriting a
        // previously longer SAF document. Not all providers support "w" truncation.
        try(OutputStream stream=resolver.openOutputStream(uri,"wt")){
            if(stream==null)throw new IOException("Datoteka nije dostupna.");
            writeBytes(stream,bytes);
        }
    }

    /** Strict 512-kB cap, even when provider streams have unknown lengths. */
    static byte[] readBounded(InputStream input) throws IOException{
        ByteArrayOutputStream result=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];
        int size=0,read;
        while((read=input.read(buffer))!=-1){
            size+=read;
            if(size>MAX_BYTES)throw new IOException("Datoteka je prevelika.");
            result.write(buffer,0,read);
        }
        return result.toByteArray();
    }

    static void writeBytes(OutputStream output,byte[] bytes) throws IOException{
        if(bytes==null||bytes.length>MAX_BYTES)
            throw new IOException("Sigurnosna kopija je prevelika.");
        output.write(bytes);
        output.flush();
    }
}
