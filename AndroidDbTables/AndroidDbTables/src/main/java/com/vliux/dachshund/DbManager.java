package com.vliux.dachshund;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.vliux.dachshund.annotation.DbField;
import com.vliux.dachshund.annotation.DbTable;
import com.vliux.dachshund.annotation.ForeignKey;
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by vliux on 12/6/13.
 */
public class DbManager {
    public static final String TAG = "Dachshund";
    private static DbManager sInstance;

    private Context mAppContext;
    private DbHelper mDbHelper;
    private HashMap<Class<BaseDbTable>, BaseDbTable> mDbTables;
    private AnnotationParser mAnnotationParser;

    /* store column definitions of tables */
    private HashMap<Class, HashMap<String, DbColumnDef>> mTableColumnDefinitions =
            new HashMap<Class, HashMap<String, DbColumnDef>>();
    /* store table definition of tables */
    private HashMap<Class, DbTableDef> mTableDefinitions =
            new HashMap<Class, DbTableDef>();

    /**
     * Initialize the database manager.
     * @param appContext the application context (should not be Activity)
     * @param dbTableClasses list of db table classes
     * @param dbName name of database
     * @param dbVersion current version of database
     * @return
     * @throws InstantiationException
     */
    public static DbManager init(Context appContext, Class[] dbTableClasses, String dbName, int dbVersion)
            throws InstantiationException {
        if(null == dbTableClasses || dbTableClasses.length <= 0){
            throw new IllegalArgumentException("none of BaseDbTable class is registered");
        }
        if(null == appContext){
            throw new IllegalArgumentException("application context provided is NULL");
        }else if(appContext instanceof Activity){
            throw new IllegalArgumentException("must provide an application context instead of Activity");
        }

        if(null == sInstance){
            synchronized (DbManager.class){
                if(null == sInstance){
                    sInstance = new DbManager(appContext, dbTableClasses, dbName, dbVersion);
                }
            }
        }
        return sInstance;
    }

    private DbManager(Context appContext, Class[] dbTableClasses, String dbName, int dbVersion) throws InstantiationException {
        mAppContext = appContext;
        mDbHelper = new DbHelper(mAppContext, dbName, dbVersion);
        mDbTables = new HashMap<Class<BaseDbTable>, BaseDbTable>();
        mAnnotationParser = new AnnotationParser();

        for(Class tableClz : dbTableClasses){
            mAnnotationParser.detectAnnotations(tableClz);
            try {
                Constructor constructor = tableClz.getDeclaredConstructor(new Class[]{SQLiteOpenHelper.class, DbManager.class});
                mDbTables.put(tableClz, (BaseDbTable) constructor.newInstance(mDbHelper, this));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                InstantiationException thrownE =
                        new InstantiationException("unable to create instance of BaseDbTable, do you miss the constructor(SQLiteOpenHelper)?");
                thrownE.initCause(e);
                throw thrownE;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                InstantiationException thrownE =
                        new InstantiationException("unable to create instance of BaseDbTable, constructor(SQLiteOpenHelper) failed");
                thrownE.initCause(e);
                throw thrownE;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                InstantiationException thrownE =
                        new InstantiationException("unable to create instance of BaseDbTable, do you make the constructor(SQLiteOpenHelper) private?");
                thrownE.initCause(e);
                throw thrownE;
            }
        }
        mDbHelper.setDbTables(mDbTables.values().toArray(new BaseDbTable[0]));
    }

    public static DbManager getsInstance(){
        if(null == sInstance){
            throw new IllegalStateException("must call init() first");
        }
        return sInstance;
    }

    public BaseDbTable getTable(Class tableClass){
        return mDbTables.get(tableClass);
    }

    /**
     * Get Column definitions of the related table.
     * @param tableClass
     * @return Null if the table class is not registered.
     */
    public HashMap<String, DbColumnDef> getColumnDefs(Class tableClass){
        return mTableColumnDefinitions.get(tableClass);
    }

    /**
     *
     * @param tableClass
     * @return Null if the table class is not registered.
     */
    public DbTableDef getTableDef(Class tableClass){
        return mTableDefinitions.get(tableClass);
    }

    public String getTableName(Class tableClass){
        String name = null;
        if(mTableDefinitions.containsKey(tableClass)){
            DbTableDef dbTableDef = mTableDefinitions.get(tableClass);
            if(null != dbTableDef){
                name = dbTableDef.getTableName();
            }
        }

        if(TextUtils.isEmpty(name)){
            return tableClass.getSimpleName();
        }else{
            return name;
        }
    }

    private enum HitMode{
        DB_COLUMN, FOREIGN_KEY, NONE
    }

    private class AnnotationParser{
        public void detectAnnotations(Class tableClass) {
            /* check DbTable annotation */
            DbTableDef dbTableDef = null;
            if (tableClass.isAnnotationPresent(DbTable.class)) {
                dbTableDef = new DbTableDef();
                DbTable dbTableAnnotation = (DbTable) tableClass.getAnnotation(DbTable.class);
                dbTableDef.setMinVersion(dbTableAnnotation.minVersion());
                if(TextUtils.isEmpty(dbTableAnnotation.tableName())){
                    dbTableDef.setTableName(tableClass.getSimpleName());
                }else{
                    dbTableDef.setTableName(dbTableAnnotation.tableName());
                }
                mTableDefinitions.put(tableClass, dbTableDef);
                Log.d(DbManager.TAG, String.format("class %s has DbTable annotation, minVersion = %d", tableClass.getSimpleName(), dbTableDef.getMinVersion()));
            }

            HashMap<String, DbColumnDef> columnDefHashMap = new HashMap<String, DbColumnDef>();
            Class currentTableClass = tableClass;
            while (true) {
                if (null == currentTableClass || currentTableClass.equals(BaseDbTable.class)) {
                    break;
                }
                Field[] fields = currentTableClass.getDeclaredFields();
                for (Field field : fields) {
                    Log.d(DbManager.TAG, "check field " + field.getName());
                    HitMode hitMode = HitMode.NONE;
                    if (field.isAnnotationPresent(DbField.class)) {
                        hitMode = HitMode.DB_COLUMN;
                    } else if (field.isAnnotationPresent(ForeignKey.class)) {
                        hitMode = HitMode.FOREIGN_KEY;
                    }

                    String fieldName = null;
                    String columnName = null;
                    if (hitMode != HitMode.NONE) {
                        fieldName = field.getName();
                        columnName = Util.getColumnName(field);
                        if (null == columnName || columnName.length() <= 0) columnName = fieldName;
                        if (null == columnName || columnName.length() <= 0) {
                            throw new IllegalArgumentException(String.format("invalid column name %s annotated at field %s",
                                    String.valueOf(columnName), fieldName));
                        }
                        Log.d(DbManager.TAG, "db columnName = " + columnName);
                    } else {
                        continue;
                    }

                    DbColumnDef dbColumnDef = null;
                    if (hitMode == HitMode.DB_COLUMN) {
                        Log.d(DbManager.TAG, String.format("field %s has DbField annotation", fieldName));
                        DbField dbField = field.getAnnotation(DbField.class);
                        DbType columnType = dbField.columnType();
                        String defaultValue = dbField.defaultValue();
                        int minVersion = getMinVersion(dbField, dbTableDef);
                        if (minVersion <= 0) {
                            throw new IllegalArgumentException(String.format("invalid minVersion %d annotated at field %s",
                                    minVersion, fieldName));
                        }
                        dbColumnDef = new DbColumnDef();
                        dbColumnDef.setColumn(columnName);
                        dbColumnDef.setDefaultValue(defaultValue);
                        dbColumnDef.setIntroducedVersion(dbField.minVersion());
                        dbColumnDef.setType(columnType);
                    } else if (hitMode == HitMode.FOREIGN_KEY) {
                        Log.d(DbManager.TAG, String.format("field %s has ForeignKey annotation", fieldName));
                        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
                        Class referToClz = foreignKey.referTo();
                        if (null == referToClz) {
                            throw new IllegalArgumentException(String.format("invalid foreign key %s defined: referTo is null", fieldName));
                        }
                        int minVersion = getMinVersion(foreignKey, dbTableDef);
                        if (minVersion <= 0) {
                            throw new IllegalArgumentException(String.format("invalid minVersion %d annotated at field %s",
                                    minVersion, fieldName));
                        }

                        dbColumnDef = new DbColumnDef();
                        dbColumnDef.setColumn(columnName);
                        dbColumnDef.setType(DbType.FOREIGN_KEY);
                        dbColumnDef.setIntroducedVersion(minVersion);
                        dbColumnDef.setForeignReferTo(referToClz);
                    }

                    if (null != dbColumnDef) {
                        columnDefHashMap.put(columnName, dbColumnDef);
                    }
                }
                currentTableClass = currentTableClass.getSuperclass();
            }
            mTableColumnDefinitions.put(tableClass, columnDefHashMap);
        }

        private int getMinVersion(DbField dbField, DbTableDef dbTableDef){
            int minVersion = dbField.minVersion();
            if (minVersion <= 0 && null != dbTableDef) {
                minVersion = dbTableDef.getMinVersion();
                Log.d(DbManager.TAG,
                        "minVersion invalid from DbField, try to obtain from DbTable, which is " + minVersion);
            }
            return minVersion;
        }

        private int getMinVersion(ForeignKey foreignKey, DbTableDef dbTableDef){
            int minVersion = foreignKey.minVersion();
            if (minVersion <= 0 && null != dbTableDef) {
                minVersion = dbTableDef.getMinVersion();
                Log.d(DbManager.TAG,
                        "minVersion invalid from DbField, try to obtain from DbTable, which is " + minVersion);
            }
            return minVersion;
        }
    }
}
