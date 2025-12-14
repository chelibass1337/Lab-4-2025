import functions.*;
import functions.basic.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException, FunctionPointIndexOutOfBoundsException, InappropriateFunctionPointException, ClassNotFoundException {
        Function Func1 = new Cos();
        Function Func2 = new Sin();
        double pi = Math.PI;
        System.out.println("\nЗначения sin и cos\n");
        for (double i = 0; i <= pi; i += 0.1){
            System.out.printf("sin(%.2f) = %.6f \t cos(%.2f) = %.6f\n", i , Func2.getFunctionValue(i), i , Func1.getFunctionValue(i)); 
        }

        TabulatedFunction TabulatedCos = TabulatedFunctions.tabulate(Func1, 0, pi, 10);
        TabulatedFunction TabulatedSin = TabulatedFunctions.tabulate(Func2, 0, pi, 10);
        System.out.println("\nЗначения табулированных sin и cos\n");
        for (double i = 0; i <= pi; i += 0.1){
            System.out.printf("sin(%.2f) = %.6f \t cos(%.2f) = %.6f\n", i ,  TabulatedSin.getFunctionValue(i), i , TabulatedCos.getFunctionValue(i)); 
        }

        System.out.println("\nРассчет модуля разности между табулированными и исходными значениями sin и cos\n");
        for (double i = 0; i <= pi; i += 0.1){
            double absSin = Math.abs(TabulatedSin.getFunctionValue(i) - Func2.getFunctionValue(i));
            double absCos = Math.abs(TabulatedCos.getFunctionValue(i) - Func1.getFunctionValue(i));
            System.out.printf("Разность sin = %.6f \t Разность cos = %.6f\n", absSin, absCos);
        }
    
        Function SumOfSquaresSinAndCos = Functions.sum(Functions.power(TabulatedSin, 2), Functions.power(TabulatedCos, 2));
        System.out.println("\nСумма квадратов синуса и косинуса\n");
        for (double i = 0; i <= pi; i += 0.1) {
            System.out.printf("sin(%.2f)^2 + cos(%.2f)^2 = %.6f\n", i, i, SumOfSquaresSinAndCos.getFunctionValue(i));
        }

        TabulatedFunction Exp = TabulatedFunctions.tabulate(new Exp(), 0, 10, 11);
        File f = new File("exp.txt");
        FileWriter fw = new FileWriter(f);
        TabulatedFunctions.writeTabulatedFunction(Exp, fw);
        fw.close();
        FileReader fr = new FileReader(f);
        TabulatedFunction TabExp = TabulatedFunctions.readTabulatedFunction(fr);

        System.out.println("\nЗначение экспонент\n");
        for (int i = 0; i < 11; i++){
            System.out.printf("Exp = %.6f \t TabExp = %.6f\n", Exp.getFunctionValue(i), TabExp.getFunctionValue(i));
        }

        TabulatedFunction ln = TabulatedFunctions.tabulate(new Log(Math.E), 1, 10, 11);
        File f2 = new File("ln.txt");
        FileOutputStream fw2 = new FileOutputStream(f2);
        TabulatedFunctions.outputTabulatedFunction(ln, fw2);
        fw2.close();
        FileInputStream fr2 = new FileInputStream(f2);
        TabulatedFunction TabLn = TabulatedFunctions.inputTabulatedFunction(fr2);

        System.out.println("\nЗначение натурального логарифма\n");
        for (int i = 1; i < 11; i++){
            System.out.printf("Ln(" + i + ") = %.6f \t TabLn(" + i + ") = %.6f\n", ln.getFunctionValue(i), TabLn.getFunctionValue(i));
        }

        TabulatedFunction Сomposition = TabulatedFunctions.tabulate(Functions.composition(new Exp(), new Log(Math.E)), 0, 10, 11);
        
        FileOutputStream fos = new FileOutputStream("Externalizable.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(Сomposition);
        oos.close();

        FileInputStream fis = new FileInputStream("Externalizable.txt");
        ObjectInputStream ois = new ObjectInputStream(fis);
        TabulatedFunction loadedComposition = (TabulatedFunction)ois.readObject();
        ois.close();

        System.out.println("Логарифм от экспоненты():");
        for (int i = 0; i < 11; i++) {
            System.out.printf("Exp(%d) = %.6f \t TabExp(%d) = %.6f%n", i, Сomposition.getFunctionValue(i), i, loadedComposition.getFunctionValue(i));
        }
    }
}