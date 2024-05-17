import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CompileEngine {
    private static StringBuilder className = new StringBuilder();
    private static StringBuilder curSubroutine = new StringBuilder();

    private final static String[] op = {"+", "-", "*", "/", "&", "|", "<", ">", "="};
    private final static String[] unaryOp = {"-", "~"};
    private final static String[] keywordConstant = {"true", "false", "null", "this"};
    private static String[] type = {"int", "char", "boolean", "Array", "void"};
    private final static String[] subroutineDec = {"function", "method", "constructor"};

    private static int ifLabelCount = 0;
    private static int whileLabelCount = 0;

    //only use the static int to recode the label index will lead to confusion in embeded statement
    //so we use an extra map 
    private static Map<Integer, Integer> loopCount = new HashMap<>();

    private static Map<String, String> funcType = new HashMap<>();

    static {
        funcType.put("Keyboard.readInt", "int");
        funcType.put("Output.printString", "void");
        funcType.put("Output.printInt", "void");
        funcType.put("Array.new", "Array");
    }

    private static void initType() {
        for (String s : Tokenizer.className) Operation.addToArray(type, s);
    }

    private static boolean containOP(String line) {
        for (String s : op) {
            if (line.contains(s)) return true;
        }

        return false;
    }

    private static boolean isKeywordConstant(String s) {
        for (int i = 0; i < type.length; i++) {
            if (s.equals(type[i])) return true;
        }

        return false;
    }

    private static boolean isSubroutineDec(String s) {
        for (int i = 0; i < subroutineDec.length; i++) {
            if (s.indexOf(subroutineDec[i]) == 0) return true;
        }

        return false;
    }

    private static boolean isOp(char ch) {
        for (String s : op) {
            if (Character.toString(ch).equals(s)) return true;
        }

        return false;
    }

    private static boolean isVarDec(String line){
        return (line.indexOf("var") == 0
                || line.indexOf("field") == 0
                || line.indexOf("static") == 0);
    }

    private static ArrayList<String> compileSubroutineCall(String line) {
        ArrayList<String> subroutine = new ArrayList<>();
        int argNum = 0;

        //we first store the functionName
        String funcName = line.substring(0, line.indexOf("("));
        line = line.substring(line.indexOf("("));
        if (!funcName.contains(".")) funcName = className + "." + funcName;

        //then comes the expressionList
        String expressionList = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
        if (!expressionList.isEmpty()) {
            ArrayList<String> expressions = compileExpressionList(expressionList);
            subroutine.addAll(expressions);
        }

        if (!expressionList.isEmpty()) {
            argNum = 1;
            for (int i = 0; i < expressionList.length(); i++) {
                if (expressionList.charAt(i) == ',') argNum++;
            }
        }

        //finally we call the method
        if (Tokenizer.funcCollection.contains(funcName))
            subroutine.add(VMWriter.writeFunctionCall(funcName, argNum));
        else {
            for (String s : Tokenizer.funcCollection) {
                if (s.substring(s.indexOf(".") + 1).equals(funcName.substring(funcName.indexOf(".") + 1))) {
                    subroutine.add(VMWriter.writeFunctionCall(s, argNum));
                }
            }
        }

        if (funcType.get(funcName).equals("void"))
            subroutine.add(VMWriter.writePop("temp", 0));
        return subroutine;
    }

    private static ArrayList<String> compileSubroutineBody(ArrayList<String> lines) {
        ArrayList<String> subroutinebody = new ArrayList<>();
        int lineIndex = 0;
        int ifstatementLineNum = -1;
        int whilestatementLineNum = -1;

        for (String line : lines){
            
            if (lineIndex == ifstatementLineNum) {
                int labelCnt = loopCount.get(ifstatementLineNum);
                subroutinebody.add("label IF_FALSE" + Integer.toString(labelCnt));
            }
            if (lineIndex == whilestatementLineNum) {
                int labelCnt = loopCount.get(whilestatementLineNum);
                subroutinebody.add("goto WHILE_EXP" + Integer.toString(labelCnt));
                subroutinebody.add("label WHILE_END" + Integer.toString(labelCnt));
            }

            if (line.indexOf("else") == 0) line = line.substring(4);

            if (line.indexOf("do") == 0) {
                subroutinebody.addAll(compileDoStatement(line));
                lineIndex ++;
            }
            else if (line.indexOf("let") == 0) {
                subroutinebody.addAll(compileLetStatement(line));
                lineIndex ++;
            }
            else if (line.indexOf("return") == 0) {
                subroutinebody.addAll(compileReturnStatement(line));
                lineIndex ++;
            }

            else if (line.indexOf("if") == 0) {
                String ifstatement = "";
                if (line.contains("{"))
                    ifstatement = line.substring(0, line.indexOf("{"));
                else
                    ifstatement = line;

                subroutinebody.addAll(compileIfStatement(ifstatement));
                subroutinebody.add("if-goto IF_TRUE" + Integer.toString(ifLabelCount));
                subroutinebody.add("goto IF_FALSE" + Integer.toString(ifLabelCount));
                subroutinebody.add("label IF_TRUE" + Integer.toString(ifLabelCount));
                if (line.contains("{") && line.indexOf("{") != line.length() - 1) {
                    ArrayList<String> statement = new ArrayList<>();
                    statement.add(line.substring(line.indexOf("{") + 1));
                    subroutinebody.addAll(compileSubroutineBody(statement));
                }

                ifstatementLineNum = Operation.selectContent(Operation.subList(lines, lineIndex)).size() + lineIndex ++;
                loopCount.put(ifstatementLineNum, ifLabelCount ++);
            }

            else if (line.indexOf("while") == 0) {
                String whilestatement = "";
                if (line.contains("{"))
                    whilestatement = line.substring(0, line.indexOf("{"));
                else
                    whilestatement = line;

                subroutinebody.add("label WHILE_EXP" + Integer.toString(whileLabelCount));
                subroutinebody.addAll(compileWhileStatement(whilestatement));
                subroutinebody.add("not");
                subroutinebody.add("if-goto WHILE_END" + Integer.toString(whileLabelCount));
                if (line.contains("{") && line.indexOf("{") != line.length() - 1) {
                    ArrayList<String> statement = new ArrayList<>();
                    statement.add(line.substring(line.indexOf("{") + 1));
                    subroutinebody.addAll(compileSubroutineBody(statement));
                }

                whilestatementLineNum = Operation.selectContent(Operation.subList(lines, lineIndex)).size() + lineIndex ++;
                loopCount.put(whilestatementLineNum, whileLabelCount ++);
            }

            else lineIndex ++;
        }

        return subroutinebody;
    }

    private static ArrayList<String> compileLetStatement(String line) {
        ArrayList<String> letstatement = new ArrayList<>();
        line = line.substring(3);
        String var1 = line.substring(0, line.indexOf("="));
        String var2 = line.substring(line.indexOf("=") + 1, line.indexOf(";"));

        //case 1: array var + expression
        if (var1.contains("[")) {
            String exp1 = var1.substring(var1.indexOf("[") + 1, var1.indexOf("]"));
            String name1 = var1.substring(0, var1.indexOf("["));

            ArrayList<String> expression1 = compileExpression(exp1);
            letstatement.addAll(expression1);
            letstatement.add(VMWriter.writePush(SymbolTable.varKind(name1, curSubroutine.toString()), SymbolTable.varCount(name1, curSubroutine.toString())));
            letstatement.add("add");
            letstatement.addAll(compileExpression(var2));
            letstatement.add(VMWriter.writePop("temp", 0));
            letstatement.add(VMWriter.writePop("pointer", 1));
            letstatement.add(VMWriter.writePush("temp", 0));
            letstatement.add(VMWriter.writePop("that", 0));
            return letstatement;
        }

        //case 2: non-array var + expression
        letstatement.addAll(compileExpression(var2));
        letstatement.add(VMWriter.writePop(SymbolTable.varKind(var1, curSubroutine.toString()), SymbolTable.varCount(var1, curSubroutine.toString())));
        return letstatement;
    }

    private static ArrayList<String> compileDoStatement(String line) {
        line = line.substring(2);
        return compileSubroutineCall(line);
    }

    private static ArrayList<String> compileIfStatement(String line) {
        return compileExpression(line.substring(2));
    }

    private static ArrayList<String> compileWhileStatement(String line) {
        return compileExpression(line.substring(5));
    }

    private static ArrayList<String> compileReturnStatement(String line) {
        ArrayList<String> returnstatement = new ArrayList<>();

        String returnVal = line.substring(6, line.indexOf(";"));

        if (!returnVal.isEmpty()) {
            if (isKeywordConstant(returnVal)) {
                if (returnVal.equals("this")) {
                    returnstatement.add(VMWriter.writePush("pointer", 0));
                }

                if (returnVal.equals("true")) {
                    returnstatement.add(VMWriter.writePush("constant", 0));
                    returnstatement.add("not");
                }

                if (returnVal.equals("false") || returnVal.equals("null")) {
                    returnstatement.add(VMWriter.writePush("constant", 0));
                }
            } else if (returnVal.matches("-?\\d+")) {
                returnstatement.add(VMWriter.writePush("constant", Integer.parseInt(returnVal)));
            } else if (returnVal.charAt(0) == '"') {
                returnVal = returnVal.substring(1);
                String string = returnVal.substring(0, returnVal.length() - 1);
                returnstatement.addAll(VMWriter.writeString(string));
            } else
                returnstatement.add(VMWriter.writePush(SymbolTable.varKind(returnVal, curSubroutine.toString()), SymbolTable.varCount(returnVal, curSubroutine.toString())));
        } else {
            returnstatement.add(VMWriter.writePush("constant", 0));
        }

        returnstatement.add("return");
        return returnstatement;
    }

    private static ArrayList<String> compileArray(String line) {
        ArrayList<String> array = new ArrayList<>();
        //the array consist of arr + [ + expression + ]
        String varName = line.substring(0, line.indexOf("["));
        String exp = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
        ArrayList<String> expression = compileExpression(exp);

        array.add(VMWriter.writePush(SymbolTable.varKind(varName, curSubroutine.toString()), SymbolTable.varCount(varName, curSubroutine.toString())));
        array.addAll(expression);
        array.add("add");
        array.add(VMWriter.writePop("pointer", 1));
        array.add(VMWriter.writePush("that", 0));

        return array;
    }

    private static ArrayList<String> compileExpressionList(String line) {
        ArrayList<String> expressionList = new ArrayList<>();

        if (!line.contains(",")) {
            ArrayList<String> expression = compileExpression(line);
            expressionList.addAll(expression);
            return expressionList;
        } else {
            String exp = line.substring(0, line.indexOf(","));
            ArrayList<String> expression = compileExpression(exp);
            expressionList.addAll(expression);

            line = line.substring(1);

            while (line.contains(",")) {
                exp = line.substring(0, line.indexOf(","));
                expression = compileExpression(exp);
                expressionList.addAll(expression);

                line = line.substring(1);
            }

            expression = compileExpression(line);
            expressionList.addAll(expression);
            return expressionList;
        }
    }

    private static ArrayList<String> compileExpression(String line) {
        ArrayList<String> expression = new ArrayList<>();

        //if the first character belongs unary op
        //then we skip it
        String unaryOp = "";
        if (isOp(line.charAt(0))) {
            unaryOp = Character.toString(line.charAt(0));
            line = line.substring(1);
        }

        if (!containOP(line)) {
            ArrayList<String> term = compileTerm(line);
            expression.addAll(term);
            if (!unaryOp.isEmpty())
                expression.add(VMWriter.writeUnaryOp(unaryOp));
            return expression;
        }

        //if we meet (, deal with it first
        if (line.charAt(0) == '(') {
            Stack<Integer> stack = new Stack<>();
            int rightIndex = -1;
            for (int i = 1; i < line.length(); i++) {
                if (line.charAt(i) == '(') stack.push(i);
                if (line.charAt(i) == ')') {
                    if (!stack.isEmpty()) stack.pop();
                    else {
                        rightIndex = i;
                        break;
                    }
                }
            }

            String termExp = line.substring(0, rightIndex + 1);
            expression.addAll(compileTerm(termExp));
            if (!unaryOp.isEmpty()) expression.add(VMWriter.writeUnaryOp(unaryOp));

            //if the ) is not the last, then after it comes an op
            //it's defined by the Jack grammar
            if (rightIndex < line.length() - 1) {
                expression.addAll(compileExpression(line.substring(rightIndex + 2)));
                expression.add(VMWriter.writeOp(Character.toString(line.charAt(rightIndex + 1))));
            }
            return expression;
        }

        //we deal with the next op
        int opIndex = -1;
        String curOp = "";
        for (int i = 0; i < line.length(); i++) {
            if (isOp(line.charAt(i))) {
                opIndex = i;
                curOp = Character.toString(line.charAt(i));
                break;
            }
        }

        String termExp1 = line.substring(0, opIndex);
        String termExp2 = line.substring(opIndex + 1);
        expression.addAll(compileTerm(termExp1));
        if (!unaryOp.isEmpty())
            expression.add(VMWriter.writeUnaryOp(unaryOp));
        if (!termExp2.isEmpty())
            expression.addAll(compileExpression(termExp2));
        expression.add(VMWriter.writeOp(curOp));
        return expression;
    }

    private static ArrayList<String> compileTerm(String line) {
        ArrayList<String> term = new ArrayList<>();

        if (line.matches("-?\\d+")) {
            term.add(VMWriter.writePush("constant", Integer.parseInt(line)));
            return term;
        }

        if (line.contains("(")) {
            //if the first is "(", then it's (expression)
            if (line.charAt(0) == '(') {
                term.addAll(compileExpression(line.substring(1, line.length() - 1)));
                return term;
            } else { //else, it's a functionCall
                ArrayList<String> functionCall = compileSubroutineCall(line);
                term.addAll(functionCall);
                return term;
            }
        }

        //if line contains '[', it must be an array
        if (line.contains("[")) {
            term.addAll(compileArray(line));
            return term;
        }

        //if it's a stringConstant, we use string method to append char by char
        if (line.charAt(0) == '"') {
            String string = line.substring(1, line.length() - 1);
            term.addAll(VMWriter.writeString(string));

            return term;
        }

        //else, it's a variable
        else {
            term.add(VMWriter.writePush(SymbolTable.varKind(line, curSubroutine.toString()), SymbolTable.varCount(line, curSubroutine.toString())));
            return term;
        }

    }

    public static void constructor(String[] files) {
        for (String filename : files) {
            String name = filename.substring(0, filename.length() - 5);
            Operation.addToArray(Tokenizer.className, name);
        }

        initType();

        for (String filename : files) {
            SymbolTable.constructor(filename);
            try {
                String vmFile = filename.substring(0, filename.length() - 5) + ".vm";
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                BufferedWriter writer = new BufferedWriter(new FileWriter(vmFile));

                String line = Operation.modify(reader.readLine());
                while (line != null) {
                    if (!line.isEmpty()) {
                        if (line.indexOf("class") == 0) {
                            className.setLength(0);
                            className.append(line.substring(5, line.length() - 1));
                            line = Operation.modify(reader.readLine());
                            continue;
                        }

                        if (isSubroutineDec(line)) {
                            String subroutineKind = "";
                            String subroutineType = "";
                            int subroutineLength = -1;
                            int typeLength = -1;
                            for (int i = 0; i < subroutineDec.length; i++) {
                                if (line.indexOf(subroutineDec[i]) == 0) {
                                    subroutineKind = subroutineDec[i];
                                    subroutineLength = subroutineDec[i].length();
                                    break;
                                }
                            }

                            line = line.substring(subroutineLength);

                            //then comes the type
                            for (int i = 0; i < type.length; i++) {
                                if (line.indexOf(type[i]) == 0) {
                                    subroutineType = type[i];
                                    typeLength = type[i].length();
                                    break;
                                }
                            }

                            line = line.substring(typeLength);
                            String subroutine = className + "." + line.substring(0, line.indexOf("("));
                            curSubroutine.setLength(0);
                            curSubroutine.append(subroutine);
                            funcType.put(curSubroutine.toString(), subroutineType);
                            writer.write(VMWriter.writeFunctionDec(curSubroutine.toString(), SymbolTable.kindCount("local", curSubroutine.toString())));
                            writer.newLine();

                            if (subroutineKind.equals("constructor")) {
                                String line1 = VMWriter.writePush("constant", SymbolTable.kindCount("field", null));
                                String line2 = VMWriter.writeCall("Memory.alloc", 1);
                                writer.write(line1);
                                writer.newLine();
                                writer.write(line2);
                                writer.newLine();
                            }

                            if (subroutineKind.equals("method")) {
                                String line1 = VMWriter.writePush("argument", 0);
                                String line2 = VMWriter.writePop("pointer", 0);
                                writer.write(line1);
                                writer.newLine();
                                writer.write(line2);
                                writer.newLine();
                            }
                            line = Operation.modify(reader.readLine());
                            continue;
                        }

                        //case 2: varDec, we just ignore it
                        else if (isVarDec(line)){
                            while (isVarDec(line))
                                line = Operation.modify(reader.readLine());
                            continue;
                        }

                        //case 3: subroutineBody
                        else {
                            ArrayList<String> subroutineCode = new ArrayList<>();
                            while (line != null && !isSubroutineDec(line)) {
                                if (!line.isEmpty()) {
                                    System.out.println(line);
                                    subroutineCode.add(line);
                                }
                                line = Operation.modify(reader.readLine());
                            }

                            ArrayList<String> subroutinebody = new ArrayList<>(compileSubroutineBody(subroutineCode));
                            for (String s : subroutinebody) {
                                writer.write(s);
                                writer.newLine();
                            }

                            line = Operation.modify(reader.readLine());
                            continue;
                        }
                    }
                    line = Operation.modify(reader.readLine());
                }

                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
