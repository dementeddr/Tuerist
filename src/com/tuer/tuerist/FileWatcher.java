package com.tuer.tuerist;

import android.app.Activity;
import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

public class FileWatcher extends FileObserver {
	
	MainActivity main;

	public FileWatcher(String path, MainActivity main) {
		super(path);
		// TODO Auto-generated constructor stub
//		this.startWatching();
		this.main = main;
	}

	@Override
	public void onEvent(int event, String path) {
		
		if(event == FileObserver.CREATE && !path.equals(".probe")){
		
			Log.v("Tuerist", "Picture taken");

			main.onNewPictureDetected();
//			main.updateData();
//			main.sendData();
		}
	}

}
