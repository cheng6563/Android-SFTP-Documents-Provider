package com.island.androidsftpdocumentsprovider.provider;

import com.jcraft.jsch.SftpException;

import java.io.IOException;

public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws IOException;
}