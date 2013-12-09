Dachshund
=========

A ligt-weight framework of Android Sqlite DB, which manages automatic DB tables creation and upgrade.
On Android development with SQLite, a lot of us have to manually generate SQL strings in onCreate() and onUpgrade(). With this simple framework, just annoate your class fields as database columns, you get your tables created or upgraded in time.

How to use?
Step 1. Override BaseDbTable:

    @DbTable(tableName = "MyDbTable1", minVersion = 2)
    public class MyDbTable extends BaseDbTable{
        @DbField(columnType = DbType.TEXT, defaultValue = "some_value")
        private static String column_username = "userName";
        
        @DbField(columnType = DbType.INTEGER, defaultValue = "0", minVersion = 3)
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
        public void addRandom(){ insert(...); ... }
    }

As annotated, the table was added in DB version 2 (minVersion = 2), the column "username" was added in ver 2 also as it derives the value from table; while column "userage" was a new column added in version 3.

Step 2. Init DbManager:

    // here we specify dbVersion is 3, so if your current db versoin < 3, Dachshund will upgrade it by adding 'userAge' into table MyDbTable
    DbManager dbManager = DbManager.init(getContext().getApplicationContext(), 
        new Class<BaseDbTable>[]{MyDbTable.class}, 
        "myDataBase", 3);
    
Step 3. Use your table:

    MyDbTable dbTable = (MyDbTable)dbManager.getTable(MyDbTable.class);
    dbTable.addRandom(); // while trigger db creation or upgrade here
    
Step 4. THAT'S ALL!
