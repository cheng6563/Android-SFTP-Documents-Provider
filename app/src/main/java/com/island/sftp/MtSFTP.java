package com.island.sftp;

import android.net.Uri;
import android.util.Log;

import com.island.androidsftpdocumentsprovider.provider.SFTPProvider;
import com.island.androidsftpdocumentsprovider.provider.Supplier;
import com.island.androidsftpdocumentsprovider.provider.SyncNetworkUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

public class MtSFTP extends SFTP {

    public MtSFTP(final Uri uri, final String password) throws ConnectException {
        super();
        try {
            SyncNetworkUtil.invoke(new Supplier<Void>() {
                @Override
                public Void get() throws IOException {
                    MtSFTP.super.init(uri, password);
                    return null;
                }
            });
        } catch (IOException e) {
            Log.e(SFTPProvider.TAG, "Init SFTP error", e);
            throw new ConnectException(e.getMessage());
        }
    }


    @Override
    public long lastModified(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<Long>() {
            @Override
            public Long get() throws IOException {
                return MtSFTP.super.lastModified(file);
            }
        });
    }

    @Override
    public long length(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<Long>() {
            @Override
            public Long get() throws IOException {
                return MtSFTP.super.length(file);
            }
        });
    }

    @Override
    public boolean isDirectory(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<Boolean>() {
            @Override
            public Boolean get() throws IOException {
                return MtSFTP.super.isDirectory(file);
            }
        });
    }

    @Override
    public void close() throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.close();
                return null;
            }
        });
    }

    @Override
    public File[] listFiles(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<File[]>() {
            @Override
            public File[] get() throws IOException {
                return MtSFTP.super.listFiles(file);
            }
        });
    }

    @Override
    public void newFile(final File file) throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.newFile(file);
                return null;
            }
        });
    }

    @Override
    public void delete(final File file) throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.delete(file);
                return null;
            }
        });
    }

    @Override
    public InputStream read(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<InputStream>() {
            @Override
            public InputStream get() throws IOException {
                return MtSFTP.super.read(file);
            }
        });
    }

    @Override
    public void mkdirs(final File file) throws IOException {

        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.mkdirs(file);
                return null;
            }
        });
    }

    @Override
    public boolean exists(final File file) throws IOException {

        return SyncNetworkUtil.invoke(new Supplier<Boolean>() {
            @Override
            public Boolean get() throws IOException {
                return MtSFTP.super.exists(file);
            }
        });
    }

    @Override
    public void renameTo(final File oldPath, final File newPath) throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.renameTo(oldPath, newPath);
                return null;
            }
        });
    }

    @Override
    public OutputStream write(final File file) throws IOException {
        return SyncNetworkUtil.invoke(new Supplier<OutputStream>() {
            @Override
            public OutputStream get() throws IOException {
                return MtSFTP.super.write(file);

            }
        });
    }

    @Override
    public void get(final File from, final File to) throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.get(from, to);
                return null;
            }
        });
    }

    @Override
    public void copy(final File from, final File to) throws IOException {
        SyncNetworkUtil.invoke(new Supplier<Void>() {
            @Override
            public Void get() throws IOException {
                MtSFTP.super.copy(from, to);
                return null;
            }
        });
    }

}
