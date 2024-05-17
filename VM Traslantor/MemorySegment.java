public class MemorySegment {
    //deal with memory segment
    private static String[] PushSegmentConvert(String str, String filename) {

        String[] output = new String[100];
        if (str.contains("local")) {
            int pos = str.indexOf("local");
            String num = str.substring(pos + 5);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@LCL";
            output[3] = "A=M";
            output[4] = "AD=A+D";
            output[5] = "D=M";
            output[6] = "@SP";
            output[7] = "A=M";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "M=M+1";

            return output;
        }

        if (str.contains("argument")) {
            int pos = str.indexOf("argument");
            String num = str.substring(pos + 8);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@ARG";
            output[3] = "A=M";
            output[4] = "AD=A+D";
            output[5] = "D=M";
            output[6] = "@SP";
            output[7] = "A=M";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "M=M+1";

            return output;
        }

        if (str.contains("constant")) {
            int pos = str.indexOf("constant");
            String num = str.substring(pos + 8);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@SP";
            output[3] = "A=M";
            output[4] = "M=D";
            output[5] = "@SP";
            output[6] = "M=M+1";

            return output;
        }

        if (str.contains("temp")) {
            int pos = str.indexOf("temp");
            String num = str.substring(pos + 4);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@5";
            output[3] = "AD=A+D";
            output[4] = "D=M";
            output[5] = "@SP";
            output[6] = "A=M";
            output[7] = "M=D";
            output[8] = "@SP";
            output[9] = "M=M+1";

            return output;
        }

        if (str.contains("static")) {
            int pos = str.indexOf("static");
            String num = str.substring(pos + 6);

            output[0] = "@" + filename + "." + num;
            output[1] = "D=M";
            output[2] = "@SP";
            output[3] = "A=M";
            output[4] = "M=D";
            output[5] = "@SP";
            output[6] = "M=M+1";

            return output;
        }

        if (str.contains("pointer")) {
            int pos = str.indexOf("pointer");
            String num = str.substring(pos + 7);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@3";
            output[3] = "AD=A+D";
            output[4] = "D=M";
            output[5] = "@SP";
            output[6] = "A=M";
            output[7] = "M=D";
            output[8] = "@SP";
            output[9] = "M=M+1";

            return output;
        }

        if (str.contains("this")) {
            int pos = str.indexOf("this");
            String num = str.substring(pos + 4);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@THIS";
            output[3] = "A=M";
            output[4] = "AD=A+D";
            output[5] = "D=M";
            output[6] = "@SP";
            output[7] = "A=M";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "M=M+1";

            return output;
        }

        if (str.contains("that")) {
            int pos = str.indexOf("that");
            String num = str.substring(pos + 4);

            output[0] = "@" + num;
            output[1] = "D=A";
            output[2] = "@THAT";
            output[3] = "A=M";
            output[4] = "AD=A+D";
            output[5] = "D=M";
            output[6] = "@SP";
            output[7] = "A=M";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "M=M+1";

            return output;
        }

        return output;
    }

    private static String[] PopSegmentConvert(String s, String filename) {
        String str = s.replaceAll("\\s+", "");
        String[] output = new String[100];

        if (str.contains("local")) {
            int pos = str.indexOf("local");
            String num = str.substring(pos + 5);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@LCL";
            output[5] = "A=M";
            output[6] = "AD=A+D";
            output[7] = "@R13";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "A=M";
            output[11] = "D=M";
            output[12] = "@R13";
            output[13] = "A=M";
            output[14] = "M=D";

            return output;
        }

        if (str.contains("argument")) {
            int pos = str.indexOf("argument");
            String num = str.substring(pos + 8);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@ARG";
            output[5] = "A=M";
            output[6] = "AD=A+D";
            output[7] = "@R13";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "A=M";
            output[11] = "D=M";
            output[12] = "@R13";
            output[13] = "A=M";
            output[14] = "M=D";

            return output;
        }

        if (str.contains("static")) {
            int pos = str.indexOf("static");
            String num = str.substring(pos + 6);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@SP";
            output[3] = "A=M";
            output[4] = "D=M";
            output[5] = "@" + filename + "." + num;
            output[6] = "M=D";

            return output;
        }

        if (str.contains("temp")) {
            int pos = str.indexOf("temp");
            String num = str.substring(pos + 4);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@5";
            output[5] = "AD=A+D";
            output[6] = "@R13";
            output[7] = "M=D";
            output[8] = "@SP";
            output[9] = "A=M";
            output[10] = "D=M";
            output[11] = "@R13";
            output[12] = "A=M";
            output[13] = "M=D";

            return output;
        }

        if (str.contains("pointer")) {
            int pos = str.indexOf("pointer");
            String num = str.substring(pos + 7);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@3";
            output[5] = "AD=A+D";
            output[6] = "@R13";
            output[7] = "M=D";
            output[8] = "@SP";
            output[9] = "A=M";
            output[10] = "D=M";
            output[11] = "@R13";
            output[12] = "A=M";
            output[13] = "M=D";

            return output;
        }

        if (str.contains("this")) {
            int pos = str.indexOf("this");
            String num = str.substring(pos + 4);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@THIS";
            output[5] = "A=M";
            output[6] = "AD=A+D";
            output[7] = "@R13";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "A=M";
            output[11] = "D=M";
            output[12] = "@R13";
            output[13] = "A=M";
            output[14] = "M=D";

            return output;
        }

        if (str.contains("that")) {
            int pos = str.indexOf("that");
            String num = str.substring(pos + 4);

            output[0] = "@SP";
            output[1] = "M=M-1";
            output[2] = "@" + num;
            output[3] = "D=A";
            output[4] = "@THAT";
            output[5] = "A=M";
            output[6] = "AD=A+D";
            output[7] = "@R13";
            output[8] = "M=D";
            output[9] = "@SP";
            output[10] = "A=M";
            output[11] = "D=M";
            output[12] = "@R13";
            output[13] = "A=M";
            output[14] = "M=D";

            return output;
        }
        return output;
    }

    public static String[] SegmentConvert(String s, String filename) {
        if (s.contains("push"))  return PushSegmentConvert(s, filename);
        else return PopSegmentConvert(s, filename);
    }
}