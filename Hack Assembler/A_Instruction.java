import java.util.Map;
import java.util.HashMap;

public class A_Instruction {
    private static int n = 16;
    public static Map symbol;

    static {
        //the pre-defined symbols
        symbol = new HashMap<>();
        symbol.put("R0", 0);
        symbol.put("R1", 1);
        symbol.put("R2", 2);
        symbol.put("R3", 3);
        symbol.put("R4", 4);
        symbol.put("R5", 5);
        symbol.put("R6", 6);
        symbol.put("R7", 7);
        symbol.put("R8", 8);
        symbol.put("R9", 9);
        symbol.put("R10", 10);
        symbol.put("R11", 11);
        symbol.put("R12", 12);
        symbol.put("R13", 13);
        symbol.put("R14", 14);
        symbol.put("R15", 15);
        symbol.put("SCREEN", 16384);
        symbol.put("KBD", 24576);
    }

    //judge if the elements after @ is a number
    //if it's false, then it's a symbol
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // the s is the name of variable
    public static int AVariableconvert(String s) {
        // if is found in the symbol table, return it
        if (symbol.get(s) != null) return (int)symbol.get(s);

        //else, it's a new variable, find its own address
        while(symbol.containsKey(n)) n += 1;

        //put it into the symbol table
        symbol.put(s, n);
        int pos = n;

        //update the counter
        n += 1;
        return pos;
    }

    //the s is @value, not value!
    public static String Aconvert(String s) {
        String output = "";
        String str = s.trim().replaceAll("\\s+", "");
        String substr = str.substring(1);
        int num;
        //plain A instruction
        if (isInteger(substr)) {
            num = Integer.parseInt(substr);

        } else { //solve the symbol problem
            num = AVariableconvert(substr);
        }

        for (int i = 0; i < 15; i += 1) {
            int digit = num % 2;
            num -= digit;
            output = digit + output;
            num /= 2;
        }
        output = "0" + output;
        return output;
    }

}
