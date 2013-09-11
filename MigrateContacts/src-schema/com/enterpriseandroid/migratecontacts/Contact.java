package com.enterpriseandroid.migratecontacts;

import net.migrate.api.annotations.WebDataSchema;

@WebDataSchema(version="1")
public interface Contact {
    public String getFirstname();
    public String getLastname();
    public String getEmail();
    public String getPhoneNumber();
}
