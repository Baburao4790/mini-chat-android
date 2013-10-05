package com.montycall.android.lebanoncall.db;


public class DbUtility {
	public static final String DATABASE_NAME = "spactron.db";
	public static final int DATABASE_VERSION = 1;

	public static final String TABLE_NAME_CALLER_ID= "caller_ids";
	public static final String FIELD_ID = "FIELD_ID";
	public static final String CALLER_NUMBER = "CALLER_NUMBER";
	public static final String STATUS = "STATUS";
	public static final String CODE =  "CODE";

	//Query to create caller_ids table;
	public static final String CREATE_TABLE_CALLER_ID = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_CALLER_ID 
			+ " ("
			+ "FIELD_ID" 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "CALLER_NUMBER"			+ " TEXT,"
			+ "STATUS"					+ " INT,"
			+ "CODE"					+ " TEXT"
			+ ");";
}
