public class Complex {
    double re;
    double im;

    public Complex() {
        this.re = 0;
        this.im = 0;
    }

    public Complex(double a, double b) {
        this.re = a;
        this.im = b;
    }

    public double magnitude() {
        return Math.sqrt(re * re + im * im);
    }

    public Complex add(Complex other) {
        return new Complex(this.re + other.re, this.im + other.im);
    }

    public Complex mult(Complex other) {
        double r = this.re*other.re - this.im*other.im;
        double i = this.re*other.im + this.im*other.re;

        return new Complex(r, i);
    }

}
