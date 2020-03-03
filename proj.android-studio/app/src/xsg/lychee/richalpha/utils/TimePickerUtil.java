package xsg.lychee.richalpha.utils;

import android.content.res.Resources;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.richalpha.R;

class TimePickerData {
    public long minMillSeconds;
    public long maxMillSeconds;
    public long curMillSeconds;
    public int type;
    public String timeFormat;
}

public class TimePickerUtil implements OnDateSetListener {
    private static TimePickerUtil _instance = null;
    private SimpleDateFormat dateFormat = null;

    public static TimePickerUtil getInstance() {
        if (_instance == null) {
            _instance = new TimePickerUtil();
        }
        return _instance;
    }

    /**
     * 显示时间选择器
     * @param params  TimePickerData json字符串
     */
    public static void show(String params) {
        TimePickerData data = JSON.parseObject(params, TimePickerData.class);
        TimePickerUtil.getInstance().show(data);
    }

    public void show(TimePickerData data) {
        AppActivity appActivity = (AppActivity) AppActivity.getContext();
        Resources resources = appActivity.getResources();

        if (data.timeFormat != null) {
            dateFormat = new SimpleDateFormat(data.timeFormat);
        }
        else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }


        Type type;
        switch (data.type) {
            case 1:
                type = Type.YEAR_MONTH_DAY;
                break;
            case 2:
                type = Type.HOURS_MINS;
                break;
            case 3:
                type = Type.MONTH_DAY_HOUR_MIN;
                break;
            case 4:
                type = Type.YEAR_MONTH;
                break;
            case 5:
                type = Type.YEAR;
                break;
                default:
                    type = Type.ALL;
                    break;
        }

        TimePickerDialog mDialogAll = new TimePickerDialog.Builder()
                .setMinMillseconds(data.minMillSeconds == 0 ? System.currentTimeMillis() : data.minMillSeconds)
                .setMaxMillseconds(data.maxMillSeconds == 0 ? System.currentTimeMillis() : data.maxMillSeconds)
                .setCurrentMillseconds(data.curMillSeconds == 0 ? System.currentTimeMillis() : data.curMillSeconds)
                .setType(type)
                .setCancelStringId(appActivity.getString(R.string.time_picker_cancel))
                .setSureStringId(appActivity.getString(R.string.time_picker_sure))
                .setTitleStringId(appActivity.getString(R.string.time_picker_title))
                .setYearText(appActivity.getString(R.string.time_picker_year))
                .setMonthText(appActivity.getString(R.string.time_picker_month))
                .setDayText(appActivity.getString(R.string.time_picker_day))
                .setHourText(appActivity.getString(R.string.time_picker_hour))
                .setMinuteText(appActivity.getString(R.string.time_picker_minute))
                .setCallBack(this)
                .setCyclic(false)
                .setThemeColor(resources.getColor(R.color.timepicker_dialog_bg))
                .setWheelItemTextNormalColor(resources.getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(resources.getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build();
        mDialogAll.show(appActivity.getSupportFragmentManager(), "all");
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerDialog, long millseconds) {
        String timeFormatStr = getDateToString(millseconds);
        JSONObject postMessage = new JSONObject();
        try {
            postMessage.put("timeFormatStr", timeFormatStr);
            postMessage.put("time", millseconds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JavaJsBridge.notifyEventToJs(BridgeEnum.TIME_PICKER, postMessage);
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        return dateFormat.format(d);
    }
}
