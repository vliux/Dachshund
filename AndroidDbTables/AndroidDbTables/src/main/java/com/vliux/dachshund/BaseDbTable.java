package com.vliux.dachshund;

/**
 * Created by vliux on 9/21/13.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.annotation.DbField;
import com.vliux.dachshund.annotation.DbTable;
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseDbTable{
    public static final String PRIMARY_COLUMN_NAME = "_id";

    private int mId;

    public void setId(int id){
        mId = id;
    }

    public int getId(){
        return mId;
    }
}

