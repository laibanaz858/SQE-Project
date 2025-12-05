package com.yourcompany.hooks;

import com.yourcompany.utils.DataSourceManager;

public class DataSourceHooks {
    
    @io.cucumber.java.Before("@excel")
    public void setExcelSource() {
        DataSourceManager.setSource(DataSourceManager.Source.EXCEL);
    }
    
    @io.cucumber.java.Before("@database")
    public void setDatabaseSource() {
        DataSourceManager.setSource(DataSourceManager.Source.DATABASE);
    }
    
    @io.cucumber.java.Before("@redis")
    public void setRedisSource() {
        DataSourceManager.setSource(DataSourceManager.Source.REDIS);
    }
    
    @io.cucumber.java.Before("@hardcoded")
    public void setHardcodedSource() {
        DataSourceManager.setSource(DataSourceManager.Source.EXCEL);
    }
}