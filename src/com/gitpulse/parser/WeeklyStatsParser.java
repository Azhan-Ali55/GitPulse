package com.gitpulse.parser;

import java.util.ArrayList;
import java.util.List;
import com.gitpulse.model.Repository;
import com.gitpulse.util.ErrorHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeeklyStatsParser extends BaseJsonParser {
    @Override
    protected void process(String json, Repository repository) {
        List<Integer> weeklyCommits = new ArrayList<>();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        // Loop to move through array and add weekly commits
        for (JsonElement element : array) {
            JsonObject week = element.getAsJsonObject();
            weeklyCommits.add(week.get("total").getAsInt());
        }
        repository.addWeeklyStats(weeklyCommits);
    }
    @Override
    protected String getParserName() {
        return "WeeklyStatsParser";
    }
}
