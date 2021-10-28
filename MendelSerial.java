// serial Time taken: 77.251166324
// parallel Time taken: 37.560134994 with tile size 1000

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import javax.imageio.ImageIO;

public class MendelSerial {
    private static final double TICKS = 1_000_000_000.0;
    public  final int MAX_ITER = 5000;

    int[] depths;
    Complex upperLeft;
    Complex lowerRight;
    int width;
    int height;

    public MendelSerial(int[] d, Complex ul, Complex lr, int w, int h){
        depths = d;
        upperLeft = ul;
        lowerRight = lr;
        width = w;
        height = h;
    }

    public void checkAndOutput(String name){
        MendelRecursive up = new MendelRecursive(0,height);
        ForkJoinPool.commonPool().invoke(up);
    }

    private  int index(int i, int j) {
        return i * width + j;
    }


    private  int check(Complex c) {
        Complex z = new Complex();
        int count = 0;
        for (int k = 0; k < MAX_ITER; ++k) {
            if (z.magnitude() >= 2.0) {
                break;
            }
            z = z.mult(z).add(c);
            count++;
        }
        return count;
    }


    public  void mandelbrot() {
        // (0,0) => upperLeft; (height-1, width-1) => lowerRight
        Complex c = new Complex();
        for (int i = 0; i < height; ++i) {
            c.im = upperLeft.im + i * (lowerRight.im - upperLeft.im) / (height - 1);
            for (int j = 0; j < width; ++j) {
                c.re = upperLeft.re + j * (lowerRight.re - upperLeft.re) / (width - 1);
                depths[index(i, j)] = check(c);
            }
        }
    }

    public int mandelsect(int start, int end) {
        Complex c = new Complex();
        for (int i = start; i < end; ++i) {
            c.im = upperLeft.im + i * (lowerRight.im - upperLeft.im) / (height - 1);
            for (int j = 0; j < width; ++j) {
                c.re = upperLeft.re + j * (lowerRight.re - upperLeft.re) / (width - 1);
                depths[index(i, j)] = check(c);
            }
        }
        return 0;
    }

    public  Color shade(int k) {
        if (k == MAX_ITER) {
            return new Color(0, 0, 0);
        }
        else if (k < 20) {
            return new Color(40, 15, 15);
        }
        else if (k < 100) {
            return new Color(100, 20, 20);
        }
        else {
            return new Color(20, 220, 20);
        }
    }

    public  void output(String picName) {
        try {
            // retrieve image
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    bi.setRGB(j, i, shade(depths[index(i, j)]).getRGB());
                }
            }

            File outputfile = new File(picName + ".png");
            ImageIO.write(bi, "png", outputfile);
            System.out.println("Image written");
        }
        catch (IOException e) {
            System.out.println("oops");
        }
    }

    public static void main(String[] args) {
        final int SIZE = 4000;
        int[] pixels = new int[SIZE * SIZE];

        //Use these coordinates to zoom in on a nice region:
        //set(pixels, new Complex(-1.172277, -0.295644), new Complex(-1.172258, -0.295625), SIZE, SIZE);
        //set(pixels, new Complex(-2, 1.5), new Complex(1, -1.5), SIZE, SIZE);

        long start = System.nanoTime();
        MendelSerial test = new MendelSerial(pixels, new Complex(-2, 1.5), new Complex(1, -1.5), SIZE, SIZE);
        test.checkAndOutput("pic");
        System.out.println("Time taken: " + (System.nanoTime() - start)/TICKS);
        //output("pic");
    }



    private class MendelRecursive extends RecursiveAction{
        int start;
        int end;

        public MendelRecursive(int start,int end){
            this.start=start;
            this.end=end;
        }
        public void compute() {
            if (end-start <= 1000) {
                // base case. Compute a reduction over the ith tile.
                mandelsect(start, end);
                return;
            }
                int k = (end-start)/2;
                var left = new MendelRecursive(start, start+k);
                left.fork();
                var right = new MendelRecursive(start+k, end);
                right.compute();
                left.join();
            }
    }
}
