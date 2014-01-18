package com.tuer.tuerist;

import android.app.Activity;
import android.os.FileObserver;

public class FileWatcher extends FileObserver {
	
	MainActivity main;

	public FileWatcher(String path, MainActivity main) {
		super(path);
		// TODO Auto-generated constructor stub
		this.startWatching();
		this.main = main;
	}

	@Override
	public void onEvent(int event, String path) {
		
		if(event == FileObserver.CREATE && !path.equals(".probe")){
		
			main.sendNotification();
		}
	}

}
