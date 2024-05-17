import java.util.ArrayList;

public class VMWriter {
    private final static String[] op = {"+", "-", "*", "/", "&", "|", "<", ">", "="};
    private final static String[] unaryOp = {"-", "~"};

    public static String writePush(String segment, int index) {
        return "push" + " " + segment + " " + Integer.toString(index);
    }

    public static String writePop(String segment, int index) {
        return "pop" + " " + segment + " " + Integer.toString(index);
    }

    public static String writeOp(String command) {
        if (command.equals("+")) return "add";
        if (command.equals("-")) return "sub";
        if (command.equals("*")) return "call Math.multiply 2";
        if (command.equals("/")) return "call Math.divide 2";
        if (command.equals("&")) return "and";
        if (command.equals("|")) return "or";
        if (command.equals("<")) return "lt";
        if (command.equals(">")) return "gt";
        if (command.equals("=")) return "eq";
        return null;
    }

    public static String writeUnaryOp(String command) {
        if (command.equals("-")) return "neg";
        if (command.equals("~")) return "not";
        return null;
    }

    public static String writeCall(String name, int argNum) {
        return "call" + " " + name + " " + Integer.toString(argNum);
    }

    public static String writeFunctionDec(String name, int lclNum) {
        return "function" + " " + name + " " + Integer.toString(lclNum);
    }

    public static String writeFunctionCall(String name, int argNum) {
        return "call" + " " + name + " " + Integer.toString(argNum);
    }

    public static String writeReturn() {
        return "return";
    }

    public static ArrayList<String> writeString(String string) {
        ArrayList<String> str = new ArrayList<>();
        str.add(VMWriter.writePush("constant", string.length()));
        str.add(VMWriter.writeFunctionCall("String.new", 1));

        //we write in the string character by character
        for (int i = 0; i < string.length(); i ++) {
            int ascii = (int)string.charAt(i);
            str.add(VMWriter.writePush("constant", ascii));
            str.add(VMWriter.writeFunctionCall("String.appendChar", 2));
        }

        return str;
    }

}
