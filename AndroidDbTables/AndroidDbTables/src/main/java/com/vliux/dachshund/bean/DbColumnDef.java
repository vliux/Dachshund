package com.vliux.dachshund.bean;

import com.vliux.dachshund.Const;
import com.vliux.dachshund.DbColumnType;

/**
 * Created by vliux on 12/8/13.
 */
public class DbColumnDef {
    private String column;
    private DbColumnType type;
    private String defaultValue = "";
    private int introducedVersion = Const.MIN_VER_DEFAULT;
    private DbForeignKeyDef foreignKey;

    public DbForeignKeyDef getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(DbForeignKeyDef foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public DbColumnType getType() {
        return type;
    }

    public void setType(DbColumnType type) {
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
