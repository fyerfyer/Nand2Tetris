import java.io.*;
import java.util.ArrayList;
import java.io.IOException;
import java.util.ArrayList;

public class Tokenizer {

    static ArrayList<String> token = new ArrayList<>();
    static ArrayList<String> tokenLine = new ArrayList<>();
    static ArrayList<String> stringCollection = new ArrayList<>();

    static String tokenFile;

    static char[] symbol = {'{', '}', '(', ')', '[', ']', '.', ',',
            ';', '+', '-', '*', '/', '&', '|', '<',
            '>', '=', '~'};
    static String[] keyword = {"class", "constructor", "function", "method",
            "field", "static", "var", "int", "char", "boolean",
            "void", "true", "false", "null", "this", "let", "do", "if",
            "else", "while", "return"};
    static String[] type = {"int", "char", "boolean", "void"};
    static String[] className = {"Array", "Main"};

    //the collection of method call
    public static ArrayList<String> funcCollection = new ArrayList<>();
    public static String currentClass;

    private static boolean isSymbol(char ch) {
        for (char sym : symbol) {
            if (sym == ch) return true;
        }

        return false;
    }

    static {
        funcCollection.add("Keyboard.readInt");
        funcCollection.add("Output.printString");
        funcCollection.add("Output.printInt");
        funcCollection.add("Array.new");
    }

    private static boolean isVarDec(String s) {
        if (s.equals("field") || s.equals("static") || s.equals("var")) return true;
        return false;
    }

    private static boolean isSubroutineDec(String s) {
        if (s.equals("constructor") || s.equals("method") || s.equals("function")) return true;
        return false;
    }

    private static String keywordVal(String keywordName) {
        return "<keyword>" + " " + keywordName + " " + "</keyword>";
    }

    private static String symbolVal(String symName) {
        return "<symbol>" + " " + symName + " " + "</symbol>";
    }

    private static String identifierVal(String identifierName) {
        return "<identifier>" + " " + identifierName + " " + "</identifier>";
    }

    private static String intVal(String intName) {
        return "<integerConstant>" + " " + intName + " " + "</integerConstant>";
    }

    private static String stringVal(String stringName) {
        return "<stringConstant>" + " " + stringName + " " + "</stringConstant>";
    }

    public static boolean tokenHasNext() {
        return !token.isEmpty();
    }

    public static String tokenAdvance() {
        try {
            if (!token.isEmpty()) {
                String returnStr = token.get(0);
                token.remove(0);
                return returnStr;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static String getNextToken() {
        try {
            if (!token.isEmpty()) return token.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static boolean stringHasNext() {
        return !stringCollection.isEmpty();
    }

    public static String stringAdvance() {
        try {
            if (!stringCollection.isEmpty()) {
                String returnStr = stringCollection.get(0);
                stringCollection.remove(0);
                return returnStr;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static String getNextString() {
        try {
            if (!stringCollection.isEmpty()) return stringCollection.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static boolean tokenLineHasNext() {
        return !tokenLine.isEmpty();
    }

    public static String tokenLineAdvance() {
        try {
            if (!tokenLine.isEmpty()) {
                String returnStr = tokenLine.get(0);
                tokenLine.remove(0);
                return returnStr;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static String getNextTokenLine() {
        try {
            if (!tokenLine.isEmpty()) return tokenLine.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of index!");
        }

        return null;
    }

    public static void resetTokenizer() {
        token.clear();
        tokenLine.clear();
        stringCollection.clear();
    }

    public static void constructor(String filename) {
        try {
            String tokenfile = filename.substring(0, filename.length() - 5) + "T.xml";
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tokenfile));
            tokenFile = tokenfile;

            String line = Operation.modify(reader.readLine());
            writer.write("<tokens>");
            writer.newLine();
            while (line != null) {
                //if the token array doesn't have enough size, do resize.
                while (!line.isEmpty()) {
                    char ch = line.charAt(0);
                    //String Constant
                    if (ch == '"') {
                        line = line.substring(1);
                        int index = line.indexOf('"');
                        String stringName = line.substring(0, index);
                        writer.write(stringVal(stringName));
                        writer.newLine();
                        token.add("stringConstant");
                        tokenLine.add(stringVal(stringName));
                        stringCollection.add(stringName);
                        line = line.substring(index + 1);
                        continue;
                    }

                    //integer Constant
                    if (Character.isDigit(ch)) {
                        int index = 0;
                        String num = Character.toString(ch);
                        while (Character.isDigit(line.charAt(index + 1))) {
                            num += Character.toString(line.charAt(index + 1));
                            index++;
                        }
                        writer.write(intVal(num));
                        writer.newLine();
                        token.add("integerConstant");
                        tokenLine.add(intVal(num));
                        stringCollection.add(num);
                        line = line.substring(index + 1);
                        continue;
                    }

                    boolean flag = false;

                    if (isSymbol(ch)) {
                        writer.write(symbolVal(Character.toString(ch)));
                        writer.newLine();
                        token.add("symbol");
                        tokenLine.add(symbolVal(Character.toString(ch)));
                        stringCollection.add(Character.toString(ch));
                        line = line.substring(1);
                        flag = true;
                    }

                    if (flag) continue;

                    //identifier and keyword
                    for (String keywordName : keyword) {
                        if (line.indexOf(keywordName) == 0) {
                            writer.write(keywordVal(keywordName));
                            writer.newLine();
                            token.add("keyword");
                            tokenLine.add(keywordVal(keywordName));
                            stringCollection.add(keywordName);
                            line = line.substring(keywordName.length());
                            flag = true;

                            //there are two special patterns: varDec and subroutineDec
                            if (isVarDec(keywordName)) {
                                //the first is type dec
                                boolean tag = false;
                                for (String s : type) {
                                    if (line.indexOf(s) == 0) {
                                        tag = true;
                                        writer.write(keywordVal(s));
                                        writer.newLine();
                                        token.add("keyword");
                                        tokenLine.add(keywordVal(s));
                                        stringCollection.add(s);
                                        line = line.substring(s.length());
                                        break;
                                    }
                                }

                                if (!tag) {
                                    for (String s : className) {
                                        if (line.indexOf(s) == 0) {
                                            writer.write(identifierVal(s));
                                            writer.newLine();
                                            token.add("identifier");
                                            tokenLine.add(identifierVal(s));
                                            stringCollection.add(s);
                                            line = line.substring(s.length());
                                            break;
                                        }
                                    }
                                }

                                //then comes the varName
                                String varName;

                                if (line.contains(",") && line.indexOf(",") < line.indexOf(";")) {
                                    varName = line.substring(0, line.indexOf(","));
                                    writer.write(identifierVal(varName));
                                    writer.newLine();
                                    token.add("identifier");
                                    tokenLine.add(identifierVal(varName));
                                    stringCollection.add(varName);
                                    line = line.substring(varName.length());

                                    //we need to consider if there exist a ','
                                    while (line.charAt(0) == ',') {
                                        writer.write(identifierVal(","));
                                        writer.newLine();
                                        token.add("identifier");
                                        tokenLine.add(identifierVal(","));
                                        stringCollection.add(",");
                                        line = line.substring(1);

                                        if (line.contains(",") && line.indexOf(",") < line.indexOf(";")) {
                                            varName = line.substring(0, line.indexOf(","));
                                        } else {
                                            varName = line.substring(0, line.indexOf(";"));
                                        }

                                        writer.write(identifierVal(varName));
                                        writer.newLine();
                                        token.add("identifier");
                                        tokenLine.add(identifierVal(varName));
                                        stringCollection.add(varName);
                                        line = line.substring(varName.length());
                                    }

                                    writer.write(identifierVal(";"));
                                    writer.newLine();
                                    token.add("identifier");
                                    tokenLine.add(identifierVal(";"));
                                    stringCollection.add(";");
                                    line = line.substring(1);
                                } else { //no ',', just ';'
                                    varName = line.substring(0, line.indexOf(";"));
                                    writer.write(identifierVal(varName));
                                    writer.newLine();
                                    token.add("identifier");
                                    tokenLine.add(identifierVal(varName));
                                    stringCollection.add(varName);
                                    line = line.substring(varName.length());

                                    writer.write(identifierVal(";"));
                                    writer.newLine();
                                    token.add("identifier");
                                    tokenLine.add(identifierVal(";"));
                                    stringCollection.add(";");
                                    line = line.substring(1);
                                }
                                break;
                            }

                            //subroutineDec
                            if (isSubroutineDec(keywordName)) {
                                boolean tag = false;
                                for (String s : type) {
                                    if (line.indexOf(s) == 0) {
                                        tag = true;
                                        writer.write(keywordVal(s));
                                        writer.newLine();
                                        token.add("keyword");
                                        tokenLine.add(keywordVal(s));
                                        stringCollection.add(s);
                                        line = line.substring(s.length());
                                        break;
                                    }
                                }

                                if (!tag) {
                                    for (String s : className) {
                                        if (line.indexOf(s) == 0) {
                                            writer.write(identifierVal(s));
                                            writer.newLine();
                                            token.add("identifier");
                                            tokenLine.add(identifierVal(s));
                                            stringCollection.add(s);
                                            line = line.substring(s.length());
                                            break;
                                        }
                                    }
                                }

                                String varName = line.substring(0, line.indexOf("("));
                                funcCollection.add(currentClass + "." + varName);
                                writer.write(identifierVal(varName));
                                writer.newLine();
                                token.add("identifier");
                                tokenLine.add(identifierVal(varName));
                                stringCollection.add(varName);
                                line = line.substring(varName.length());
                                break;
                            }

                            //one special case: the className declaration
                            if (keywordName.equals("class")) {
                                String classname = line.substring(0, line.indexOf("{"));
                                currentClass = classname;
                                writer.write(identifierVal(classname));
                                writer.newLine();
                                token.add("identifier");
                                tokenLine.add(identifierVal(classname));
                                stringCollection.add(classname);
                                line = line.substring(classname.length());
                                break;
                            }
                        }
                    }

                    if (flag) continue;

                    //else, it's an identifier
                    String identifier = Character.toString(ch);
                    int index = 0;
                    while (!isSymbol(line.charAt(index + 1)) && line.charAt(index + 1) != '"') {
                        identifier += Character.toString(line.charAt(index + 1));
                        index++;
                    }
                    writer.write(identifierVal(identifier));
                    writer.newLine();
                    token.add("identifier");
                    tokenLine.add(identifierVal(identifier));
                    stringCollection.add(identifier);
                    line = line.substring(index + 1);
                }
                line = Operation.modify(reader.readLine());
            }

            writer.write("</tokens>");
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}