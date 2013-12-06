package com.vliux.dachshund;

import android.app.Activity;
import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by vliux on 12/6/13.
 */
public class DbManager {
    private static DbManager sInstance;

    private Context mAppContext;
    private DbHelper mDbHelper;
    private HashMap<Class<BaseDbTable>, BaseDbTable> mDbTables;

    public static DbManager init(Context appContext, Class<BaseDbTable>[] dbTableClasses)
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
                    sInstance = new DbManager(appContext, dbTableClasses);
                }
            }
        }
        return sInstance;
    }

    private DbManager(Context appContext, Class<BaseDbTable>[] dbTableClasses) throws InstantiationException {
        mAppContext = appContext;
        mDbHelper = new DbHelper(mAppContext);
        for(Class<BaseDbTable> tableClz : dbTableClasses){
            try {
                Constructor constructor = tableClz.getDeclaredConstructor(DbHelper.class);
                mDbTables.put(tableClz, (BaseDbTable) constructor.newInstance(mDbHelper));
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

    public DbManager getsInstance(){
        if(null == mAppContext){
            throw new IllegalStateException("must call init() first");
        }
        return sInstance;
    }

    public BaseDbTable getTable(Class<BaseDbTable> tableClass){
        return mDbTables.get(tableClass);
    }
}
