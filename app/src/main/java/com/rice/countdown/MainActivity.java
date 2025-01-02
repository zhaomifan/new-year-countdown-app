package com.rice.countdown;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView countdownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countdownText = findViewById(R.id.timer);

        long duration = timeToNextYearNewYear();
//        duration = duration > 60 * 60 * 24 ? 0 : duration;
        startCountdown(duration);
    }

    private void startCountdown(long seconds) {
        long millisUntilFinished = seconds * 1000;
        new CountDownTimer(millisUntilFinished, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timerString = secondsToTimeFormatString(millisUntilFinished / 1000);
                countdownText.setText(timerString);
            }

            @Override
            public void onFinish() {
                countdownText.setText("元旦快乐!");
            }
        }.start();
    }

    public String secondsToTimeFormatString(long seconds) {

        // 获取小时、分钟和秒数
        long days = Duration.ofSeconds(seconds).toDaysPart();
        long hours = Duration.ofSeconds(seconds).toHoursPart();
        int minutes = Duration.ofSeconds(seconds).toMinutesPart();
        int secs = Duration.ofSeconds(seconds).toSecondsPart();
        // 创建时间字符串
        return String.format(Locale.getDefault(), "%02d : %02d : %02d : %02d", days, hours, minutes, secs);
    }


    public long timeToNextYearNewYear() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 获取下一年的元旦时间，先获取当前年份，加 1 得到下一年，然后设置为 1 月 1 日 00:00:00
        LocalDateTime nextYearNewYear = LocalDateTime.of(now.getYear() + 1, 1, 1, 0, 0, 0);
        // 将当前时间和下一年元旦时间转换为同一时区的 ZonedDateTime 对象
        ZonedDateTime zonedNow = ZonedDateTime.of(now, ZoneId.systemDefault());
        ZonedDateTime zonedNextYearNewYear = ZonedDateTime.of(nextYearNewYear, ZoneId.systemDefault());
        // 计算时间差
        Duration duration = Duration.between(zonedNow, zonedNextYearNewYear);
        // 获取时间差的总秒数
        long seconds = duration.getSeconds();
        return seconds;
    }
}