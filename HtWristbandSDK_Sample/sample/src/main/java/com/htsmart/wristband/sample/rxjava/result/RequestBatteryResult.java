package com.htsmart.wristband.sample.rxjava.result;

/**
 * Created by Kilnn on 2017/9/2.
 */

public class RequestBatteryResult {
    public int percentage;
    public int charging;

    public RequestBatteryResult(int percentage, int charging) {
        this.percentage = percentage;
        this.charging = charging;
    }
}
