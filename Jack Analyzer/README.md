#! https://zhuanlan.zhihu.com/p/698417850
# $10.9.$分析器实现笔记

### 1.分词器

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

### 2.分析器
&emsp;&emsp;在分析器中，我们使用经分词器处理过的文件来构建最终的.xml文件。由于对一个元素我们可能会生成多行代码，我们选择利用`ArrayList<String>`来存储每个元素产生的语句。当一个元素中包含了其他子元素时，我们**在调用处理子元素的方法、得到子元素的分析代码后，加入该元素的`ArrayList`即可**。这样，我们就可以通过**递归**调用来完成对程序的分析

&emsp;&emsp;以`class`的分析为例，我们可以写出以下的代码框架：

```Java
private static ArrayList<String> compileClass() {
    ArrayList<String> class_ = new ArrayList<>();

    class_.add("<class>");
    ......

    //call method recursively
    class_.addAll(compileClassVar());
    class_.addAll(compileSubroutineDec());

    ......
    class_.add("</class>");
    return class_;
}
```
