package com.htsmart.wristband.sample.rxjava.result;

/**
 * Created by Kilnn on 2017/9/2.
 */

public class RequestEnterOTAResult {
    public boolean enter;//是否进入
    public int failedReason;//失败原因，1.电量低

    public RequestEnterOTAResult(boolean enter, int failedReason) {
        this.enter = enter;
        this.failedReason = failedReason;
    }
}
