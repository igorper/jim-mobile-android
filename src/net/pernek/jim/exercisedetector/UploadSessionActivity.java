package net.pernek.jim.exercisedetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UploadSessionActivity extends ListActivity {

	private ResponseReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateUi();
		
		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_UPLOAD_DONE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
	}

	private void updateUi() {
		File f = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());

		File[] files = f.listFiles();
		List<String> fileList = new ArrayList<String>(files.length);
		for (File file : files) {
			fileList.add(file.getName());
		}

		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, fileList);
		setListAdapter(directoryList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String file = (String) l.getItemAtPosition(position);

		Intent intent = new Intent(this, DataUploaderService.class);
		intent.putExtra(DataUploaderService.INTENT_KEY_FILE,
				Environment.getExternalStorageDirectory() + "/jimdata/" + file);
		startService(intent);
	}

	class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_UPLOAD_DONE = "upload_done";
		
		public static final String PARAM_STATUS = "status";

		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getExtras().getInt(PARAM_STATUS);
			Toast.makeText(getApplicationContext(), 
					"Upload finnished with code " + Integer.toString(status), 
					Toast.LENGTH_SHORT).show();
			updateUi();
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
}
