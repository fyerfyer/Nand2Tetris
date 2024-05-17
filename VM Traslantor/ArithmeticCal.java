public class ArithmeticCal {
    //deal with arithmetic and logic calculation
    private static int n = 0;

    private static String[] addConvert() {
        String[] output = new String[16];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A+D";
        output[11] = "@SP";
        output[12] = "A=M";
        output[13] = "M=D";
        output[14] = "@SP";
        output[15] = "M=M+1";

        return output;
    }

    private static String[] subConvert() {
        String[] output = new String[16];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A-D";
        output[11] = "@SP";
        output[12] = "A=M";
        output[13] = "M=D";
        output[14] = "@SP";
        output[15] = "M=M+1";

        return output;
    }

    private static String[] andConvert() {
        String[] output = new String[16];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=D&A";
        output[11] = "@SP";
        output[12] = "A=M";
        output[13] = "M=D";
        output[14] = "@SP";
        output[15] = "M=M+1";

        return output;
    }

    private static String[] orConvert() {
        String[] output = new String[16];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A|D";
        output[11] = "@SP";
        output[12] = "A=M";
        output[13] = "M=D";
        output[14] = "@SP";
        output[15] = "M=M+1";

        return output;
    }

    private static String[] notConvert() {
        String[] output = new String[18];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "D=A";
        String cnt = Integer.toString(n);
        n += 1;
        output[4] = "@LABEL" + cnt;
        output[5] = "D;JEQ";
        output[6] = "@SP";
        output[7] = "A=M";
        output[8] = "M=0";
        String cnt2 = Integer.toString(n);
        n += 1;
        output[9] = "@LABEL" + cnt2;
        output[10] = "0;JMP";
        output[11] = "(LABEL" + cnt + ")";
        output[12] = "@SP";
        output[13] = "A=M";
        output[14] = "M=-1";
        output[15] = "(LABEL" + cnt2 + ")";
        output[16] = "@SP";
        output[17] = "M=M+1";

        return output;
    }

    private static String[] eqConvert() {
        String[] output = new String[25];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A-D";
        String cnt = Integer.toString(n);
        output[11] = "@LABEL" + cnt;
        output[12] = "D;JEQ";
        output[13] = "@SP";
        output[14] = "A=M";
        output[15] = "M=0";
        int tmp = n;
        n = n + 1;
        String cnt2 = Integer.toString(n);
        String cnt3 = Integer.toString(tmp);
        output[16] = "@LABEL" + cnt2;
        output[17] = "0;JMP";
        output[18] = "(LABEL" + cnt3 + ")";
        output[19] = "@SP";
        output[20] = "A=M";
        output[21] = "M=-1";
        output[22] = "(LABEL" + cnt2 + ")";
        output[23] = "@SP";
        output[24] = "M=M+1";
        n += 1;

        return output;
    }

    private static String[] ltConvert() {
        String[] output = new String[25];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A-D";
        String cnt = Integer.toString(n);
        output[11] = "@LABEL" + cnt;
        output[12] = "D;JLT";
        output[13] = "@SP";
        output[14] = "A=M";
        output[15] = "M=0";
        int tmp = n;
        n += 1;
        String cnt2 = Integer.toString(n);
        String cnt3 = Integer.toString(tmp);
        output[16] = "@LABEL" + cnt2;
        output[17] = "0;JMP";
        output[18] = "(LABEL" + cnt3 + ")";
        output[19] = "@SP";
        output[20] = "A=M";
        output[21] = "M=-1";
        output[22] = "(LABEL" + cnt2 + ")";
        output[23] = "@SP";
        output[24] = "M=M+1";
        n += 1;

        return output;
    }

    private static String[] gtConvert() {
        String[] output = new String[25];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "@SP";
        output[6] = "M=M-1";
        output[7] = "@SP";
        output[8] = "A=M";
        output[9] = "A=M";
        output[10] = "D=A-D";
        String cnt = Integer.toString(n);
        output[11] = "@LABEL" + cnt;
        output[12] = "D;JGT";
        output[13] = "@SP";
        output[14] = "A=M";
        output[15] = "M=0";
        int tmp = n;
        n += 1;
        String cnt2 = Integer.toString(n);
        String cnt3 = Integer.toString(tmp);
        output[16] = "@LABEL" + cnt2;
        output[17] = "0;JMP";
        output[18] = "(LABEL" + cnt3 + ")";
        output[19] = "@SP";
        output[20] = "A=M";
        output[21] = "M=-1";
        output[22] = "(LABEL" + cnt2 + ")";
        output[23] = "@SP";
        output[24] = "M=M+1";
        n += 1;

        return output;
    }

    private static String[] negConvert() {
        String[] output = new String[11];
        output[0] = "@SP";
        output[1] = "M=M-1";
        output[2] = "@SP";
        output[3] = "A=M";
        output[4] = "D=M";
        output[5] = "D=-D";
        output[6] = "@SP";
        output[7] = "A=M";
        output[8] = "M=D";
        output[9] = "@SP";
        output[10] = "M=M+1";

        return output;
    }

    public static String[] arithConvert(String s) {

        if (s.contains("add")) return addConvert();
        if (s.contains("sub")) return subConvert();
        if (s.contains("eq")) return eqConvert();
        if (s.contains("lt")) return ltConvert();
        if (s.contains("gt")) return gtConvert();
        if (s.contains("neg")) return negConvert();
        if (s.contains("or")) return orConvert();
        if (s.contains("not")) return notConvert();
        else return andConvert();
    }

}
