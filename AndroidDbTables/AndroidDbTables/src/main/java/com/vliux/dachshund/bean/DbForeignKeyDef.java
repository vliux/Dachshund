package com.vliux.dachshund.bean;

import com.vliux.dachshund.annotation.ForeignKey;

/**
 * Created by vliux on 1/19/14.
 */
public class DbForeignKeyDef {
    private Class foreignReferTo = null;
    private ForeignKey.OnAction onDelete = ForeignKey.OnAction.RESTRICT;
    private ForeignKey.OnAction onUpdate = ForeignKey.OnAction.RESTRICT;

    public Class getForeignReferTo() {
        return foreignReferTo;
    }

    public void setForeignReferTo(Class foreignReferTo) {
        this.foreignReferTo = foreignReferTo;
    }

    public ForeignKey.OnAction getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(ForeignKey.OnAction onDelete) {
        this.onDelete = onDelete;
    }

    public ForeignKey.OnAction getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(ForeignKey.OnAction onUpdate) {
        this.onUpdate = onUpdate;
    }
}
