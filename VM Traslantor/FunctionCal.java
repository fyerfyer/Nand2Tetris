public class FunctionCal {

    private static int n;
    private static String[] funcCal(String str) {
        int pos = str.indexOf("function");
        String name = str.substring(pos + 8, str.length() - 1);

        String[] output = new String[10];
        output[0] = "(" + name + ")";

        return output;
    }

    private static String[] returnCal(String s) {

        String[] output = new String[50];
        output[0] = "@LCL";
        output[1] = "D=M";
        output[2] = "@frame";
        output[3] = "M=D";

        output[4] = "@5";
        output[5] = "A=D-A";
        output[6] = "D=M";
        output[7] = "@ret";
        output[8] = "M=D";

        output[9] = "@SP";
        output[10] = "M=M-1";
        output[11] = "A=M";
        output[12] = "D=M";
        output[13] = "@ARG";
        output[14] = "A=M";
        output[15] = "M=D";

        output[16] = "@ARG";
        output[17] = "D=M";
        output[18] = "@SP";
        output[19] = "M=D+1";

        output[20] = "@frame";
        output[21] = "M=M-1";
        output[22] = "A=M";
        output[23] = "D=M";
        output[24] = "@THAT";
        output[25] = "M=D";

        output[26] = "@frame";
        output[27] = "M=M-1";
        output[28] = "A=M";
        output[29] = "D=M";
        output[30] = "@THIS";
        output[31] = "M=D";

        output[32] = "@frame";
        output[33] = "M=M-1";
        output[34] = "A=M";
        output[35] = "D=M";
        output[36] = "@ARG";
        output[37] = "M=D";

        output[38] = "@frame";
        output[39] = "M=M-1";
        output[40] = "A=M";
        output[41] = "D=M";
        output[42] = "@LCL";
        output[43] = "M=D";

        output[44] = "@ret";
        output[45] = "A=M";
        output[46] = "0;JMP";

        return output;
    }

    private static String[] callCal(String str) {

        String name = str.substring(4, str.length() - 1);
        int num = Character.getNumericValue(str.charAt(str.length() - 1)) + 5;

        String[] output = new String[100];
        output[0] = "@" + "return_address" + Integer.toString(n);
        output[1] = "D=A";

        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "M=D";
        output[5] = "@SP";
        output[6] = "M=M+1";

        output[7] = "@LCL";
        output[8] = "D=M";
        output[9] = "@SP";
        output[10] = "A=M";
        output[11] = "M=D";
        output[12] = "@SP";
        output[13] = "M=M+1";

        output[14] = "@ARG";
        output[15] = "D=M";
        output[16] = "@SP";
        output[17] = "A=M";
        output[18] = "M=D";
        output[19] = "@SP";
        output[20] = "M=M+1";

        output[21] = "@THIS";
        output[22] = "D=M";
        output[23] = "@SP";
        output[24] = "A=M";
        output[25] = "M=D";
        output[26] = "@SP";
        output[27] = "M=M+1";

        output[28] = "@THAT";
        output[29] = "D=M";
        output[30] = "@SP";
        output[31] = "A=M";
        output[32] = "M=D";
        output[33] = "@SP";
        output[34] = "M=M+1";

        output[35] = "@" + Integer.toString(num);
        output[36] = "D=A";
        output[37] = "@SP";
        output[38] = "A=M";
        output[39] = "AD=A-D";
        output[40] = "@ARG";
        output[41] = "M=D";

        output[42] = "@SP";
        output[43] = "D=M";
        output[44] = "@LCL";
        output[45] = "M=D";

        output[46] = "@" + name;
        output[47] = "0;JMP";
        output[48] = "(" + "return_address" + Integer.toString(n) + ")";
        n++;

        return output;
    }

    public static String[] functionCal(String s) {
        if (s.contains("call")) return callCal(s);
        if (s.contains("return")) return returnCal(s);
        else return funcCal(s);
    }
}
