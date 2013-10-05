package com.montycall.android.lebanoncall.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpactronDataBaseHelper extends SQLiteOpenHelper {

	public SpactronDataBaseHelper(Context context) {
		super(context, DbUtility.DATABASE_NAME, null, DbUtility.DATABASE_VERSION);
		
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DbUtility.CREATE_TABLE_CALLER_ID);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DbUtility.TABLE_NAME_CALLER_ID);
		onCreate(db);

	}	
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DbUtility.TABLE_NAME_CALLER_ID);
		onCreate(db);
	}
}
