package com.enterpriseandroid.migrateclinic.data;

import net.migrate.api.annotations.WebdataSchema;


@WebdataSchema(version="1")
public interface Patient {
    public String getSsn();
    public String getFirstname();
    public String getLastname();
    public String getInsurer();
}
