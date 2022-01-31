package com.botpanda.entities.enums;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

import java.lang.reflect.Type;

@JsonAdapter(TimeGranularity.Serializer.class)
public enum TimeGranularity{
    MINUTES1  (TimeUnits.MINUTES, 1),
    MINUTES5  (TimeUnits.MINUTES, 5),
    MINUTES15 (TimeUnits.MINUTES, 15),
    MINUTES30 (TimeUnits.MINUTES, 30),
    HOURS1    (TimeUnits.HOURS,   1),
    HOURS4    (TimeUnits.HOURS,   4);

    @Getter
    @Expose
    @ToString.Include
    final TimeUnits unit;

    @Getter
    @Expose
    @ToString.Include
    final int period;


    TimeGranularity(TimeUnits unit, int period){
        this.unit = unit;
        this.period = period;
    }

    public static TimeGranularity findByValues(String unit, int period){
        return findByValues(TimeUnits.valueOf(unit), period);
    }

    public static TimeGranularity findByValues(TimeUnits unit, int period){
        TimeGranularity last;
        for (TimeGranularity v : values()){
            if (v.unit.equals(unit) && v.period == period){
                return v;
            }
        }
        throw new IllegalArgumentException("Enum not found for unit:" + unit + " and period: " + period);
    }

    public int getSeconds(){
        return this.period * unit.seconds;
    }

    public int getMinutes(){
        return getSeconds() / 60;
    }

    static class Serializer implements JsonSerializer<TimeGranularity>, JsonDeserializer<TimeGranularity> {
        final static String UNIT = "unit";
        final static String PERIOD = "period";
        @Override
        public JsonElement serialize(TimeGranularity src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new Gson();
            JSONObject result = new JSONObject();
            result.put(UNIT, src.getUnit().name());
            result.put(PERIOD, src.getPeriod());
            return gson.fromJson(result.toString(), JsonElement.class);
        }

        @Override
        public TimeGranularity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            try {
                return findByValues(json.getAsJsonObject().get(UNIT).getAsString(), json.getAsJsonObject().get(PERIOD).getAsInt());
            } catch (JsonParseException e) {
                return MINUTES1;
            }
        }
    }
}