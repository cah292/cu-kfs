package edu.cornell.kfs.sys.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class ByteContent {

    private final byte[] content;

    public ByteContent(byte[] content) {
        this.content = content;
    }

    public ByteContent(Supplier<byte[]> contentSupplier) {
        this.content = contentSupplier.get();
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(content);
    }

}
