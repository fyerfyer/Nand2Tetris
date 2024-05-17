&emsp;&emsp;分析器总体较为简单，在实现完分词器后，按照Jack语法递归构建解析树即可。不过在之后写编译器的过程中发现先前的分词器有些许问题，故记之。

&emsp;&emsp;实现分词器，容易想到先按照token进行分类：

```Java
if (ch == '"')
if (Character.isDigit(ch))
if (isSymbol(ch))
for (String keywordName : keyword)
else
```

&emsp;&emsp;但是，有时关键词会混杂在标识符中，导致分词错误。此时我们在处理完关键词后，可以引入一些**语法模式**，对一些特例**优先处理**，以避免意外情况的出现：

1. 类的定义：

```Java
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
```

2. 变量的声明：

```Java
if (isVarDec(keywordName)) {
    // The first is type declaration
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

    // Then comes the varName
    String varName;

    if (line.contains(",") && line.indexOf(",") < line.indexOf(";")) {
        varName = line.substring(0, line.indexOf(","));
        writer.write(identifierVal(varName));
        writer.newLine();
        token.add("identifier");
        tokenLine.add(identifierVal(varName));
        stringCollection.add(varName);
        line = line.substring(varName.length());

        // We need to consider if there exists a ','
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
    } else { // No ',', just ';'
        varName = line.substring(0, line.indexOf(";"));
        writer.write(identifierVal(varName));
        writer.newLine();
        token.add("identifier");
        tokenLine.add(identifierVal(varName));
        stringCollection.add(varName);
        line = line.substring(varName.length());

        writer.write(identifierVal(";"));
        writer.newLine();
        token.add("symbol");
        tokenLine.add(identifierVal(";"));
        stringCollection.add(";");
        line = line.substring(1);
    }
    break;
}

```

3. 函数的声明：

```Java
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

```