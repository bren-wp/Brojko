package com.brendigo.brojko;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/** Pure stream tests; no Android device, file permissions or SAF provider needed. */
public final class BackupDocumentIoTest {
    @Test public void streamReadSupportsPartialReadsAndExactBoundary() throws Exception {
        byte[] expected=new byte[BackupDocumentIo.MAX_BYTES];
        for(int i=0;i<expected.length;i++)expected[i]=(byte)(i*31);
        InputStream partial=new ByteArrayInputStream(expected){
            @Override public synchronized int read(byte[] buffer,int offset,int length){
                return super.read(buffer,offset,Math.min(length,17));
            }
        };
        assertArrayEquals(expected,BackupDocumentIo.readBounded(partial));
    }

    @Test public void zeroLengthProviderReadStillMakesForwardProgress() throws Exception {
        byte[] expected=new byte[]{2,4,8,16};
        InputStream zeroOnce=new ByteArrayInputStream(expected){
            boolean first=true;
            @Override public synchronized int read(byte[] buffer,int offset,int length){
                if(first){first=false;return 0;}
                return super.read(buffer,offset,length);
            }
        };
        assertArrayEquals(expected,BackupDocumentIo.readBounded(zeroOnce));
    }

    @Test public void oversizedImportIsRejectedBeforeUnlimitedAllocation() throws Exception {
        byte[] oversized=new byte[BackupDocumentIo.MAX_BYTES+1];
        try{
            BackupDocumentIo.readBounded(new ByteArrayInputStream(oversized));
            fail("Oversized backup must be rejected");
        }catch(IOException expected){
            assertTrue(expected.getMessage().contains("prevelika"));
        }
    }

    @Test public void validDocumentWritesAllBytes() throws Exception {
        byte[] expected=new byte[]{0,1,2,3,4,5,(byte)255};
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        BackupDocumentIo.writeBytes(output,expected);
        assertArrayEquals(expected,output.toByteArray());
    }

    @Test public void oversizedExportDoesNotWritePartially() throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{
            BackupDocumentIo.writeBytes(output,
                new byte[BackupDocumentIo.MAX_BYTES+1]);
            fail("Oversized export must be rejected");
        }catch(IOException expected){
            assertEquals(0,output.size());
        }
    }

    @Test public void failedRemoteReadDoesNotReturnTruncatedBackup() throws Exception {
        InputStream fails=new InputStream(){
            private int calls;
            @Override public int read() throws IOException {
                if(++calls>7)throw new IOException("Provider read failed");
                return 12;
            }
        };
        try{
            BackupDocumentIo.readBounded(fails);
            fail("Read error must propagate to the UI");
        }catch(IOException expected){
            assertEquals("Provider read failed",expected.getMessage());
        }
    }
}
