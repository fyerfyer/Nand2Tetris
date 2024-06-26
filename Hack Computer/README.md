# $5.5.$Hack计算机实现笔记
### 1.CPU实现

![](image.png)

#### &emsp;&emsp;$a.$指令识别

* `instruction[15]`:用于**判断是否为`A`指令**。
* `instruction[12]`用于**选择ALU的第二个输入来源**。当`instruction[12] = 0`时，ALU的第二个输入为`A`寄存器；否则为存储器的输入数据`inM`。
* `instruction[6:11]`:用于**控制ALU的操作**。
* `instruction[5]`:当`instruction[15] = 0`时，`instruction[5]`**就是`A`指令的一部分**；否则，它就是`C`指令的一部分。
* `instruction[4]`:用于**选择ALU的第二个输入**。当`instruction[4] = 1`时，我们**将内存中读取的数据作为ALU的第二个输入(即`inM`)**；否则**将`A`寄存器中的数据作为ALU的第二个输入**。
* `instruction[3]`:用于**指示是否要向数据内存写入数据**。当`instruction[3] = 1`时，表示要向数据内存中写入数据(即`writeM = 1`)；否则不对数据内存进行写入操作。
* `C`指令对应的16位指令：

```
1 c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3
```

* `A`指令对应的16位指令

```
0 address[14:0]
```

#### &emsp;&emsp;$b.$`A`寄存器处理

![](image-1.png)

1. 输入的16位指令和ALU的输出经过`Mux16`作为`A`寄存器的输入：

```HDL
Mux16(a = instruction, b = ALUOut, sel = instruction[15], out = Ain);
```

2. 在后面的过程中，我们需要知道指令是否为`A`指令，因此我们用一个pin来记录：

```HDL
Not(in = instruction[15], out = NotA);
```

3. 判断是否需要将指令加载到`A`寄存器中。此时有两种情况：
	1. 当前指令不是`A`指令，则选择加载。
	2. 当前指令为`A`指令，但是`instruction[5] = 1`(该位置用于存储`A`指令有关信息)，则选择加载：

```HDL
Or(a = NotA, b = instruction[5] , out=loadA);
ARegister(in = Ain, load = loadA, out = Aout, out[0..14] = addressM);
```

#### &emsp;&emsp;$c.$ALU运算

![](image-2.png)

1. 我们先处理ALU的两个输入源，其中第二个输入源由`C`指令集的`instruction[12]`控制：

```HDL
Mux16(a = Aout, b = inM, sel = instruction[12], out = AMout);
And(a = instruction[15], b = instruction[4], out = loadD);
DRegister(in = ALUOut, load = loadD, out = Dout);
```

2. 然后我们根据先前的指令识别进行ALU运算并输出：

```HDL
And(a = instruction[11], b = instruction[15], out = zx);
And(a = instruction[10], b = instruction[15], out = nx);
Or(a = instruction[9], b = NotA, out = zy);
Or(a = instruction[8], b = NotA, out = ny);
And(a = instruction[7], b = instruction[15], out = f);
And(a = instruction[6], b = instruction[15], out = no);

ALU(x = Dout, y = AMout, zx = zx, nx = nx, zy = zy, ny = ny, f = f, no = no, out = outM, out = ALUOut, zr = zero, ng = nega);
```

> `zy`和`ny`控制信号在`A`指令时被强制为1是为了**确保ALU的输入在处理`A`指令时被简化与固定，不受`C`指令控制位的影响**，从而避免不必要的计算。

3. 之后我们需要执行`C`指令集的跳转逻辑。我们根据**ALU计算结果和指令中的跳转位**来决定是否跳转。

&emsp;&emsp;首先对`C`指令集的跳转位进行明晰：
* `instruction[0]`对应`JGT`，当ALU输出为正数时跳转。
* `instruction[1]`对应`JEQ`，当ALU输出为0时跳转。
* `instruction[2]`对应`JLT`，当ALU输出为负数时跳转。

&emsp;&emsp;于是我们可以写出以下代码：

```HDL
Or(a = zero, b = nega, out = notpos);
Not(in = notpos, out = pos);
    
And(a = instruction[0], b = pos, out = out3);
And(a = instruction[1], b = zero, out = out2);
And(a = instruction[2], b = nega, out = out1);

Or(a = out1, b = out2, out = out12);
Or(a = out12, b = out3, out = out123);

And(a = out123, b = instruction[15], out = jump);
```

> `pos`, `nega`, `zero`分别代表正数、负数和恰好为0。并且注意除了满足跳转条件外，还需要**确保当前是`C`指令**。

4. 最后我们调用`PC`以存储并更新当前指令的地址：

```HDL
PC(in = Aout, load = jump, reset = reset, inc = true, out[0..14] = pc);
```

### 2.内存实现

&emsp;&emsp;内存实现较为简单，根据传入的15位地址`address`的后两位以及`load`来调控RAM、屏幕以及键盘即可。

* `01 and load: RAMLoad`
* `10 and load: SCRLoad`
* `11 and load and other digit 0: KEYLoad`

### 3.Hack计算机实现

![](image-3.png)

&emsp;&emsp;我们根据计算机的结构，对已有芯片进行接线即可：

```HDL
CPU(inM = MemoryOut, instruction = Rominstruction, reset = reset, outM = CPUoutM, writeM = CPUload, addressM = CPUaddress, pc = CPUpc);
Memory(in = CPUoutM, load = CPUload, address = CPUaddress, out = MemoryOut);
ROM32K(address = CPUpc, out = Rominstruction);
```