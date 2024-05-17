import java.util.Map;
import java.util.HashMap;

public class C_Instruction {
    private static Map cDestMap, cCompMap, cJumpMap;

    static {
        cDestMap = new HashMap<>();
        cCompMap = new HashMap<>();
        cJumpMap = new HashMap<>();
        //dest mapping
        cDestMap.put("", "000");
        cDestMap.put("M", "001");
        cDestMap.put("D", "010");
        cDestMap.put("MD", "011");
        cDestMap.put("A", "100");
        cDestMap.put("AM", "101");
        cDestMap.put("AD", "110");
        cDestMap.put("AMD", "111");

        //jump mapping
        cJumpMap.put("", "000");
        cJumpMap.put("JGT", "001");
        cJumpMap.put("JEQ", "010");
        cJumpMap.put("JGE", "011");
        cJumpMap.put("JLT", "100");
        cJumpMap.put("JNE", "101");
        cJumpMap.put("JLE", "110");
        cJumpMap.put("JMP", "111");

        //comp mapping
        cCompMap.put("0", "0101010");
        cCompMap.put("1", "0111111");
        cCompMap.put("-1", "0111010");
        cCompMap.put("D", "0001100");
        cCompMap.put("A", "0110000");
        cCompMap.put("M", "1110000");
        cCompMap.put("!D", "0001101");
        cCompMap.put("!A", "0110001");
        cCompMap.put("!M", "1110001");
        cCompMap.put("-D", "0001111");
        cCompMap.put("-A", "0110011");
        cCompMap.put("-M", "1110011");
        cCompMap.put("D+1", "0011111");
        cCompMap.put("A+1", "0110111");
        cCompMap.put("M+1", "1110111");
        cCompMap.put("D-1", "0001110");
        cCompMap.put("A-1", "0110010");
        cCompMap.put("M-1", "1110010");
        cCompMap.put("D+A", "0000010");
        cCompMap.put("D+M", "1000010");
        cCompMap.put("D-A", "0010011");
        cCompMap.put("D-M", "1010011");
        cCompMap.put("A-D", "0000111");
        cCompMap.put("M-D", "1000111");
        cCompMap.put("D&A", "0000000");
        cCompMap.put("D&M", "1000000");
        cCompMap.put("D|A", "0010101");
        cCompMap.put("D|M", "1010101");
    }

    public static String Cconvert(String s) {
        //divide the C instruction into three parts
        String str = s.trim().replaceAll("\\s+", "");

        int hasEqual = str.indexOf('=');
        int hasJump = str.indexOf(';');

        String dest = "";
        String comp = "";
        String jump = "";

        //0;JMP, for example
        if (hasEqual == -1) {
            comp = str.substring(0, hasJump);
            jump = str.substring(hasJump + 1);
        } else if (hasJump == -1) {
            //D=D-M, for example
            dest = str.substring(0, hasEqual);
            comp = str.substring(hasEqual + 1);
        }

        String output = "";
        output = "111" + cCompMap.get(comp) + cDestMap.get(dest) + cJumpMap.get(jump) + output;
        return output;
    }

}
