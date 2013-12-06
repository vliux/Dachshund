Dachshund
=========

A simple framework of Android Sqlite DB which supports table creation and upgrade.

How to use?
Step 1. Override BaseDbTable:

    public class MyDbTable extends BaseDbTable{
        private static final String DB_COL_<column-identifier>_<data-type>_<min-version> = "<column-name-in-db>";
        // for example:
        // private static final String DB_COL_USER_NAME_TEXT_1 = "userName";
        
        @Override
        public void onTableCreated(SQLiteDatabase db){
            // make initialization after your table is right created
            // normally you can insert initial rows into table
        }
        
        // implement your additional logic below
        public void addSomething(){...}
    }
    
Step 2. Init DbManager:

    DbManager dbManager = DbManager.init(getContext().getApplicationContext(), new Class<BaseDbTable>[]{MyDbTable.class});
    
Step 3. Use your table:

    MyDbTable dbTable = (MyDbTable)dbManager.getTable(MyDbTable.class);
    dbTable.addSomething();
    
Step 4. THAT'S ALL!
