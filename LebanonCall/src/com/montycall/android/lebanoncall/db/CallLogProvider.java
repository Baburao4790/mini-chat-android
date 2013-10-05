package com.montycall.android.lebanoncall.db;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class CallLogProvider extends ContentProvider {

	public static final String AUTHORITY = "com.montycall.android.lebanoncall.provider.CallLogs";
	public static final String TABLE_NAME = "calllogs";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int CALL_LOG = 1;
	private static final int CALL_LOG_ID = 2;

	static {
		URI_MATCHER.addURI(AUTHORITY, "calllogs", CALL_LOG);
		URI_MATCHER.addURI(AUTHORITY, "calllogs/#", CALL_LOG_ID);
	}

	private static final String TAG = "monty.CallLogProvider";

	private SQLiteOpenHelper mOpenHelper;

	public CallLogProvider() {
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(url)) {

		case CALL_LOG:
			count = db.delete(TABLE_NAME, where, whereArgs);
			break;
		case CALL_LOG_ID:
			String segment = url.getPathSegments().get(1);

			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}

			count = db.delete(TABLE_NAME, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public String getType(Uri url) {
		int match = URI_MATCHER.match(url);
		switch (match) {
		case CALL_LOG:
			return CallLogConstants.CONTENT_TYPE;
		case CALL_LOG_ID:
			return CallLogConstants.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		if (URI_MATCHER.match(url) != CALL_LOG) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}

		ContentValues values = (initialValues != null) ? new ContentValues(
				initialValues) : new ContentValues();

		for (String colName : CallLogConstants.getRequiredColumns()) {
			if (values.containsKey(colName) == false) {
				throw new IllegalArgumentException("Missing column: " + colName);
			}
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(TABLE_NAME, CallLogConstants.DATE, values);

		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}

		Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(noteUri, null);
		return noteUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new CallLogDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = URI_MATCHER.match(url);

		switch (match) {
		case CALL_LOG:
			qBuilder.setTables(TABLE_NAME);
			break;
		case CALL_LOG_ID:
			qBuilder.setTables(TABLE_NAME);
			qBuilder.appendWhere("_id=");
			qBuilder.appendWhere(url.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = CallLogConstants.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs,
				null, null, orderBy);

		if (ret == null) {
			infoLog("CallLogProvider.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}

		return ret;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		long rowId = 0;
		int match = URI_MATCHER.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
		case CALL_LOG:
			count = db.update(TABLE_NAME, values, where, whereArgs);
			break;
		case CALL_LOG_ID:
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update(TABLE_NAME, values, "_id=" + rowId, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update URL: " + url);
		}

		infoLog("*** notifyChange() rowId: " + rowId + " url " + url);

		getContext().getContentResolver().notifyChange(url, null);
		return count;

	}

	private static void infoLog(String data) {
		
			//Log.i(TAG, data);
		
	}

	private static class CallLogDatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "montycall.db";
		private static final int DATABASE_VERSION = 2;

		public CallLogDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			infoLog("creating new call log table");
			

			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + CallLogConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ CallLogConstants.DATE + " INTEGER,"
					+ CallLogConstants.DURATION + " INTEGER,"
					+ CallLogConstants.DIRECTION + " INTEGER,"
					+ CallLogConstants.NUMBER + " TEXT,"
					+ CallLogConstants.NAME + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			infoLog("onUpgrade: from " + oldVersion + " to " + newVersion);
			switch (oldVersion) {
			case 3:
				db.execSQL("UPDATE " + TABLE_NAME + " SET READ=1");
			default:
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
				onCreate(db);
			}
		}

	}

	public static final class CallLogConstants implements BaseColumns {

		private CallLogConstants() {
		}

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monty.call";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monty.call";
		public static final String DEFAULT_SORT_ORDER = CallLogConstants.DATE
				+ " ASC";

		public static final String DATE = "date";
		public static final String DIRECTION = "from_me";
		public static final String NUMBER = "number";
		public static final String NAME = "name";
		public static final String DURATION = "duration";

		// boolean mappings
		public static final int INCOMING = 0;
		public static final int OUTGOING = 1;
		public static final int MISSED = 2; 

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(DATE);
			tmpList.add(DIRECTION);
			tmpList.add(NUMBER);
			tmpList.add(NAME);
			tmpList.add(DURATION);
			return tmpList;
		}
	}
}
