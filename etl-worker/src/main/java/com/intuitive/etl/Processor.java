package com.intuitive.etl;

import java.io.File;
import java.io.Writer;

public interface Processor {
    boolean canProcess(File file);

    int process(File inputFile, Writer outputWriter) throws Exception;
}
