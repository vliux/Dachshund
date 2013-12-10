package com.vliux.dachshund;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.annotation.DbField;
import com.vliux.dachshund.annotation.DbTable;
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by vliux on 12/6/13.
 */
public final class DbManager {
    public static final String TAG = "Dachshund";
    private static DbManager sInstance;

    private Context mAppContext;
    private DbHelper mDbHelper;

    private AnnotationParser mAnnotationParser = new AnnotationParser();
    private HashMap<Class, DbTableDef> mDbTableDefinitions = new HashMap<Class, DbTableDef>();
    private HashMap<Class, HashMap<Field, DbColumnDef>> mDbColumnDefinitions = new HashMap<Class, HashMap<Field, DbColumnDef>>();
    /* for fast indexing from field name to field object and to DbColumnDef */
    private HashMap<Class, HashMap<String, Field>> mDbFieldMaps = new HashMap<Class, HashMap<String, Field>>();

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
        for(Class tableClz : dbTableClasses){
            mAnnotationParser.detectAnnotations(tableClz);
        }
    }

    public static DbManager getsInstance(){
        if(null == sInstance){
            throw new IllegalStateException("must call init() first");
        }
        return sInstance;
    }

    public Class[] getTableClasses(){
        return mDbTableDefinitions.keySet().toArray(new Class[0]);
    }

    public DbTableDef getTableDefinition(Class tableClass){
        return mDbTableDefinitions.get(tableClass);
    }

    public HashMap<Field, DbColumnDef> getColumnDefinitions(Class tableClass){
        return mDbColumnDefinitions.get(tableClass);
    }

    public SQLiteOpenHelper getHelper(){
        return mDbHelper;
    }

    public Field findField(Class tableClass, String fieldName){
        if(mDbFieldMaps.containsKey(tableClass)){
            return mDbFieldMaps.get(tableClass).get(fieldName);
        }else{
            return null;
        }
    }

    /**
     * Parse annotations from table class
     */
    private class AnnotationParser{
        public void detectAnnotations(Class tableClass){
            Class theClass = tableClass;
            /* check DbTable annotation */
            DbTableDef dbTableDef = null;
            if(theClass.isAnnotationPresent(DbTable.class)){
                dbTableDef = new DbTableDef();
                DbTable dbTableAnnotation = (DbTable) theClass.getAnnotation(DbTable.class);
                dbTableDef.setMinVersion(dbTableAnnotation.minVersion());
                if(null == dbTableAnnotation.tableName() || dbTableAnnotation.tableName().length() <= 0){
                    dbTableDef.setTableName(tableClass.getSimpleName());
                }else{
                    dbTableDef.setTableName(dbTableAnnotation.tableName());
                }
                mDbTableDefinitions.put(tableClass, dbTableDef);
                Log.d(DbManager.TAG, String.format("table %s has DbTable annotation, minVersion = %d", dbTableDef.getTableName(), dbTableDef.getMinVersion()));
            }else{
                throw new IllegalArgumentException(String.format("Class %s is not annotated by DbTable", tableClass.getSimpleName()));
            }

            /* check DbField annotations */
            HashMap<Field, DbColumnDef> columnDefs = new HashMap<Field, DbColumnDef>();
            while(true){
                if(null == theClass || theClass.equals(BaseDbTable.class)){
                    break;
                }

                Field[] fields = theClass.getDeclaredFields();
                for (Field field : fields) {
                    Log.d(DbManager.TAG, "check field " + field.getName());
                    if (field.isAnnotationPresent(DbField.class)) {
                        String fieldName = field.getName();
                        Log.d(DbManager.TAG, String.format("field %s has DbField annotation", fieldName));
                        DbField dbField = field.getAnnotation(DbField.class);
                        String columnName = dbField.columnName();
                        DbType columnType = dbField.columnType();
                        String defaultValue = dbField.defaultValue();
                        int minVersion = dbField.minVersion();
                        if(minVersion <= 0){
                            if(null != dbTableDef && dbTableDef.getMinVersion() > 0){
                                minVersion = dbTableDef.getMinVersion();
                                Log.d(DbManager.TAG, "minVersion invalid from DbField, try to obtain from DbTable, which is " + minVersion);
                            }else{
                                throw new IllegalArgumentException(String.format("minVersion of annotation DbTable on class %s is invalid", tableClass.getName()));
                            }
                        }

                        if (null == columnName || columnName.length() <= 0) {
                            throw new IllegalArgumentException(String.format("invalid column name %s annotated at field %s",
                                    String.valueOf(columnName), fieldName));
                        }
                        Log.d(DbManager.TAG, "db columnName = " + columnName);

                        if (minVersion <= 0) {
                            throw new IllegalArgumentException(String.format("invalid minVersion %d annotated at field %s",
                                    minVersion, fieldName));
                        }

                        DbColumnDef dbColumnDef = new DbColumnDef();
                        dbColumnDef.setColumn(columnName);
                        dbColumnDef.setDefaultValue(defaultValue);
                        dbColumnDef.setIntroducedVersion(minVersion);
                        dbColumnDef.setType(columnType);
                        columnDefs.put(field, dbColumnDef);
                    }
                }
                theClass = theClass.getSuperclass();
            }
            if(columnDefs.size() > 0){
                mDbColumnDefinitions.put(tableClass, columnDefs);
            }
        }
    }
}
