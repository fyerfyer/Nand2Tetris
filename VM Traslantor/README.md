# $8.8.$翻译器实现笔记

### 1. `local,argument,this,that,temp`的实现
&emsp;&emsp;这几个内存段的`push`实现都遵循`*sp=*addr`的过程。我们可以将该过程拆分为下面的子过程：

* `D=*addr`.
* `*sp=D`.
* `sp++`

&emsp;&emsp;同时，根据先前的讨论，`push`操作中的`addr`可以通过`baseAddr+index`来计算。`index`的值很容易获取，假设我们的操作为`push local x`, 则：
```
//get index
@x
D=A
```

&emsp;&emsp;但接下来，如果要获取`baseAddr`，因为此时的D寄存器已经存储了`x`的序号， 我们会想通过A寄存器获得`baseAddr`, 然后将`baseAddr+index`的地址存入A寄存器中：
```
//get base addr
@LCL
A=M
A=A+D
```

&emsp;&emsp;顺利完成`LCL`内存段的地址读入后，我们接着用D寄存器读入`baseAddr+index`对应的数据。
```
D=A+D
```
&emsp;&emsp;但是，此时的`A`储存的已不是原先的`baseAddr`了，通过这种方式更新`D`的值是不行的。这时，我们可以考虑**同时更新`A`与`D`的值**，这样就可以利用未改变的`A`成功更新`A`与`D`:
```
@x
D=A
@LCL
A=M //pointer
AD=A+D
```

&emsp;&emsp;然后，我们需要`D=*addr`，由前面讲解的指针的表示，只需补上`D=M`语句即可。

&emsp;&emsp;接下来就是较为简单的`*sp=D`操作了：
```
@SP
A=M
M=D
@SP
M=M+1
```

### 2.`Loop`实现
&emsp;&emsp;对于`if-goto`语句，我们只需根据栈顶元素是否为`0`来判断是否需要跳转到所需分支。因为在栈中，我们用`0`表示`false`：
```
@SP
M=M-1
@SP
A=M
D=M
@LABEL
D;JNE
```

&emsp;&emsp;而对于无条件的`goto`语句，利用`0,JMP`即可实现：
```
@LABEL
0;JMP
```

### 3.`function`实现
&emsp;&emsp;在`Hack`中，我们只需为`function`创建对应标签即可：
```
(LABEL)
```

### 4.`Call`实现
&emsp;&emsp;我们按照$8.6.$节所讲逐步实现`call`功能：
1. 在栈中放入`returnAddress`的标签，这标志着我们在调用函数后将返回的地址：
```
@return_address1
```
&emsp;&emsp;该地址亦是栈指针的初始位置所在之处：
```
D=A
@SP
A=M
M=D
```
2. 然后，我们依次放入`LCL`, `ARG`, `THIS`, `THAT`，以保存调用者(caller)的内存段。

&emsp;&emsp;对于每一个内存段，我们像普通的`push`操作一样，将其放入栈指针所指位置，然后移动栈指针：
```
SP = memorySegment
SP = SP + 1
```

```
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1

@SRG
D=M
@SP
A=M
M=D
@SP
M=M+1

@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1

@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
```
3. 接下来我们要重定向`ARG`的位置，我们需要把它定向至`ARG[0]`的位置，而该位置可以通过计算得出：

&emsp;&emsp;假设我们调用的函数为`call func x`，则在`ARG`内存段前有4(前四个内存段)+x(`LCL`需要的位置个数)，对应代码为：
```
@5+x
D=A
```

&emsp;&emsp;然后我们需要将栈指针移到对应位置，并将`ARG`的初始位置设置在这里：
```
@SP
A=M
AD=A-D
@ARG
M=D
```

4. 为了能够正确访问`LCL`的位置，我们将`LCL` `push`到栈指针所在位置：
```
@SP
D=M
@LCL
M=D
```

5. 然后，我们`goto`需要执行的函数以执行函数：
```
@func
0;JMP
```

6.  最后我们插入`returnAddress`的标签，这样当函数被执行完，我们可以回到最初调用它的地方：
```
(return_address1)
```

### 5.`return`实现
&emsp;&emsp;我们按照$8.6.$节所讲逐步实现`call`功能：
1. 创建一个临时变量`frame`，并把`LCL`的值赋给它。
```
@LCL
D=M
@frame
M=D
```

2. 我们将`return`的值放在栈顶。由于有四个占据主内存的内存段，因此我们需要上移4+1=5个，`frame-5`即为`return`的地址：
```
@5
A=D-A
D=M
@ret
M=D
```

> 为什么此时不考虑每个内存段中的原有元素了呢？因为**在`return`的过程中，我们需要覆盖所有的已有元素，这要求我们将这些元素当作不存在**。而如果我们考虑这些元素并将`ret`上移，**在之后的步骤中就无法覆盖一些原有的旧数据和清空调用函数的栈了**。

3. 由于我们需要将待`return`的值复制到`ARG[0]`的位置，而该值就在栈顶，我们可以轻易地通过操纵栈指针读取该值：
```
@SP
M=M-1
A=M
D=M
@ARG
A=M
M=D
```

4. 调用者需要能够获取该值，因此栈指针应与`ARG`相邻：
```
@ARG
D=M
@SP
M=D+1
```
5. 然后，我们就可以着手覆盖原有的内存段了。我们从先前设置的`frame`地址往下，依次设置新的内存段地址：
```
@frame
M=M-1
A=M
D=M
@THAT 
M=D

@frame
M=M-1
A=M
D=M
@THIS 
M=D

@frame
M=M-1
A=M
D=M
@ARG 
M=D

@frame
M=M-1
A=M
D=M
@LCL 
M=D
```
6. 最后，我们回到最初设置的`ret`位置。由于`ret`并非标签，要跳转到其地址，我们需要指针操作：
```
@ret
A=M
0;JMP
```

### 6.`Initial`实现

&emsp;&emsp;当我们需要翻译的VM代码是有效的时，我们就需要对我们的硬件系统进行初始化：
```
@256
D=A
@SP
M=D

call Sys.init// same as other call statements before.
```






























