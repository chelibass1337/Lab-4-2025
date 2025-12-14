package functions.basic;

import functions.Function;

public class Log implements Function {
    private double base;
    
    public Log(double base) {
        if (base <= 0) {
            throw new IllegalArgumentException("Основание логарифма должно быть > 0");
        }
        this.base = base;
    }
    
    @Override
    public double getLeftDomainBorder() {
        return 0;
    }
    
    @Override
    public double getRightDomainBorder() {
        return Double.POSITIVE_INFINITY;
    }
    
    @Override
    public double getFunctionValue(double x) {
        if (x <= 0) {
            return Double.NaN;
        }
        else{
            return Math.log(x) / Math.log(base);
        }
    }
}