package kkt.com.joggers.model;

import java.util.HashMap;
import java.util.Map;

public class Record {
    private int year; // 기록연도
    private int month; // 기록한 달
    private int day; // 기록일
    private float distance; // 달린 거리
    private int stepCount; // 걸음수
    private long time;

    // DataSnapshot.getValue(Record.class)를 위한 기본생성자
    Record() {
    }

    public Record(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        distance = 0;
        stepCount = 0;
        time = 0;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("day", day);
        result.put("distance", distance);
        result.put("stepCount", stepCount);
        result.put("time", time);

        return result;
    }

}
