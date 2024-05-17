public class LoopCal {

    private static String[] labelConvert(String s) {
        String[] output = new String[1];

        int pos = s.indexOf("label");
        String name = s.substring(pos + 5);
        output[0] = "(" + name + ")";

        return output;
    }

    private static String[] if_gotoConvert(String s) {
        String[] output = new String[10];

        int pos = s.indexOf("if-goto");
        String name = s.substring(pos + 7);

        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@" + name;
        output[6] = "D;JNE";

        return output;
    }

    private static String[] gotoConvert(String s) {
        String[] output = new String[10];

        int pos = s.indexOf("goto");
        String name = s.substring(pos + 4);

        output[0] = "@" + name;
        output[1] = "0;JMP";

        return output;
    }

    public static String[] loopConvert(String s) {

        if (s.contains("label")) return labelConvert(s);
        if (s.contains("if-goto")) return if_gotoConvert(s);
        else return gotoConvert(s);
    }
}
