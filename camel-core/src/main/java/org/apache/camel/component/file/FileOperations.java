/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File operations for {@link java.io.File}.
 */
public class FileOperations implements GenericFileOperations<File> {
    private static final transient Log LOG = LogFactory.getLog(FileOperations.class);
    private FileEndpoint endpoint;

    public FileOperations() {
    }

    public FileOperations(FileEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpoint(GenericFileEndpoint endpoint) {
        this.endpoint = (FileEndpoint) endpoint;
    }

    public boolean deleteFile(String name) throws GenericFileOperationFailedException {        
        File file = new File(name);

        // do not try to delete non existing files
        if (!file.exists()) {
            return false;
        }

        // some OS such as Windows can have problem doing delete IO operations so we may need to
        // retry a couple of times to let it work
        boolean deleted = false;
        int count = 0;
        while (!deleted && count < 3) {
            if (LOG.isDebugEnabled() && count > 0) {
                LOG.debug("Retrying attempt " + count + " to delete file: " + name);
            }

            deleted = file.delete();
            if (!deleted && count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            count++;
        }


        if (LOG.isDebugEnabled() && count > 0) {
            LOG.debug("Tried " + count + " to delete file: " + name + " with result: " + deleted);
        }
        return deleted;
    }

    public boolean renameFile(String from, String to) throws GenericFileOperationFailedException {
        File file = new File(from);
        File target = new File(to);

        // do not try to rename non existing files
        if (!file.exists()) {
            return false;
        }

        // some OS such as Windows can have problem doing rename IO operations so we may need to
        // retry a couple of times to let it work
        boolean renamed = false;
        int count = 0;
        while (!renamed && count < 3) {
            if (LOG.isDebugEnabled() && count > 0) {
                LOG.debug("Retrying attempt " + count + " to rename file from: " + from + " to: " + to);
            }

            renamed = file.renameTo(target);
            if (!renamed && count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            count++;
        }

        if (LOG.isDebugEnabled() && count > 0) {
            LOG.debug("Tried " + count + " to rename file: " + from + " to: " + to + " with result: " + renamed);
        }
        return renamed;
    }

    public boolean existsFile(String name) throws GenericFileOperationFailedException {
        File file = new File(name);
        return file.exists();
    }

    public boolean buildDirectory(String directory, boolean absolute) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(endpoint, "endpoint");       

        // always create endpoint defined directory
        if (endpoint.isAutoCreate() && !endpoint.getFile().exists()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Building starting directory: " + endpoint.getFile());
            }
            endpoint.getFile().mkdirs();
        }

        if (ObjectHelper.isEmpty(directory)) {
            // no directory to build so return true to indicate ok
            return true;
        }

        File endpointPath = endpoint.getFile();
        File target = new File(directory);

        File path;
        if (absolute) {
            // absolute path
            path = target;
        } else if (endpointPath.equals(target)) {
            // its just the root of the endpoint path
            path = endpointPath;
        } else {
            // relative after the endpoint path
            String afterRoot = ObjectHelper.after(directory, endpointPath.getPath() + File.separator);
            if (ObjectHelper.isNotEmpty(afterRoot)) {
                // dir is under the root path
                path = new File(endpoint.getFile(), afterRoot);
            } else {
                // dir is relative to the root path
                path = new File(endpoint.getFile(), directory);
            }
        }

        if (path.isDirectory() && path.exists()) {
            // the directory already exists
            return true;
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Building directory: " + path);
            }
            return path.mkdirs();
        }
    }

    public List<File> listFiles() throws GenericFileOperationFailedException {
        // noop
        return null;
    }

    public List<File> listFiles(String path) throws GenericFileOperationFailedException {
        // noop
        return null;
    }

    public void changeCurrentDirectory(String path) throws GenericFileOperationFailedException {
        // noop
    }

    public String getCurrentDirectory() throws GenericFileOperationFailedException {
        // noop
        return null;
    }

    public boolean retrieveFile(String name, Exchange exchange) throws GenericFileOperationFailedException {
        // noop as we use type converters to read the body content for java.io.File
        return true;
    }

    public boolean storeFile(String fileName, Exchange exchange) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(endpoint, "endpoint");

        File file = new File(fileName);

        // if an existing file already exsists what should we do?
        if (file.exists()) {
            if (endpoint.getFileExist() == GenericFileExist.Ignore) {
                // ignore but indicate that the file was written
                if (LOG.isTraceEnabled()) {
                    LOG.trace("An existing file already exists: " + file + ". Ignore and do not override it.");
                }
                return true;
            } else if (endpoint.getFileExist() == GenericFileExist.Fail) {
                throw new GenericFileOperationFailedException("File already exist: " + file + ". Cannot write new file.");
            }
        }

        // we can write the file by 3 different techniques
        // 1. write file to file
        // 2. rename a file from a local work path
        // 3. write stream to file
        try {

            // is the body file based
            File source = null;
            if (exchange.getIn().getBody() instanceof File || exchange.getIn().getBody() instanceof GenericFile) {
                source = exchange.getIn().getBody(File.class);
            }

            if (source != null) {
                // okay we know the body is a file type

                // so try to see if we can optimize by renaming the local work path file instead of doing
                // a full file to file copy, as the local work copy is to be deleted afterwords anyway
                // local work path
                File local = exchange.getIn().getHeader(Exchange.FILE_LOCAL_WORK_PATH, File.class);
                if (local != null && local.exists()) {
                    boolean renamed = writeFileByLocalWorkPath(local, file);
                    if (renamed) {
                        // clear header as we have renamed the file
                        exchange.getIn().setHeader(Exchange.FILE_LOCAL_WORK_PATH, null);
                        // return as the operation is complete, we just renamed the local work file
                        // to the target.
                        return true;
                    }
                } else if (source.exists()) {
                    // no there is no local work file so use file to file copy if the source exists
                    writeFileByFile(source, file);
                    return true;
                }
            }

            // fallback and use stream based
            InputStream in = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
            writeFileByStream(in, file);
            return true;
        } catch (IOException e) {
            throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
        } catch (InvalidPayloadException e) {
            throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
        }
    }

    private boolean writeFileByLocalWorkPath(File source, File file) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Using local work file being renamed from: " + source + " to: " + file);
        }
        return source.renameTo(file);
    }

    private void writeFileByFile(File source, File target) throws IOException {
        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = null;
        try {
            out = prepareOutputFileChannel(target, out);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Using FileChannel to transfer from: " + in + " to: " + out);
            }
            in.transferTo(0, in.size(), out);
        } finally {
            ObjectHelper.close(in, source.getName(), LOG);
            ObjectHelper.close(out, source.getName(), LOG);
        }
    }

    private void writeFileByStream(InputStream in, File target) throws IOException {
        FileChannel out = null;
        try {
            out = prepareOutputFileChannel(target, out);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Using InputStream to transfer from: " + in + " to: " + out);
            }
            int size = endpoint.getBufferSize();
            byte[] buffer = new byte[size];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while (true) {
                int count = in.read(buffer);
                if (count <= 0) {
                    break;
                } else if (count < size) {
                    byteBuffer = ByteBuffer.wrap(buffer, 0, count);
                    out.write(byteBuffer);
                    break;
                } else {
                    out.write(byteBuffer);
                    byteBuffer.clear();
                }
            }
        } finally {
            ObjectHelper.close(in, target.getName(), LOG);
            ObjectHelper.close(out, target.getName(), LOG);
        }
    }

    /**
     * Creates and prepares the output file channel. Will position itself in correct position if eg. it should append
     * or override any existing content.
     */
    private FileChannel prepareOutputFileChannel(File target, FileChannel out) throws IOException {
        if (endpoint.getFileExist() == GenericFileExist.Append) {
            out = new RandomAccessFile(target, "rw").getChannel();
            out = out.position(out.size());
        } else {
            // will override
            out = new FileOutputStream(target).getChannel();
        }
        return out;
    }
}