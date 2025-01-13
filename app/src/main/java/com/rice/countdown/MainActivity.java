package com.rice.countdown;

import android.content.SharedPreferences;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView countdownText;
    private PopupWindow popupWindow;
    private Calendar selectedCalendar;
    private CountDownTimer countDownTimer;
    private static final String PREFS_NAME = "CountdownPrefs";
    private static final String KEY_TITLE = "title";
    private static final String KEY_FINISH_MSG = "finish_msg";
    private static final String KEY_TARGET_TIME = "target_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAppLanguage("zh");
        
        // 读取保存的设置
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedTitle = prefs.getString(KEY_TITLE, null);
        String savedFinishMsg = prefs.getString(KEY_FINISH_MSG, null);
        String savedTargetTime = prefs.getString(KEY_TARGET_TIME, null);
        
        if (savedTitle != null && savedTargetTime != null) {
            LocalDateTime targetTime = LocalDateTime.parse(savedTargetTime, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            setCountdown(secondsToTargetTime(targetTime), savedTitle, savedFinishMsg);
        }
        // 初始化日历对象
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);

        // 打开弹出窗口的按钮
        Button openPopupButton = findViewById(R.id.openPopupButton);
        openPopupButton.setOnClickListener(v -> showPopupWindow());
    }

    private void setCountdown(long duration, String text,String finishMessage) {
        TextView countDownTitle = findViewById(R.id.countdown_title);
        if (text != null) {
            countDownTitle.setText(text);
        }
        countdownText = findViewById(R.id.timer);
        startCountdown(duration, finishMessage);
    }

    private void startCountdown(long seconds,String finishMessage) {
        long millisUntilFinished = seconds * 1000;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timerString = secondsToTimeFormatString(millisUntilFinished / 1000);
                countdownText.setText(timerString);
            }

            @Override
            public void onFinish() {
                countdownText.setText(finishMessage);
            }
        }.start();
    }

    private String secondsToTimeFormatString(long seconds) {

        // 获取小时、分钟和秒数
        long days = Duration.ofSeconds(seconds).toDaysPart();
        long hours = Duration.ofSeconds(seconds).toHoursPart();
        int minutes = Duration.ofSeconds(seconds).toMinutesPart();
        int secs = Duration.ofSeconds(seconds).toSecondsPart();
        // 创建时间字符串
        return String.format(Locale.getDefault(), "%02d : %02d : %02d : %02d", days, hours, minutes, secs);
    }

    private long secondsToTargetTime(LocalDateTime targetTime) {
        // 将当前时间和下一年元旦时间转换为同一时区的 ZonedDateTime 对象
        ZonedDateTime zonedNow = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault());
        ZonedDateTime zonedNextYearNewYear = ZonedDateTime.of(targetTime, ZoneId.systemDefault());
        // 计算时间差
        Duration duration = Duration.between(zonedNow, zonedNextYearNewYear);
        // 获取时间差的总秒数
        long seconds = duration.getSeconds();
        return seconds;
    }

    // 显示弹出窗口
    private void showPopupWindow() {
        // 加载布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_input, null);

        // 获取布局中的控件
        EditText titleInput = popupView.findViewById(R.id.titleInput);
        EditText finishMessageInput = popupView.findViewById(R.id.finnishInput);
        EditText timeInput = popupView.findViewById(R.id.timeInput);
        Button confirmButton = popupView.findViewById(R.id.confirmButton);

        // 设置时间输入框的点击事件
        timeInput.setOnClickListener(v -> showDateTimePicker(timeInput));

        // 创建 PopupWindow
        popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // 设置背景和动画
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
        popupWindow.setElevation(8);

        // 显示 PopupWindow
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // 确认按钮的点击事件
            confirmButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String finishMessage = finishMessageInput.getText().toString();
            String time = timeInput.getText().toString();

            if (!title.isEmpty() && !time.isEmpty()) {
                // 处理提交逻辑
                String result = "标题: " + title + "\n结束语:" + finishMessage + "\n时间: " + time;
                showResult(result);
                popupWindow.dismiss(); // 关闭弹出窗口
                //设置计时器
                // 将字符串解析为 LocalDateTime
                LocalDateTime targetTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                setCountdown(secondsToTargetTime(targetTime), title,finishMessage);
                
                // 保存设置
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(KEY_TITLE, title);
                editor.putString(KEY_FINISH_MSG, finishMessage);
                editor.putString(KEY_TARGET_TIME, time);
                editor.apply();
            } else {
                showResult("请填写完整信息");
            }
        });
    }

    // 显示日期和时间选择器
    private void showDateTimePicker(EditText timeInput) {
        // 日期选择器
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 时间选择器
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);

                                // 更新时间输入框
                                String dateTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:00", selectedCalendar).toString();
                                timeInput.setText(dateTime);
                            },
                            selectedCalendar.get(Calendar.HOUR_OF_DAY),
                            selectedCalendar.get(Calendar.MINUTE),
                            true // 24小时制
                    );
                    timePickerDialog.show();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // 显示结果
    private void showResult(String message) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void setAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
