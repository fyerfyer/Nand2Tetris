public class StringOperation {
    public static String modify(String ss) {
        if (ss == null) return ss;

        int index = ss.indexOf("//");
        String s = ss;
        if (index != -1) s = ss.substring(0, index);
        return s.replaceAll("\\s", "");
    }

}
