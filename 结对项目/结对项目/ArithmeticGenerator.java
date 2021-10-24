package com.lfh;

import java.io.*;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.Scanner;

public class ArithmeticGenerator {
    public static void main(String[] args) throws IOException {
        //参数整型化
//        int args1=Integer.parseInt(args[1]);
//        int args3=Integer.parseInt(args[3]);

        //生成题目和答案
//        textGenerator(args[0],args1,args[2],args3,args[4],args[5],args[6],args[7]);
        textGenerator("-n",10,"-r",10,"-e","Exercise.txt","-a","Answer.txt");

        //等待作业提交
        System.out.println("题目已生成，请作答并在输入答案后保存文本，然后在命令行中输入finish再回车来提交你的答案");
        Scanner sc=new Scanner(System.in);
        while (!sc.next().equals("finish")){
            System.out.println("您的输入有误，请输入finish");
        }
        //批改作业
        System.out.println("批改结果已生成");
//        correctionTextGenerator(args[5], args[7],args1);
        correctionTextGenerator("Exercise.txt", "Answer.txt",10);

    }

    //生成计算题目
    private static String[] calculationProblemGenerator(int range) {
        //运算符的数量
        int symbolNumber = symbolNumber();
        //创建存储运算符的字符串数组并随机初始化
        String[] symbolArray = new String[symbolNumber];
        for (int j = 0; j < symbolNumber; j++) {
            symbolArray[j] = symbolGenerator();
        }
        //运算数的数量
        int operationNumber = symbolNumber + 1;
        //创建存储运算数的字符串数组并随机初始化
        String[] numberArray = new String[operationNumber];
        for (int j = 0; j < operationNumber; j++) {
            numberArray[j] = numberGenerator(range);
        }
        //创建并初始化一个暂时的容量为20的字符串数组用于存储计算题
        String[] temporaryCalculationProblem = new String[20];
        //根据随机数是否在题目中生成结合运算符的标记，如为0则不生成
        int bracket = new Random().nextInt(2);
        if (symbolNumber >= 2 && bracket == 1) {
            //创建计算题数组的结合运算符的插入索引数组并生成索引
            int[] bracketIndex = bracketGenerator(symbolNumber);
            //插入结合运算符
            temporaryCalculationProblem[bracketIndex[0]] = "(";
            temporaryCalculationProblem[bracketIndex[1]] = ")";
        }
        //在运算题目中交替插入运算数和运算符号
        int index = 0;
        int count = 1;
        //生成局部变量存储计算数和运算符的数量，用于插入计算数和运算符
        int operationReplace = operationNumber, symbolReplace = symbolNumber;
        //当还有运算数和运算符没有插入计算题时
        while (operationReplace > 0 || symbolReplace > 0) {
            //如果计算题数组当前位置为空时
            if (temporaryCalculationProblem[index] == null) {
                //count为奇数时插入运算数
                if (count % 2 == 1) {
                    temporaryCalculationProblem[index] = numberArray[operationNumber - operationReplace];
                    operationReplace--;
                }
                //count为偶数时插入运算符
                else {
                    temporaryCalculationProblem[index] = symbolArray[symbolNumber - symbolReplace];
                    symbolReplace--;
                }
                count++;
            }
            index++;
        }
        //此时index索引指向的可能是右括号，以致于无法插入=号，所以要进一步检查
        if (temporaryCalculationProblem[index]==")") {
            index++;
        }
        //插入=号
        temporaryCalculationProblem[index] = " = ";
        //创建最终的计算题目，保证计算题数组没有值为null的数组元素
        //返回计算题
        return reduceArrayLength(temporaryCalculationProblem);
    }

    //缩减数组长度
    public static String[] reduceArrayLength(String[] Array) {
        int length=0;
        //获取旧数组的有效长度
        while (Array[length]!=null)
        {
            length++;
        }
        //创建新数组并复制旧数组有效内容
        String[] newArray=new String[length];
        System.arraycopy(Array, 0, newArray, 0, length);

        return newArray;
    }

    //随机决定1~3个运算符号的数量
    public static int symbolNumber() {
        return new Random().nextInt(3) + 1;
    }

    //随机生成运算符号
    public static String symbolGenerator() {
        //生成随机数决定+、-、*、/的运算符号
        int i = new Random().nextInt(4);
        //生成运算符号并返回
        String[] symbol = {" + ", " - ", " × ", " ÷ "};
        return symbol[i];
    }

    //随机生成运算数
    public static String numberGenerator(double range) {
        Random r = new Random();
        //生成不为0的分母
        int denominator = 0;
        while (denominator == 0) {
            denominator = r.nextInt(10);
        }
        //生成分子并计算该随机数及返回该数的字符串形式
        while (true) {
            int molecule = r.nextInt((int)range*10);
            //求最大公约数并对分子分母进行约分
            int greatestCommonDivisor = rollingPhaseDivision(molecule, denominator);
            molecule = molecule / greatestCommonDivisor;
            denominator = denominator / greatestCommonDivisor;

            //根据给定参数范围生成随机数，该随机数以字符串的形式返回
            double result = (double) molecule / denominator;
            if (result < range) {
                double modResult = result % 1;
               return numberChangeString(molecule,denominator,result,modResult);
            }
        }
    }

    //真分数转字符串
    public static String numberChangeString(int molecule,int denominator,double result,double modResult){
        //返回带整数的真分数
        StringBuilder sb = new StringBuilder();
        if (result > 1 && modResult > 0) {
            return sb.append(molecule / denominator).append("'").append(molecule % denominator).append("/").append(denominator).toString();
        }
        //返回不带整数的真分数
        else if (result < 1 && modResult > 0) {
            return sb.append(molecule).append("/").append(denominator).toString();
        }
        //返回整数
        else return sb.append(molecule / denominator).toString();
    }

    //用辗转相除法求最大公约数
    public static int rollingPhaseDivision(int a, int b) {
        while (a % b > 0) {
            int temp = a;
            a = b;
            b = temp % b;
        }
        return b;
    }

    //加入结合运算符
    public static int[] bracketGenerator(int symbolNumber) {
        //结合运算符索引的随机生成
        Random r = new Random();
        int random = 1;
        //运算符为2时
        if (symbolNumber == 2) {
            //选定[0,4]或[2,6]为括号插入位置
            while (random % 2 == 1) {
                random = r.nextInt(3);
            }
        }
        //运算符为3时
        else if (symbolNumber == 3) {
            //选定[0,4]、[2,6]或[4,8]为括号插入位置
            while (random % 2 == 1) {
                random = r.nextInt(5);
            }
        }
        //返回结合运算符的索引数组
        return new int[]{random, random + 4};
    }

    //逆波兰排序
    public static String[] reversePolishSort(String[] input) {
        //创建一个栈且栈指针指向-1
        String[] stack = new String[20];
        int topOfStack = -1;
        //创建一个输出数组用于输出后缀排序并且创建一个数组索引
        String[] temporaryOutput = new String[20];
        int outputIndex = 0;
        //运算题目的逆波兰处理，+、-、×、÷的前后都有空格，故下面的charAt索引用1
        for (String s : input) {
                char firstChar = s.charAt(0);
                char secondString='?';
                if (s.length()>=2) {
                    secondString = s.charAt(1);
                }

                //字符串为运算数时，数据直接插入输出数组
                if (firstChar != ' ' && firstChar != '(' && firstChar != ')') {
                    temporaryOutput[outputIndex++] = s;
                    continue;
                }
                //字符串为×、÷时
                if (secondString == '×' || secondString == '÷') {
                    //栈为空时,直接入栈
                    if (topOfStack == -1) {
                        stack[++topOfStack] = s;
                    }
                    //栈非空时
                    else {
                        //栈顶为+、-、(时，直接入栈
                        if (stack[topOfStack].equals(" + ") || stack[topOfStack].equals(" - ") || stack[topOfStack].equals("(")){
                            stack[++topOfStack] = s;
                        }
                        //栈顶为×、÷时，先弹栈再入栈
                        else if (stack[topOfStack].equals(" × ") || stack[topOfStack].equals(" ÷ ")) {
                            temporaryOutput[outputIndex++] = stack[topOfStack];
                            stack[topOfStack] = s;
                        }
                    }
                    continue;
                }
                //字符串为+、-时
                if (secondString== '+' || secondString == '-') {
                    while (topOfStack >= 0 && !stack[topOfStack].equals("(")) {
                        temporaryOutput[outputIndex++] = stack[topOfStack--];
                    }
                    stack[++topOfStack] = s;
                }
                //左括号直接入栈
                if (firstChar == '(') {
                    stack[++topOfStack] = s;
                }
                //当字符串为右括号时
                if (firstChar == ')') {
                    //栈为空时，抛出异常
                    if (topOfStack == -1)
                        throw new EmptyStackException();
                    //栈不为空时
                    while (!stack[topOfStack].equals("(")) {
                        //括号之间的运算符依次从栈顶输出到输出数组
                        temporaryOutput[outputIndex++] = stack[topOfStack--];
                    }
                    //除去栈中的(
                    if (stack[topOfStack].equals("(")){
                        topOfStack--;
                    }
                }
        }
        //弹出栈中剩余的运算符
        while (topOfStack >= 0) {
            temporaryOutput[outputIndex++] = stack[topOfStack--];
        }
        //缩减数组长度

        return reduceArrayLength(temporaryOutput);
    }

    //逆波兰计算
    public static String reversePolishCalculation(String[] output){
        String[] stack=new String[20];
        int topOfStack=-1;
        for (String s:output){
            char firstChar=s.charAt(0);
            //数据直接入栈
            if (firstChar!=' '){
                stack[++topOfStack]=s;
            }
            //如遇到运算符则从栈中取出两数进行计算
            else if (firstChar==' '&& !s.equals(" = ")){
                String result=changeAndCalculate(stack[topOfStack-1],stack[topOfStack],s);
                if (result.equals("-1"))
                    return "-1";
                stack[--topOfStack]=result;
            }
        }

        return stack[topOfStack];
    }

    //字符串数据转为具体计算数并计算
    public static String changeAndCalculate(String number1,String number2,String symbol){
        //创建一个数组，索引0、1存储第一个数的分子、分母，索引2、3存储第二个数的分子、分母
        int[] Santiago=new int[4];
        int index=0;
        String[] input=new String[]{number1,number2};
        for (String s:input){
            //运算数为整数带分数时
            if (s.contains("'") &&s.contains("/")){
                String[] temp1=s.split("'");
                String[] temp2=temp1[1].split("/");
                int integer=Integer.parseInt(temp1[0]);
                int denominator=Integer.parseInt(temp2[1]);
                int molecule=Integer.parseInt(temp2[0])+integer*denominator;
                Santiago[index++]=molecule;
                Santiago[index++]=denominator;
            }
            //运算数为不带整数的真分数时
            else if (s.contains("/")){
                String[] temp=s.split("/");
                Santiago[index++]=Integer.parseInt(temp[0]);
                Santiago[index++]=Integer.parseInt(temp[1]);
            }
            //运算数为整数时
            else {
                Santiago[index++]=Integer.parseInt(s);
                Santiago[index++]=1;
            }
        }
        //计算运算结果
        int molecule;
        int denominator;
        switch (symbol){
            case " + ":
                molecule=Santiago[0]*Santiago[3]+Santiago[2]*Santiago[1];
                denominator=Santiago[1]*Santiago[3];
                break;
            case " - ":
                molecule=Santiago[0]*Santiago[3]-Santiago[2]*Santiago[1];
                denominator=Santiago[1]*Santiago[3];
                if (molecule<0){
                    return "-1";
                }
                break;
            case " × ":
                molecule=Santiago[0]*Santiago[2];
                denominator=Santiago[1]*Santiago[3];
                if (molecule>=2147483647)
                    return "-1";
                break;
            case " ÷ ":
                if (Santiago[2]==0){
                    return "-1";
                }
                molecule=Santiago[0]*Santiago[3];
                denominator=Santiago[2]*Santiago[1];
                if (molecule>=2147483647)
                    return "-1";
                break;
            default:
                throw new IllegalArgumentException();
        }
        //约分
        int greatestCommonDivisor=rollingPhaseDivision(molecule,denominator);
        molecule/=greatestCommonDivisor;
        denominator/=greatestCommonDivisor;
        //数字转换并返回字符串
        double result = (double) molecule / denominator;
        double modResult=result%1;
        return numberChangeString(molecule,denominator,result,modResult);
    }

    //生成作业文本和答案文本
    public static void textGenerator(String n,int number,String r,int range,String e,String file1,String a,String file2) throws IOException {
        if (!n.equals("-n") || !r.equals("-r") || !e.equals("-e") || !a.equals("-a")){
            System.out.println("参数异常");
            System.exit(0);
        }
        BufferedWriter bw1=new BufferedWriter(new FileWriter(file1));
        BufferedWriter bw2=new BufferedWriter(new FileWriter(file2));

        bw1.write("本次作业所要解答的四则运算题如下,请解答并将结果写在等号右方：");
        bw1.write("\n\r");
        bw2.write("以下为参考答案：");
        bw2.write("\n\r");

        for (int i=0;i<number;i++){
            //生成题目
            String[] calculationProblem = calculationProblemGenerator(range);
            //逆波兰排序
            String[] temp= reversePolishSort(calculationProblem);
            //逆波兰计算
            String result=reversePolishCalculation(temp);

            //使运算过程中不会出现负数和结果非负
            if (result.charAt(0)=='-')
            {
                i--;
                continue;
            }

            //题目文本和答案文本的编辑
            bw1.write(i+1+"、");
            bw2.write(i+1+"、");
            for (String s : calculationProblem)
            {
                bw1.write(s);
            }
            bw1.write("\n\r");
            bw1.flush();

            bw2.write(result);
            bw2.write("\n\r");
            bw2.flush();
        }

        bw1.close();
        bw2.close();
    }

    //生成作业批改文本
    public static void correctionTextGenerator(String Exercise,String Answer,int n) throws IOException {
        BufferedReader bf1=new BufferedReader(new FileReader(Exercise));
        BufferedReader bf2=new BufferedReader(new FileReader(Answer));
        BufferedWriter bw=new BufferedWriter(new FileWriter("Grade.txt"));

        //读取作答文本和答案文本
        String exercise=bf1.readLine();
        String answer= bf2.readLine();
        int[] a=new int[n];
        int count=0;
        //进行作答与答案的对比来评分
        while (exercise!=null&&answer!=null){
            if (exercise.contains(" = ") && answer.contains("、")){
                String[] s1=exercise.split(" ");
                String[] s2=answer.split("、");
                String result = null;
                for (String s:s1){
                    result=s;
                }
                if (result.equals(s2[1])){
                    a[count++]=1;
                }
                else {
                    a[count++]=0;
                }
            }
            exercise=bf1.readLine();
            answer=bf2.readLine();
        }

        //计算对错的总题数
        int sum1=0;
        for (int i=0;i<n;i++)
            sum1+=a[i];
        int sum2=10-sum1;

        //输出正确的结果
        int count1=1;
        bw.write("Correct: "+sum1+" (");
        for (int i=0;i<a.length;i++){
            if (a[i]==1&&count1<sum1){
                bw.write((i+1)+",");
                count1++;
            }
            else if (a[i]==1&&count1==sum1){
                String s=Integer.toString(i+1);
                bw.write(s);
            }
            bw.flush();
        }
        bw.write(")");
        bw.flush();

        bw.write("\n\r");
        bw.flush();

        //输出错误的结果
        int count2=1;
        bw.write("Wrong: "+sum2+" (");
        for (int i=0;i<n;i++){
            if (a[i]==0&&count2<sum2){
                bw.write((i+1)+",");
                count2++;
            }
            else if (a[i]==0&&count2==sum2){
                 String s=Integer.toString(i+1);
                bw.write(s);
            }
            bw.flush();
        }
        bw.write(")");
        bw.flush();

        bf1.close();
        bf2.close();
        bw.close();
    }
}
