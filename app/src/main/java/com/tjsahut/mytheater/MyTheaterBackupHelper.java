package com.tjsahut.mytheater;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.tjsahut.mytheater.db.DBHelper;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MyTheaterBackupHelper extends BackupAgentHelper {

    // The name of the SharedPreferences file
    static final String PREFS = "synopsis";

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";

    // The name of the SharedPreferences file
    static final String DB = "../databases/cintetime.s3db";

    // A key to uniquely identify the set of backup data
    static final String FILES_BACKUP_KEY = "files";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
        FileBackupHelper helperF = new FileBackupHelper(this, DB);
        addHelper(FILES_BACKUP_KEY, helperF);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized (DBHelper.sDataLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized (DBHelper.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }
}
