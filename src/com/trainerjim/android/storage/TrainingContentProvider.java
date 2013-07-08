package com.trainerjim.android.storage;

import java.util.HashMap;

import com.trainerjim.android.util.Utils;

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
import android.util.Log;

public class TrainingContentProvider extends ContentProvider {

	private static final String TAG = Utils.getApplicationTag();

	public static final String AUTHORITY = "net.pernek.jim.provider.JimContentProvider";

	private static final String DATABASE_NAME = "jim.db";
	private static final int DATABASE_VERSION = 1;

	private static final int TRAINING_PLANS = 1;
	private static final int TRAINING_PLAN_ID = 2;
	private static final int COMPLETED_TRAININGS = 3;
	private static final int COMPLETED_TRAINING_ID = 4;

	private static HashMap<String, String> sTrainingPlansProjectionMap;
	private static HashMap<String, String> sCompletedTrainingProjectionMap;

	private DatabaseHelper mOpenHelper;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TrainingPlan.TABLE_NAME + " ("
					+ TrainingPlan._ID + " INTEGER PRIMARY KEY,"
					+ TrainingPlan.NAME + " TEXT," + TrainingPlan.DATA
					+ " TEXT" + ");");
			db.execSQL("CREATE TABLE " + CompletedTraining.TABLE_NAME + " ("
					+ CompletedTraining._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ CompletedTraining.NAME + " TEXT,"
					+ CompletedTraining.DATA + " TEXT" + ");");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TrainingPlan.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CompletedTraining.TABLE_NAME);
			onCreate(db);
		}

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS: {
			count = db.delete(TrainingPlan.TABLE_NAME, where, whereArgs);
			break;
		}
		case TRAINING_PLAN_ID: {
			String trainingPlanId = uri.getPathSegments().get(1);
			count = db.delete(
					TrainingPlan.TABLE_NAME,
					TrainingPlan._ID
							+ "="
							+ trainingPlanId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		}
		case COMPLETED_TRAININGS: {
			count = db.delete(CompletedTraining.TABLE_NAME, where, whereArgs);
			break;
		}
		case COMPLETED_TRAINING_ID: {
			String completedTrainingId = uri.getPathSegments().get(1);
			count = db.delete(CompletedTraining.TABLE_NAME,
					CompletedTraining._ID
							+ "="
							+ completedTrainingId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		}
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

		case COMPLETED_TRAININGS:
			return CompletedTraining.CONTENT_TYPE;

		case COMPLETED_TRAINING_ID:
			return CompletedTraining.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) == TRAINING_PLANS) {

			ContentValues values;
			if (initialValues != null) {
				values = new ContentValues(initialValues);
			} else {
				values = new ContentValues();
			}

			// Make sure that the fields are all set
			if (!values.containsKey(TrainingPlan._ID)
					|| !values.containsKey(TrainingPlan.NAME)
					|| !values.containsKey(TrainingPlan.DATA)) {
				throw new IllegalArgumentException(
						"Incomplete training plan data.");
			}

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(TrainingPlan.TABLE_NAME, null, values);
			if (rowId > 0) {
				Uri noteUri = ContentUris.withAppendedId(
						TrainingPlan.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
		} else if (sUriMatcher.match(uri) == COMPLETED_TRAININGS){
			ContentValues values;
			if (initialValues != null) {
				values = new ContentValues(initialValues);
			} else {
				values = new ContentValues();
			}

			// Make sure that all required fields are set
			if (!values.containsKey(CompletedTraining.NAME)
					|| !values.containsKey(CompletedTraining.DATA)) {
				throw new IllegalArgumentException(
						"Incomplete completed training data.");
			}

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(CompletedTraining.TABLE_NAME, null, values);
			if (rowId > 0) {
				Uri noteUri = ContentUris.withAppendedId(
						CompletedTraining.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
			
		} else{
			throw new IllegalArgumentException("Unknown URI " + uri);
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

		switch (sUriMatcher.match(uri)) {
		case TRAINING_PLANS:
			qb.setTables(TrainingPlan.TABLE_NAME);
			qb.setProjectionMap(sTrainingPlansProjectionMap);
			break;

		case TRAINING_PLAN_ID:
			qb.setTables(TrainingPlan.TABLE_NAME);
			qb.setProjectionMap(sTrainingPlansProjectionMap);
			qb.appendWhere(TrainingPlan._ID + "="
					+ uri.getPathSegments().get(1));
			break;
			
		case COMPLETED_TRAININGS:
			qb.setTables(CompletedTraining.TABLE_NAME);
			qb.setProjectionMap(sCompletedTrainingProjectionMap);
			break;

		case COMPLETED_TRAINING_ID:
			qb.setTables(CompletedTraining.TABLE_NAME);
			qb.setProjectionMap(sCompletedTrainingProjectionMap);
			qb.appendWhere(CompletedTraining._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			switch(sUriMatcher.match(uri)){
			case TRAINING_PLANS:
			case TRAINING_PLAN_ID:
				orderBy = TrainingPlan.DEFAULT_SORT_ORDER;
				break;
				
			case COMPLETED_TRAININGS:
			case COMPLETED_TRAINING_ID:
				orderBy = CompletedTraining.DEFAULT_SORT_ORDER;
				break;
			default:
				orderBy = null;
				break;
			}
			
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = null;

		try {
			c = qb.query(db, projection, selection, selectionArgs, null, null,
					orderBy);
		} catch (Exception ex) {
			int ka = 0;
			ka++;
		}

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
			count = db.update(TrainingPlan.TABLE_NAME, values, where,
					whereArgs);
			break;

		case TRAINING_PLAN_ID:
			String trainingPlanId = uri.getPathSegments().get(1);
			count = db.update(
					TrainingPlan.TABLE_NAME,
					values,
					TrainingPlan._ID
							+ "="
							+ trainingPlanId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case COMPLETED_TRAININGS:
			count = db.update(CompletedTraining.TABLE_NAME, values, where,
					whereArgs);
			break;

		case COMPLETED_TRAINING_ID:
			String completedTrainingId = uri.getPathSegments().get(1);
			count = db.update(
					CompletedTraining.TABLE_NAME,
					values,
					CompletedTraining._ID
							+ "="
							+ completedTrainingId
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
		sUriMatcher.addURI(AUTHORITY, "training_plans", TRAINING_PLANS);
		sUriMatcher.addURI(AUTHORITY, "training_plans/#", TRAINING_PLAN_ID);
		sUriMatcher.addURI(AUTHORITY, "completed_trainings",
				COMPLETED_TRAININGS);
		sUriMatcher.addURI(AUTHORITY, "completed_trainings/#",
				COMPLETED_TRAINING_ID);

		sTrainingPlansProjectionMap = new HashMap<String, String>();
		sTrainingPlansProjectionMap.put(TrainingPlan._ID, TrainingPlan._ID);
		sTrainingPlansProjectionMap.put(TrainingPlan.NAME, TrainingPlan.NAME);
		sTrainingPlansProjectionMap.put(TrainingPlan.DATA, TrainingPlan.DATA);

		sCompletedTrainingProjectionMap = new HashMap<String, String>();
		sCompletedTrainingProjectionMap.put(CompletedTraining._ID,
				CompletedTraining._ID);
		sCompletedTrainingProjectionMap.put(CompletedTraining.NAME,
				CompletedTraining.NAME);
		sCompletedTrainingProjectionMap.put(CompletedTraining.DATA,
				CompletedTraining.DATA);
	}

	public static final class CompletedTraining implements BaseColumns {
		private CompletedTraining() {
		}

		// TODO: this could become a part of the content URI
		public static final String TABLE_NAME = "completed_trainings";


		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/completed_trainings");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * completed trainings.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jim.completed_training";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * completed training.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jim.completed_training";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "name ASC";

		/**
		 * The name of the completed training.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

		/**
		 * JSON encoded completed training data.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATA = "data";
	}

	/**
	 * Defines structure of the TrainingPlan table.
	 */
	public static final class TrainingPlan implements BaseColumns {
		// This class cannot be instantiated
		private TrainingPlan() {
		}

		public static final String TABLE_NAME = "training_plans";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/training_plans");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * training plans.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jim.training_plan";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * training plan.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jim.training_plan";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "name ASC";

		/**
		 * The name of the training plan.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

		/**
		 * JSON encoded training plan string.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATA = "data";
	}
}
