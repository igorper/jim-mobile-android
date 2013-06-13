package net.pernek.jim.exercisedetector.database;

import android.net.Uri;
import android.provider.BaseColumns;

public final class JimTables {
    public static final String AUTHORITY = "net.pernek.jim.provider.JimContentProvider";

    // This class cannot be instantiated
    private JimTables() {}
    
    /**
     * Notes table
     */
    public static final class TrainingPlan implements BaseColumns {
        // This class cannot be instantiated
        private TrainingPlan() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/training_plans");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of training plans.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jim.training_plan";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single training plan.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jim.training_plan";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";

        /**
         * The name of the training plan.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";
    }
}

