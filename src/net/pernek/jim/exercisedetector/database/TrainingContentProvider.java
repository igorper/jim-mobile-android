package net.pernek.jim.exercisedetector.database;

import java.util.HashMap;

import net.pernek.jim.exercisedetector.database.JimTables.TrainingPlan;
import net.pernek.jim.exercisedetector.util.Utils;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

public class TrainingContentProvider extends ContentProvider {

	private static final String TAG = Utils.getApplicationTag();

	private static final String TRAINING_PLANS_TABLE_NAME = "training_plans";

	private static final String DATABASE_NAME = "jim.db";
	private static final int DATABASE_VERSION = 1;

	private static final int TRAINING_PLANS = 1;
	private static final int TRAINING_PLAN_ID = 2;

	private static HashMap<String, String> sTrainingPlansProjectionMap;

	private DatabaseHelper mOpenHelper;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TRAINING_PLANS_TABLE_NAME + " ("
					+ TrainingPlan._ID + " INTEGER PRIMARY KEY,"
					+ TrainingPlan.NAME + " TEXT" + ");");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TRAINING_PLANS_TABLE_NAME);
			onCreate(db);
		}

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS:
			count = db.delete(TRAINING_PLANS_TABLE_NAME, where, whereArgs);
			break;

		case TRAINING_PLAN_ID:
			String trainingPlanId = uri.getPathSegments().get(1);
			count = db.delete(
					TRAINING_PLANS_TABLE_NAME,
					TrainingPlan._ID
							+ "="
							+ trainingPlanId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS:
			return TrainingPlan.CONTENT_TYPE;

		case TRAINING_PLAN_ID:
			return TrainingPlan.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TRAINING_PLANS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		// Make sure that the fields are all set
		if (!values.containsKey(TrainingPlan._ID)
				|| !values.containsKey(TrainingPlan.NAME)) {
			throw new IllegalArgumentException("Incomplete training plan data.");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TRAINING_PLANS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(TrainingPlan.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TRAINING_PLANS_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS:
			qb.setProjectionMap(sTrainingPlansProjectionMap);
			break;

		case TRAINING_PLAN_ID:
			qb.setProjectionMap(sTrainingPlansProjectionMap);
			qb.appendWhere(TrainingPlan._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = TrainingPlan.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS:
			count = db.update(TRAINING_PLANS_TABLE_NAME, values, where,
					whereArgs);
			break;

		case TRAINING_PLAN_ID:
			String trainingPlanId = uri.getPathSegments().get(1);
			count = db.update(
					TRAINING_PLANS_TABLE_NAME,
					values,
					TrainingPlan._ID
							+ "="
							+ trainingPlanId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(JimTables.AUTHORITY, "training_plans",
				TRAINING_PLANS);
		sUriMatcher.addURI(JimTables.AUTHORITY, "training_plans/#",
				TRAINING_PLAN_ID);

		sTrainingPlansProjectionMap = new HashMap<String, String>();
		sTrainingPlansProjectionMap.put(TrainingPlan._ID, TrainingPlan._ID);
		sTrainingPlansProjectionMap.put(TrainingPlan.NAME, TrainingPlan.NAME);
	}

}
