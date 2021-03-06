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
package org.apache.camel.component.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.converter.IOConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneIndexer {
    private static final transient Logger LOG = LoggerFactory.getLogger(LuceneIndexer.class);
    private File sourceDirectory;
    private Analyzer analyzer;
    private NIOFSDirectory niofsDirectory;
    private IndexWriter indexWriter;
    private boolean sourceDirectoryIndexed;
    private boolean indexCreated;
    
    public LuceneIndexer(File sourceDirectory, File indexDirectory, Analyzer analyzer)  throws Exception {
        if (indexDirectory != null) {
            if (!indexDirectory.exists()) {
                indexDirectory.mkdir();
            }   
            this.setNiofsDirectory(new NIOFSDirectory(indexDirectory));
        } else {
            this.setNiofsDirectory(new NIOFSDirectory(new File("./indexDirectory")));
        }
        
        this.setAnalyzer(analyzer);
        
        if ((sourceDirectory != null) && (!sourceDirectoryIndexed)) {
            this.setSourceDirectory(sourceDirectory);
            add(getSourceDirectory());
            sourceDirectoryIndexed = true;
        }
    }

    public void index(Exchange exchange) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Indexing " + exchange);
        }
        openIndexWriter();
        Map<String, Object> headers = exchange.getIn().getHeaders();
        add("exchangeId", exchange.getExchangeId(), true);
        for (Entry<String, Object> entry : headers.entrySet()) {
            String field = entry.getKey();
            String value = exchange.getContext().getTypeConverter().convertTo(String.class, entry.getValue());
            add(field, value, true);
        }

        add("contents", exchange.getIn().getMandatoryBody(String.class), true);
        closeIndexWriter();
    }

    public NIOFSDirectory getNiofsDirectory() {
        return niofsDirectory;
    }

    public void setNiofsDirectory(NIOFSDirectory niofsDirectory) {
        this.niofsDirectory = niofsDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private void add(String field, String value, boolean analyzed) throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Adding field: " + field);
            LOG.trace("       value: " + value);
        }

        Document doc = new Document();
        if (!analyzed) {
            doc.add(new Field(field, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
        } else {
            doc.add(new Field(field, value, Field.Store.YES, Field.Index.ANALYZED));
        }
        indexWriter.addDocument(doc);
    }

    private void add(File file) throws IOException {
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();

                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        add(new File(file.getAbsolutePath() + "/" + files[i]));
                    }
                }
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Adding " + file);
                }

                openIndexWriter();
                add("path", file.getPath(), false);
                add("contents", new String(IOConverter.toByteArray(file)), true);
                closeIndexWriter();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added " + file + " successfully");
                }
            }
        } else {
            LOG.warn("Directory/File " + file.getAbsolutePath() + " could not be read."
                + " This directory will not be indexed. Please check permissions and rebuild indexes.");
        }
    }

    private void openIndexWriter() throws IOException {
        if (!indexCreated) {
            indexWriter = new IndexWriter(niofsDirectory, getAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
            indexCreated = true;
            return;
        }
        indexWriter = new IndexWriter(niofsDirectory, getAnalyzer(), false, IndexWriter.MaxFieldLength.UNLIMITED);
    }

    private void closeIndexWriter() throws IOException {
        indexWriter.optimize();
        indexWriter.commit();
        indexWriter.close();
    }

}
