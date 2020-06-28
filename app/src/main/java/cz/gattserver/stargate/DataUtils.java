package cz.gattserver.stargate;

import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataUtils {

    public static final String PREFS_NAME = "STARGATE_MOBILE_DATA";
    public static final String VAR_NAME = "combination";
    public static final int COMBINATIONS_COUNT = 10;

    private DataUtils() {
    }

    public static List<String> readCombinations(ContextWrapper wrapper) {
        SharedPreferences settings = wrapper.getSharedPreferences(PREFS_NAME, 0);
        List<String> combinations = new ArrayList<>();
        for (int i = 0; i < COMBINATIONS_COUNT; i++)
            combinations.add(settings.getString(VAR_NAME + i, "0000000"));
        return combinations;
    }

    public static void saveCombinations(ContextWrapper wrapper, List<String> combinations) {
        SharedPreferences settings = wrapper.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < combinations.size(); i++)
            editor.putString(VAR_NAME + i, combinations.get(i));
        editor.commit();
    }
}
