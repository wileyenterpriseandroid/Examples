package com.enterpriseandroid.migrateclinic.data;

import java.io.InputStream;

import net.migrate.api.annotations.WebdataSchema;


@WebdataSchema(version="1")
public interface XRay {
    public String getSsn();
    public String getDescription();
    public String getNotes();
    public Long getTimestamp();
    public InputStream getXRay();
}
