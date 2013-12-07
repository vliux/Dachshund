Dachshund
=========

A simple framework of Android Sqlite DB which supports table creation and upgrade.
Just annoate your class fields as database columns, you get the support of automatic database creation and upgrade.

How to use?
Step 1. Override BaseDbTable:

    public class MyDbTable extends BaseDbTable{
        @DbField(columnType = "TEXT", defaultValue = "some_value", minVersion = 1)
        private static String column_username = "userName";
        
        @DbField(columnType = "INTEGER", defaultValue = "0", minVersion = 2)
        private static String column_userage = "userAge";

        @Override
        public void onTableCreated(SQLiteDatabase db){
            // make initialization after your table is right created
            // normally you can insert initial rows into table
        }
        
        @Override
        public void onTableUpgraded(SQLiteDatabase db, int oldVer, int newVer){
            // callback when your db is upgraded from oldVer to newVer
        }
        
        // implement your additional logic below
        public void addSomething(){...}
    }
    
Step 2. Init DbManager:

    // here we specify dbVersion is 2, so if your current db versoin is 1, Dachshund will upgrade it to 2 by adding 'userAge' into table MyDbTable
    DbManager dbManager = DbManager.init(getContext().getApplicationContext(), new Class<BaseDbTable>[]{MyDbTable.class}, "myDataBase", 2);
    
Step 3. Use your table:

    MyDbTable dbTable = (MyDbTable)dbManager.getTable(MyDbTable.class);
    dbTable.addSomething();
    
Step 4. THAT'S ALL!

Note!
For upgrade from old version, only new columns are added, not-used columns are not removed.
