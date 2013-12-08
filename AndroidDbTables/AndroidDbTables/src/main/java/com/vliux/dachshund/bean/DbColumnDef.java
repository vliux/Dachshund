package com.vliux.dachshund.bean;

import com.vliux.dachshund.DbType;

/**
 * Created by vliux on 12/8/13.
 */
public class DbColumnDef {
    private String column;
    private DbType type;
    private String defaultValue = "";
    private int introducedVersion = -1;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public DbType getType() {
        return type;
    }

    public void setType(DbType type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getIntroducedVersion() {
        return introducedVersion;
    }

    public void setIntroducedVersion(int introducedVersion) {
        this.introducedVersion = introducedVersion;
    }
}
