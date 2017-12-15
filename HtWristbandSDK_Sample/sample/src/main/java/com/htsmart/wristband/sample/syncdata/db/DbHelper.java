package com.htsmart.wristband.sample.syncdata.db;

import android.content.Context;
import android.util.Log;

import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.sample.syncdata.entity.SyncRawDataEntity;

import org.greenrobot.greendao.query.QueryBuilder;

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

    /**
     * Get the actual sleep start time between low time and high time.
     *
     * @param userId UserId
     * @param low    low time
     * @param high   high time
     * @return sleep start value
     */
    public SyncRawDataEntity getSleepStart(int userId, int low, int high) {
        QueryBuilder<SyncRawDataEntity> builder = mDao.queryBuilder();
        builder.where(
                SyncRawDataEntityDao.Properties.UserId.eq(userId),
                SyncRawDataEntityDao.Properties.Type.eq(SyncRawData.TYPE_SLEEP),
                SyncRawDataEntityDao.Properties.Time.between(low, high),
                SyncRawDataEntityDao.Properties.Value.notEq(SyncRawData.SLEEP_STATUS_SOBER)
        ).orderAsc(SyncRawDataEntityDao.Properties.Time).limit(1);
        List<SyncRawDataEntity> entities = builder.build().list();
        if (entities == null || entities.size() <= 0) {
            return null;
        } else {
            return entities.get(0);
        }
    }


    /**
     * Get the actual sleep end time between low time and high time.
     *
     * @param userId UserId
     * @param low    Low time
     * @param high   High time
     * @return Sleep end value
     */
    public SyncRawDataEntity getSleepEnd(int userId, int low, int high) {
        QueryBuilder<SyncRawDataEntity> builder = mDao.queryBuilder();
        builder.where(
                SyncRawDataEntityDao.Properties.UserId.eq(userId),
                SyncRawDataEntityDao.Properties.Type.eq(SyncRawData.TYPE_SLEEP),
                SyncRawDataEntityDao.Properties.Time.between(low, high),
                SyncRawDataEntityDao.Properties.Value.notEq(SyncRawData.SLEEP_STATUS_SOBER)
        ).orderDesc(SyncRawDataEntityDao.Properties.Time).limit(1);
        List<SyncRawDataEntity> entities = builder.build().list();
        if (entities == null || entities.size() <= 0) {
            return null;
        } else {
            return entities.get(0);
        }
    }

    /**
     * Get the all sleep datas between low time and high time.
     *
     * @param userId UserId
     * @param low    Low time
     * @param high   High time
     * @return Sleep datas
     */
    public List<SyncRawDataEntity> getSleepDataBetween(int userId, int low, int high) {
        return getDataBetween(SyncRawData.TYPE_SLEEP, userId, low, high);
    }

    public List<SyncRawDataEntity> getDataBetween(int dataType, int userId, int low, int high) {
        QueryBuilder<SyncRawDataEntity> builder = mDao.queryBuilder();
        builder.where(
                SyncRawDataEntityDao.Properties.UserId.eq(userId),
                SyncRawDataEntityDao.Properties.Type.eq(dataType),
                SyncRawDataEntityDao.Properties.Time.between(low, high)
        ).orderAsc(SyncRawDataEntityDao.Properties.Time);
        return builder.build().list();
    }

}
