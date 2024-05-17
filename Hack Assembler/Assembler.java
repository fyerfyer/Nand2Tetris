import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.*;

public class Assembler {
    //the first pass: deal with (xxx)
    public static void firstPass(String filename) {
        try {
            BufferedReader reader= new BufferedReader(new FileReader(filename));
            String line = reader.readLine();

            int lineCounter = 0;

            while (line != null) {
                if (line.isEmpty() || line.contains("//")) {
                    line = reader.readLine();
                    continue;
                } else if (line.charAt(0) == '(') {
                    int rightBrace = line.indexOf(')');
                    String symbol = line.substring(1, rightBrace);
                    if (!A_Instruction.symbol.containsKey(symbol)) {
                        A_Instruction.symbol.put(symbol, lineCounter);
                    }
                } else {
                    lineCounter += 1;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //the second pass: deal with variables
    public static void secondPass(String filename) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                if (line.isEmpty() || line.contains("//") || line.contains("(")) {
                    line = reader.readLine();
                    continue;
                } else if (line.charAt(0) == '@') {
                    String name = line.substring(1);
                    if (!A_Instruction.isInteger(name) && !A_Instruction.symbol.containsKey(name)) {
                        A_Instruction.AVariableconvert(name);
                    }
                }

                line = reader.readLine();
            }

            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //read in the file, and create the output file
        String inputFileName = args[0];
        String outputFileName = inputFileName.substring(0, inputFileName.indexOf(".asm")) + ".hack";
        firstPass(inputFileName);
        secondPass(inputFileName);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            BufferedReader reader = new BufferedReader(new FileReader(inputFileName));

            //read file line by line
            String line = reader.readLine();
            while (line != null) {
                String output = "";

                //deal with the top annotation and pseudo-code
                //it won't generate any code
                if (line.isEmpty() || line.contains("//") || line.contains("(")) {
                    line = reader.readLine();
                    continue;
                }

                //if the line is @xxx, go to A_instruction
                else if (line.contains("@")) {
                    output = A_Instruction.Aconvert(line);
                } else output = C_Instruction.Cconvert(line);

                //write output in the file.
                writer.write(output);
                writer.newLine();
                line = reader.readLine();
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
