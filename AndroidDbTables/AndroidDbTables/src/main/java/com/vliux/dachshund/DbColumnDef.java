package com.vliux.dachshund;

/**
 * Created by vliux on 12/8/13.
 */
public class DbColumnDef {
    private String column;
    private String type;
    private String defaultValue = "";
    private int introducedVersion = -1;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
