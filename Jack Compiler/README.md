# $11.11.$编译器实现笔记 

### 1.`Symbol Table`实现
&emsp;&emsp;为了方便读取单一字符，我们选择**利用`tokenizer`生成的代码**来构建我们的`Symbol Table`.

#### &emsp;&emsp;$a.$准备工作
1. 虽然理论上应该对于每一个子程序，我们都应该实时地产生一个对应的`subroutineMap`，但是由于较难实现，我选择遍历整个程序后，将每个子程序对应的`subroutineMap`用一个新的$Map$来存储：

```Java
//for convenience, we store the symbolTable of each subroutine into a map.

    private static Map<String, Map<String, Integer>> mapOfSubVar = new HashMap<>();
    private static Map<String, Map<String, String>> mapOfSubVarType = new HashMap<>();
    private static Map<String, Map<String, String>> mapOfSubVarKind = new HashMap<>();

    private static Map<String, Integer> subroutineVar = new HashMap<>();
    private static Map<String, String> typeOfSubroutineVar = new HashMap<>();
    private static Map<String, String> kindOfSubroutineVar = new HashMap<>();
```

&emsp;&emsp;同时，这要求我们记录下当前遍历的子程序的名称：

```Java
private static StringBuilder currentStringName = new StringBuilder();
```

2. 由于对每个`class`，我们都要生成一个新的`Symbol Table`， 因此我们在生成对应的`Symbol Table`前先要对储存表格的$Map$进行初始化：

```Java
private static void startClass() {
    classVar.clear();
    typeOfClassVar.clear();
    kindOfClassVar.clear();
}
```

3. 在后续生成VM代码的过程中，我们需要每个变量对应的序号，因此我们需要设置变量对其进行存储：

```Java
    //use to return the kind index
    private static int classNum = 0;
    private static int argNum = 0;
    private static int localNum = 0;

//and for subroutine, we store their argNum and localNum
    private static Map<String, Integer> subroutineArgNum = new HashMap<>();
    private static Map<String, Integer> subroutineLCLNum = new HashMap<>();
```

#### &emsp;&emsp;$b.$类变量处理
&emsp;&emsp;类中定义的变量遵循以下结构：

$$kind+type+name(,name);$$

&emsp;&emsp;由于该结构具有普适性，我们可以用**递归**的方法读取并处理我们读入的*token*：
1. 处理一定存在的$kind+type+name$.
2. 判断是否有$,name$部分，如果有的话进行处理.
3. 递归执行上述步骤，直至下一个传入变量不是`local`变量.

```Java
private static void dealClassVar() {
    if (Tokenizer.getNextString().equals("field") || Tokenizer.getNextString().equals("static")) {
        // get the kind
        String classVarKind = Tokenizer.stringAdvance();
        // get the type
        String classVarType = Tokenizer.stringAdvance();
        // get the name
        String classVarName = Tokenizer.stringAdvance();
        // add to correspond kind
        if (classVarKind.equals("static")) {
            classVar.put(classVarName, classNum++);
            typeOfClassVar.put(classVarName, classVarType);
            kindOfClassVar.put(classVarName, "this");
        } else if (classVarKind.equals("field")) {
            classVar.put(classVarName, classNum++);
            typeOfClassVar.put(classVarName, classVarType);
            kindOfClassVar.put(classVarName, "this");
        }
        // if there's a ',', there remains some vars
        while (Tokenizer.getNextString().equals(",")) {
            Tokenizer.stringAdvance();
            String nextName = Tokenizer.stringAdvance();
            // after the operation, there will be ',' or ';'
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
        // then we meets ';', we skip it
        Tokenizer.stringAdvance();
        // we continuously deal with rest classVar recursively
        if (Tokenizer.getNextString().equals("field") || Tokenizer.getNextString().equals("static")) {
            dealClassVar();
            return;
        }
        // finally, update the current string
        currentStringName.setLength(0);
        currentStringName.append(Tokenizer.stringAdvance());
    }
}

```

#### &emsp;&emsp;$c.$子程序变量
&emsp;&emsp;子程序变量结构与类变量相似，这里略过。需要注意：子程序中包含参数变量，这也需要我们纳入考虑的范畴：

* 对参数的处理：
```Java
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
        subroutineVar.put(name, argNum++);
        typeOfSubroutineVar.put(name, type);
        kindOfSubroutineVar.put(name, "argument");
        //if there's a ',', then we have more args
        while (Tokenizer.getNextString().equals(",")) {
            //ignore the ','
            Tokenizer.stringAdvance();
            type = Tokenizer.stringAdvance();
            name = Tokenizer.stringAdvance();
            subroutineVar.put(name, argNum++);
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

```

* 对程序体内部变量的处理：

```Java
private static void dealFuncLCLVar() {
    if (currentStringName.toString().equals("var")) {
        //var + type + name
        //we skip var first
        String type = Tokenizer.stringAdvance();
        String name = Tokenizer.stringAdvance();

        subroutineVar.put(name, localNum++);
        typeOfSubroutineVar.put(name, type);
        kindOfSubroutineVar.put(name, "local");

        //deal with ","
        while (Tokenizer.getNextString().equals(",")) {
            Tokenizer.stringAdvance();
            name = Tokenizer.stringAdvance();
            subroutineVar.put(name, localNum++);
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

    }
```

#### &emsp;&emsp;$d.$`Symbol Table`最终构建
&emsp;&emsp;有了上述方法的铺垫， 我们只需要在遇到对应的$token$时调用对应的方法即可。我们利用`private static String currentStringName`来记录当前读入的$token$，如果遇到了声明类或子程序的关键字，就跳转到对应的变量处理程序中：

1. 当遇到`class`关键字，我们在忽略掉$class+className+\{$ 后着手处理类变量：

```Java
//if we meet "class", then we call dealClass
if (currentStringName.toString().equals("class")) {
    startClass();

    className = Tokenizer.stringAdvance();

    //ignore the '{'
    Tokenizer.stringAdvance();

    dealClassVar();

    if (!currentStringName.toString().equals("class")) continue;
}
```
2. 当遇到`funcDec`关键词，我们只处理`varDec`部分，并将得到的$Map$存入对应子程序的$Map$中：

```Java
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

```

#### &emsp;&emsp;$d.$供其他类使用的函数
&emsp;&emsp;对于给定的输入，返回该输入在$Map$中对应的值即可：

```Java
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

```

### 2.`VMWriter`实现
&emsp;&emsp;为满足`CompileEngine`VM代码的生成，我们在`VMWriter`中设计以下函数：

```Java
public static String writePush(String segment, int index)
public static String writePop(String segment, int index)
public static String writeOp(String command)
public static String writeUnaryOp(String command)
public static String writeCall(String name, int argNum)
public static String writeFunctionDec(String name, int lclNum)
public static String writeFunctionCall(String name, int argNum)
public static String writeReturn()
public static ArrayList<String> writeString(String string)
```

### 3.`CompileEngine`实现
&emsp;&emsp;在`CompileEngine`中我们希望读入完整的表达式以生成对应的VM代码，因此我们选择直接读入源程序，然后根据读入行的关键字判断其表达式类型。

#### &emsp;&emsp;$a.$准备工作
&emsp;&emsp;为了防止`if`和`while`的$label$重复，我们可以定义`private static int ifLabelCount, whileLabelCount`。

&emsp;&emsp;同时，由于在调用函数时需要知道函数的返回值类型，我们也要在编译过程中存储我们遇到的函数：

```Java
private static Map<String, String> funcType
```

#### &emsp;&emsp;$b.$表达式计算
&emsp;&emsp;我们知道，`Jack`中的表达式有以下性质：
1. 当没有括号时，从左到右依次计算。
2. 有括号时，优先计算括号内式子。

&emsp;&emsp;因此，我们**从左往右**处理输入的表达式，并**优先考虑是否有括号，如果遇到括号优先处理括号内语句**。

> $p.s.$:由于我们先计算内侧括号、再计算外侧括号，一个自然的想法是**找到最内侧的括号对，处理完其内部表达式后将其抹去，向外延展**。但是这样不满足性质一，因而会导致失败。

&emsp;&emsp;我们可以利用**栈**来进行括号对的匹配，然后计算对应下标表达式的值。

&emsp;&emsp;在`Jack`语法一章中，我们知道表达式具有如下结构：

$$expression=term(op\;term)$$

&emsp;&emsp;我们可以继续沿用该结构，**利用`op`将原表达式拆分成一个个子表达式，然后分别递归计算**。但是，我们并不能确定$term$的个数，因此我们对上面的结构进行改动：

$$expression=term(op\;expression)$$

&emsp;&emsp;该结构的变量个数是确定的，我们只需分别递归计算即可：

> $p.s$这里有一个比较棘手的问题：对于$unary\;op$的处理。在`-(1+2)`这个表达式中，第一个`-`并不起到拆分的作用。注意到我们是以$term$为基本处理单元的，而**在$term$中$unary\;op$只能在第一个位置**。因此，我们在从左往右计算前**先判断第一个元素是否为$op$，如果是，我们对其进行计算，然后在计算完第一个$term$后记录该表达式：**

```Java
//if the first character belongs unary op //then we skip it 
String unaryOp = ""; 
if (isOp(line.charAt(0))) {
    unaryOp = Character.toString(line.charAt(0)); 
    line = line.substring(1); 
}

if (!unaryOp.isEmpty()) expression.add(VMWriter.writeUnaryOp(unaryOp));
```

```Java
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
```

&emsp;&emsp;对于$term$的处理较为简单，只需注意对函数的判断方式即可：
```Java
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

```

#### &emsp;&emsp;$c.$`return`语句

&emsp;&emsp;根据`return`变量进行分类处理即可：

```Java
private static ArrayList<String> compileReturnStatement(String line) {
    ArrayList<String> returnStatement = new ArrayList<>();

    // Extract the return value from the line
    String returnVal = line.substring(6, line.indexOf(";"));

    if (!returnVal.isEmpty()) {
        if (isKeywordConstant(returnVal)) {
            // Handle keyword constants
            if (returnVal.equals("this")) {
                returnStatement.add(VMWriter.writePush("pointer", 0));
            }

            if (returnVal.equals("true")) {
                returnStatement.add(VMWriter.writePush("constant", 0));
                returnStatement.add("not");
            }

            if (returnVal.equals("false") || returnVal.equals("null")) {
                returnStatement.add(VMWriter.writePush("constant", 0));
            }
        } else if (returnVal.matches("-?\\d+")) {
            // Handle integer constants
            returnStatement.add(VMWriter.writePush("constant", Integer.parseInt(returnVal)));
        } else if (returnVal.charAt(0) == '"') {
            // Handle string constants
            returnVal = returnVal.substring(1);
            String string = returnVal.substring(0, returnVal.length() - 1);
            returnStatement.addAll(VMWriter.writeString(string));
        } else {
            // Handle variable return values
            returnStatement.add(VMWriter.writePush(SymbolTable.varKind(returnVal, curSubroutine.toString()), SymbolTable.varCount(returnVal, curSubroutine.toString())));
        }
    } else {
        // If no return value, push 0
        returnStatement.add(VMWriter.writePush("constant", 0));
    }

    // Add return command
    returnStatement.add("return");

    return returnStatement;
}

```

#### &emsp;&emsp;$d.$`do`语句
&emsp;&emsp;`do`语句只需处理被调用的函数即可：

```Java
private static ArrayList<String> compileDoStatement(String line) {
	line = line.substring(2);
    return compileSubroutineCall(line);
}
```

#### &emsp;&emsp;$e.$`let`语句
&emsp;&emsp;对`let`语句的处理需要注意分类的依据：**左侧的表达式是否为数组**，如果左侧为数组，我们需要加入以下的操作：

```
pop temp 0
pop pointer 1
push temp 0
pop that 0
```

&emsp;&emsp;否则左侧表达式只能是一个普通的变量，我们将其`pop`即可：

```Java
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
}
```

#### &emsp;&emsp;$e.$函数体模块设计&`if, while`处理手段

&emsp;&emsp;在处理`if`语句时，我们需要在处理`if`花括号括起的部分后添加`IF_ENDLabelindex`。因此，我们自然地想到引入`compileSubroutineBody`函数对函数体模块进行整体地编译(而非一条语句一条语句地离散处理)，它接受的参数是**多条语句构成的函数体`ArrayList<String>`**。

&emsp;&emsp;那么如何实现`if`语句呢？一个自然地想法如下：

1. 设计一个函数`compileIfStatement()`，它接受一个`ArrayList<String>`，返回编译后的`ArrayList<String>`。
2. 传入`if`语句对应的子程序，然后输出结果。

&emsp;&emsp;但是，当我们得到输出结果并往下继续处理时，那些属于`if`子程序的代码会**被重复处理**。这并不是我们想要的结果。笔者曾试过处理完`if`的子程序后将其删去，但最终失败了。因此笔者想到了下面的方法：

1. 我们依然对`if`对应的子程序进行读取。
2. 但我们并不将其作为参数传入`compileIfStatement`，而是**记录下该子程序的行数**`ifIndex`。
3. 同时，在`compileSubroutineBody`中，我们实时更新当前的代码行数`currentIndex`。
4. 我们通过记录的`currentIndex`和`ifIndex`**计算出应该添加$label$的行数**，然后**当`currentIndex`的值与其相等时添加$label$**。

&emsp;&emsp;同时，我们依然保留`compileIfStatement`这个方法，只是该方法仅用来编译`if(expression)`语句。

> 如果遇到`else`语句，直接忽略即可。

>另：我们不能直接用类中定义的`static int ifLabelCount`作为标记的序号，因为当存在嵌套条件循环时，由于不管遇到哪个`if`该值都会增加1，这会导致**同一个`if`的前后标记序号不一致**。对此，我们**用一个$Map$来存储特定`lineIndex`对应的标记序号**，由于`lineIndex`是唯一的，我们的前后标记序号将会一致。


```Java
private static ArrayList<String> compileSubroutineBody(ArrayList<String> lines) {
    ArrayList<String> subroutinebody = new ArrayList<>();
    int lineIndex = 0;
    int ifstatementLineNum = -1;
    int whilestatementLineNum = -1;

    for (String line : lines) {
        if (lineIndex == ifstatementLineNum) {
            int labelCnt = loopCount.get(ifstatementLineNum);
            subroutinebody.add("label IF_FALSE" + Integer.toString(labelCnt));
        }
        if (lineIndex == whilestatementLineNum) {
            int labelCnt = loopCount.get(whilestatementLineNum);
            subroutinebody.add("goto WHILE_EXP" + Integer.toString(labelCnt));
            subroutinebody.add("label WHILE_END" + Integer.toString(labelCnt));
        }

        if (line.indexOf("else") == 0) {
            line = line.substring(4);
        }

        if (line.indexOf("do") == 0) {
            subroutinebody.addAll(compileDoStatement(line));
            lineIndex++;
        } else if (line.indexOf("let") == 0) {
            subroutinebody.addAll(compileLetStatement(line));
            lineIndex++;
        } else if (line.indexOf("return") == 0) {
            subroutinebody.addAll(compileReturnStatement(line));
            lineIndex++;
        } else if (line.indexOf("if") == 0) {
            String ifstatement = "";
            if (line.contains("{")) {
                ifstatement = line.substring(0, line.indexOf("{"));
            } else {
                ifstatement = line;
            }

            subroutinebody.addAll(compileIfStatement(ifstatement));
            subroutinebody.add("if-goto IF_TRUE" + Integer.toString(ifLabelCount));
            subroutinebody.add("goto IF_FALSE" + Integer.toString(ifLabelCount));
            subroutinebody.add("label IF_TRUE" + Integer.toString(ifLabelCount));
            
            if (line.contains("{") && line.indexOf("{") != line.length() - 1) {
                ArrayList<String> statement = new ArrayList<>();
                statement.add(line.substring(line.indexOf("{") + 1));
                subroutinebody.addAll(compileSubroutineBody(statement));
            }

            ifstatementLineNum = Operation.selectContent(Operation.subList(lines, lineIndex)).size() + lineIndex++;
            loopCount.put(ifstatementLineNum, ifLabelCount++);
        } else if (line.indexOf("while") == 0) {
            String whilestatement = "";
            if (line.contains("{")) {
                whilestatement = line.substring(0, line.indexOf("{"));
            } else {
                whilestatement = line;
            }

            subroutinebody.add("label WHILE_EXP" + Integer.toString(whileLabelCount));
            subroutinebody.addAll(compileWhileStatement(whilestatement));
            subroutinebody.add("not");
            subroutinebody.add("if-goto WHILE_END" + Integer.toString(whileLabelCount));

            if (line.contains("{") && line.indexOf("{") != line.length() - 1) {
                ArrayList<String> statement = new ArrayList<>();
                statement.add(line.substring(line.indexOf("{") + 1));
                subroutinebody.addAll(compileSubroutineBody(statement));
            }

            whilestatementLineNum = Operation.selectContent(Operation.subList(lines, lineIndex)).size() + lineIndex++;
            loopCount.put(whilestatementLineNum, whileLabelCount++);
        } else {
            lineIndex++;
        }
    }

    return subroutinebody;
}
```

#### &emsp;&emsp;$f.$其他杂项的处理
&emsp;&emsp;在实现了框架的大部分内容后，针对VM代码自身的特定，我们需要额外补充对一些操作的实现：

1. 对数组的处理：我们将数组名称所在内存段和内部表达式的值`add`，然后`pop pointer 1`，`push that 0`即可：

```Java
private static ArrayList<String> compileArray(String line) {
    ArrayList<String> array = new ArrayList<>();
    
    // the array consists of varName + [ + expression + ]
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
```

2. 调用函数：将函数所需参数`push`，然后计算参数个数、调用即可。需要注意判断函数是否为`void`型，如果是的话需要`pop temp 0`：

```Java
private static ArrayList<String> compileSubroutineCall(String line) {
    ArrayList<String> subroutine = new ArrayList<>();
    int argNum = 0;

    // we first store the functionName
    String funcName = line.substring(0, line.indexOf("("));
    line = line.substring(line.indexOf("("));
    if (!funcName.contains(".")) funcName = className + "." + funcName;

    // then comes the expressionList
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

    // finally we call the method
    if (Tokenizer.funcCollection.contains(funcName)) {
        subroutine.add(VMWriter.writeFunctionCall(funcName, argNum));
    } else {
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
```

#### &emsp;&emsp;$g.$ `CompileEngine`最终构建

1. 预处理。将待编译的一个或多个文件作为参数传入`SymbolTable.constructor`，以构建每个文件对应的`Symbol Table`。
2. 编译时，如果遇到一个函数/方法名，先将其存入`funcType`中，以在后续调用该函数时能够找到该函数/方法名。
3. 对`constructor`，`method`需要进行各自的预处理。
4. 我们可以将待编译的代码如下分类：
	* 方法/函数的声明。
	* 变量声明。
	* 方法/函数体。

&emsp;&emsp;对每一类，分别编译即可。需要注意，处理函数体时，我们需要读入整个子程序，将其放入`ArrayList<String>`中作为`compileSubroutineBody`的参数：

```Java
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

                        // then comes the type
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

                    // case 2: varDec, we just ignore it
                    else if (isVarDec(line)) {
                        while (isVarDec(line)) {
                            line = Operation.modify(reader.readLine());
                        }
                        continue;
                    }

                    // case 3: subroutineBody
                    else {
                        ArrayList<String> subroutineCode = new ArrayList<>();
                        while (line != null && !isSubroutineDec(line)) {
                            if (!line.isEmpty()) {
                                System.out.println(line);
                                subroutineCode.add(line);
                            }
                            line = Operation.modify(reader.readLine());
                        }

                        ArrayList<String> subroutineBody = new ArrayList<>(compileSubroutineBody(subroutineCode));
                        for (String s : subroutineBody) {
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

```






