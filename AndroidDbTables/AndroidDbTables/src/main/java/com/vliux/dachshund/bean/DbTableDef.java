package com.vliux.dachshund.bean;

/**
 * Created by vliux on 12/8/13.
 */
public class DbTableDef {
    private String tableName;
    private int minVersion;

    public int getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(int minVersion) {
        this.minVersion = minVersion;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
