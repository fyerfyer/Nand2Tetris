import java.io.*;
import java.io.IOException;

public class VMTranslator {

    public static void main (String[] args) {
        String outputFileName = "Program.asm";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            // if args is a directory
            // then it should contain more than 1 element
            if (args.length > 1) {
                String[] spInit = {"@256",
                                   "D=A",
                                   "@SP",
                                   "M=D"};

                for (String s : spInit) {
                    writer.write(s);
                    writer.newLine();
                }

                String[] init = FunctionCal.functionCal("call Sys.init 0");
                for (String s : init) {
                    writer.write(s);
                    writer.newLine();
                }
            }

            for (String file : args) {

                String filename = file.replace(".vm", "");
                BufferedReader reader = new BufferedReader(new FileReader(file));

                //get the line without '//' or space
                String line= StringOperation.modify(reader.readLine());

                while (line != null) {
                    //deal with the top annotation
                    //it won't generate any code
                    if (line.isEmpty() || line.indexOf("//") == 0 || line.matches("\\s*//.*")) {
                        line = reader.readLine();
                        continue;
                    }

                    if (line.contains("push") || line.contains("pop")) {
                        String[] operations = MemorySegment.SegmentConvert(line, filename);
                        for (String s : operations) {
                            if (s == null) break;
                            writer.write(s);
                            writer.newLine();
                        }
                    } else if (line.contains("label") || line.contains("goto")) {
                        String[] operations = LoopCal.loopConvert(line);
                        for (String s : operations) {
                            if (s == null) break;
                            writer.write(s);
                            writer.newLine();
                        }
                    } else if (line.contains("function") || line.contains("call") || line.contains("return")) {
                        String[] operations = FunctionCal.functionCal(line);
                        for (String s : operations) {
                            if (s == null) break;
                            writer.write(s);
                            writer.newLine();
                        }
                    } else {
                        String[] operations = ArithmeticCal.arithConvert(line);
                        for (String s : operations) {
                            if (s == null) break;
                            writer.write(s);
                            writer.newLine();
                        }
                    }

                    line= StringOperation.modify(reader.readLine());
                }
                reader.close();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
