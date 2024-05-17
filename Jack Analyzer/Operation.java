import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Operation {
    public static String[] addToArray(String[] array, String newValue) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = newValue;
        return newArray;
    }

    private static String processString(String inputStr) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.find()) {
            String cleanedStr = inputStr.substring(0, matcher.start())
                    .replaceAll("\\s", "") +
                    matcher.group(0) +
                    inputStr.substring(matcher.end())
                            .replaceAll("\\s", "");
            return cleanedStr;
        } else {
            return inputStr.replaceAll("\\s", "");
        }
    }

    //wipe out the useless elements in the line
    public static String modify(String ss) {
        if (ss == null) return ss;

        int index = ss.indexOf("//");
        int index2 = ss.indexOf("/**");
        String s = ss;
        if (index != -1) s = ss.substring(0, index);
        if (index2 != -1) s = ss.substring(0, index2);
        String sss = processString(s);
        if (sss.indexOf("*") == 0) sss = "";
        return sss;
    }

}
