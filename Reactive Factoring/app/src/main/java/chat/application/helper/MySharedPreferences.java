package chat.application.helper;

/**
 * Created by Wasim on 12-Feb-16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySharedPreferences {
    private static String PRE_FILE_NAME="Pre_file";
    public static void saveToPreference(Context context,String preferenceName,String preferenceValue)
    {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();
    }
    public static String readFromPreference(Context context,String preferenceName,String defaultValue)
    {
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(preferenceName,defaultValue);

    }
    public static void saveBooleanToPreference(Context context,String preferenceName,boolean preferenceValue)
    {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(preferenceName,preferenceValue);
        editor.apply();
    }
    public static boolean readBooleanFromPreference(Context context,String preferenceName,boolean defaultValue)
    {
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(preferenceName,defaultValue);

    }
}
