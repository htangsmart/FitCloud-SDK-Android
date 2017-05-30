package com.htsmart.wristband.sample.syncdata.db;

import android.content.Context;
import android.util.Log;

import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.sample.syncdata.entity.SyncRawDataEntity;

import java.util.List;

/**
 * Created by Kilnn on 2017/5/30.
 */

public class DbHelper {

    private static volatile DbHelper INSTANCE;

    private SyncRawDataEntityDao mDao;

    private DbHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "sample.db");
        DaoMaster master = new DaoMaster(helper.getWritableDb());
        mDao = master.newSession().getSyncRawDataEntityDao();
    }

    public static DbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DbHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DbHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    public void saveSyncRawData(int userId, List<SyncRawData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (SyncRawData data : datas) {
            //Copy and insert
            SyncRawDataEntity entity = new SyncRawDataEntity();
            entity.setUserId(userId);
            entity.setTime(data.getTimeStamp());
            entity.setType(data.getType());
            entity.setValue(data.getValue());
            entity.setValue2(data.getValue2());
            long rowId = mDao.insertOrReplace(entity);
            Log.d("DbHelper", "SyncRawDataEntity updated rowId:" + rowId);
        }
    }

}
