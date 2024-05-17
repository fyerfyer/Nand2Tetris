import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    //classVar is used to store class table
    //it stores the name of var and its number
    public static Map<String, Integer> classVar = new HashMap<>();

    //typeOfClassVar is used to store the type of classVar
    public static Map<String, String> typeOfClassVar = new HashMap<>();

    //kindOfClassVar is used to store the kind of classVar
    public static Map<String, String> kindOfClassVar = new HashMap<>();

    //for convenience, we store the symbolTable of each subroutine
    //into a map.
    private static Map<String, Map<String, Integer>> mapOfSubVar = new HashMap<>();
    private static Map<String, Map<String, String>> mapOfSubVarType = new HashMap<>();
    private static Map<String, Map<String, String>> mapOfSubVarKind = new HashMap<>();

    private static Map<String, Integer> subroutineVar = new HashMap<>();
    private static Map<String, String> typeOfSubroutineVar = new HashMap<>();
    private static Map<String, String> kindOfSubroutineVar = new HashMap<>();

    //use to return the kind index
    private static int classNum = 0;
    private static int argNum = 0;
    private static int localNum = 0;

    //and for subroutine, we store their argNum and localNum
    private static Map<String, Integer> subroutineArgNum = new HashMap<>();
    private static Map<String, Integer> subroutineLCLNum = new HashMap<>();

    //the current name of the line
    private static String className;

    //the type of each function
    //if it's void, we need to push constant 0
    //and when we call a void method, we need to pop temp 0

    //the current stringName
    private static StringBuilder currentStringName = new StringBuilder();

    //we store the current subRoutine name to store subRoutine into its map
    private static StringBuilder currentSubroutineName = new StringBuilder();

    private static final ArrayList<String> copyStringCollection = new ArrayList<>();


    //if we meet a new class, we reset it
    private static void startClass() {
        classVar.clear();
        typeOfClassVar.clear();
        kindOfClassVar.clear();
    }

    //if we meet a new subroutine, we reset it
    private static void startSubroutine() {
        subroutineVar.clear();
        typeOfSubroutineVar.clear();
        kindOfSubroutineVar.clear();
        localNum = 0;
        argNum = 0;
    }

    //when dealing with the methods, we need to push THIS to arg0
    private static void startMethod() {
        subroutineVar.put("this", argNum++);
        typeOfSubroutineVar.put("this", className);
        kindOfSubroutineVar.put("this", "argument");
    }

    private static void dealClassVar() {
        if (Tokenizer.getNextString().equals("field")
                || Tokenizer.getNextString().equals("static")) {
            //get the kind
            String classVarKind = Tokenizer.stringAdvance();

            //get the type
            String classVarType = Tokenizer.stringAdvance();

            //get the name
            String classVarName = Tokenizer.stringAdvance();

            //add to correspond kind
            if (classVarKind.equals("static")) {
                classVar.put(classVarName, classNum++);
                typeOfClassVar.put(classVarName, classVarType);
                kindOfClassVar.put(classVarName, "this");
            } else if (classVarKind.equals("field")) {
                classVar.put(classVarName, classNum++);
                typeOfClassVar.put(classVarName, classVarType);
                kindOfClassVar.put(classVarName, "this");
            }

            //if there's a ',', there remains some vars
            while (Tokenizer.getNextString().equals(",")) {
                Tokenizer.stringAdvance();
                String nextName = Tokenizer.stringAdvance();
                //after the operation, there will be ',' or ';'

                if (classVarKind.equals("static")) {
                    classVar.put(nextName, classNum++);
                    typeOfClassVar.put(nextName, classVarType);
                    kindOfClassVar.put(nextName, "this");
                } else if (classVarKind.equals("field")) {
                    classVar.put(nextName, classNum++);
                    typeOfClassVar.put(nextName, classVarType);
                    kindOfClassVar.put(nextName, "this");
                }
            }

            //then we meets ';', we skip it
            Tokenizer.stringAdvance();

            //we continuously deal with rest classVar recursively
            if (Tokenizer.getNextString().equals("field")
                    || Tokenizer.getNextString().equals("static")) {
                dealClassVar();
                return;
            }

            //finally, update the current string
            currentStringName.setLength(0);
            currentStringName.append(Tokenizer.stringAdvance());
        }
    }

    //deal with func argument
    private static void dealFuncArgVar() {
        //the function features: Dec + type + funcName + ( + args + )
        //so we need to ignore the irrelevant elements

        String subroutineType = Tokenizer.stringAdvance();
        currentSubroutineName.setLength(0);
        currentSubroutineName.append(className + ".");
        currentSubroutineName.append(Tokenizer.stringAdvance());
        Tokenizer.stringAdvance();

        //we continue read the arguments until we meet ")"
        while (!Tokenizer.getNextString().equals(")")) {
            // type + name
            String type = Tokenizer.stringAdvance();
            String name = Tokenizer.stringAdvance();
            subroutineVar.put(name, argNum ++);
            typeOfSubroutineVar.put(name, type);
            kindOfSubroutineVar.put(name, "argument");

            //if there's a ',', then we have more args
            while (Tokenizer.getNextString().equals(",")) {
                //ignore the ','
                Tokenizer.stringAdvance();
                type = Tokenizer.stringAdvance();
                name = Tokenizer.stringAdvance();
                subroutineVar.put(name, argNum ++);
                typeOfSubroutineVar.put(name, type);
                kindOfSubroutineVar.put(name, "argument");
            }
        }

        //we ignore the ")"
        Tokenizer.stringAdvance();

        //then we ignore the "{"
        Tokenizer.stringAdvance();
        currentStringName.setLength(0);
        currentStringName.append(Tokenizer.stringAdvance());
    }

    private static void dealFuncLCLVar() {
        if (currentStringName.toString().equals("var")) {
            //var + type + name
            //we skip var first
            String type = Tokenizer.stringAdvance();
            String name = Tokenizer.stringAdvance();

            subroutineVar.put(name, localNum ++);
            typeOfSubroutineVar.put(name, type);
            kindOfSubroutineVar.put(name, "local");

            //deal with ","
            while (Tokenizer.getNextString().equals(",")) {
                Tokenizer.stringAdvance();
                name = Tokenizer.stringAdvance();

                subroutineVar.put(name, localNum ++);
                typeOfSubroutineVar.put(name, type);
                kindOfSubroutineVar.put(name, "local");
            }
        }

        //we meet a ";"
        Tokenizer.stringAdvance();
        currentStringName.setLength(0);
        currentStringName.append(Tokenizer.stringAdvance());
        if (currentStringName.toString().equals("var")) dealFuncLCLVar();
    }

    public static void constructor(String filename) {
        Tokenizer.constructor(filename);
        copyStringCollection.addAll(Tokenizer.stringCollection);
        currentStringName.append(Tokenizer.stringAdvance());

        while (Tokenizer.stringHasNext()) {
            //if we meet "class", then we call dealClass
            if (currentStringName.toString().equals("class")) {
                startClass();
                className = Tokenizer.stringAdvance();
                //ignore the '{'
                Tokenizer.stringAdvance();
                dealClassVar();
                if (!currentStringName.toString().equals("class")) continue;
            }

            //if we meet func s, we call dealFunc
            if (currentStringName.toString().equals("method")
                    || currentStringName.toString().equals("function")
                    || currentStringName.toString().equals("constructor")) {
                //deal with the arguments first
                startSubroutine();
                if (currentStringName.toString().equals("method")) startMethod();
                dealFuncArgVar();

                //after the argument comes the body of func
                //we only care about varDec
                if (currentStringName.toString().equals("var")) dealFuncLCLVar();

                //we copy the current ArrayList and deliver the copyList
                Map<String, Integer> copyVar = new HashMap<>(subroutineVar);
                Map<String, String> copyType = new HashMap<>(typeOfSubroutineVar);
                Map<String, String> copyKind = new HashMap<>(kindOfSubroutineVar);

                mapOfSubVar.put(currentSubroutineName.toString(), copyVar);
                mapOfSubVarType.put(currentSubroutineName.toString(), copyType);
                mapOfSubVarKind.put(currentSubroutineName.toString(), copyKind);

                //store the index of arg and lcl
                subroutineLCLNum.put(currentSubroutineName.toString(), localNum);
                subroutineArgNum.put(currentSubroutineName.toString(), argNum);

                continue;
            }

            currentStringName.setLength(0);
            currentStringName.append(Tokenizer.stringAdvance());
        }

        Tokenizer.stringCollection.addAll(copyStringCollection);
    }

    public static int varCount(String var, String subroutine) {
        try {
            if (subroutine != null && mapOfSubVar.get(subroutine).containsKey(var))
                return mapOfSubVar.get(subroutine).get(var);
            else if (classVar.containsKey(var))
                return classVar.get(var);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("The variable is undefined");
        }

        return -1;
    }

    public static String varType(String var, String subroutine) {
        try {
            if (subroutine != null && mapOfSubVarType.get(subroutine).containsKey(var))
                return mapOfSubVarType.get(subroutine).get(var);
            else if (typeOfClassVar.containsKey(var))
                return typeOfClassVar.get(var);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("The variable is undefined");
        }

        return null;
    }

    public static String varKind(String var, String subroutine) {
        try {
            if (subroutine != null && mapOfSubVarKind.get(subroutine).containsKey(var))
                return mapOfSubVarKind.get(subroutine).get(var);
            else if (kindOfClassVar.containsKey(var))
                return kindOfClassVar.get(var);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("The variable is undefined.");
        }

        return null;
    }

    //if it's a classVar, then subroutine is null
    public static int kindCount(String kindName, String subroutine) {
        try {
            if (subroutine.equals("Output.printInt")) return 2;
            if (kindName.equals("argument")) return subroutineArgNum.get(subroutine);
            if (kindName.equals("local")) return subroutineLCLNum.get(subroutine);
            if (kindName.equals("field") || kindName.equals("static")) return classNum;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("The kind is out of range.");
        }

        return -1;
    }

}
