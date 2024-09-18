import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class parallel{
    static float[][] kernel;
    static int kernelLength;
    static int width;
    static int height;
    static BufferedImage inputIMG;
    static BufferedImage outputIMG;

    static class Task implements Runnable{
        int start;
        int end;
        Task(int start, int end){
            this.start=start;
            this.end=end;
        }

        public void run(){
            applyConvolution(start,end);
        }

    }

    public static void applyConvolution(int start, int end){
        for (int i=start; i<end; i++){
            for (int j=0; j<height; j++){
                float red = 0f;
                float green = 0f;
                float blue = 0f;

                for (int m=0; m<kernelLength; m++){
                    for (int n=0; n<kernelLength; n++){
                        int iC = (i-kernelLength/2+m+width)%width;
                        int jC = (j-kernelLength/2+n+height)%height;
                        int rgbTotal = inputIMG.getRGB(iC, jC);

                        int rgbRed = (rgbTotal >> 16) & 0xff;
                        int rgbGreen = (rgbTotal >> 8) & 0xff;
                        int rgbBlue = (rgbTotal) & 0xff;

                        red += (rgbRed * kernel[m][n]);
                        green += (rgbGreen * kernel[m][n]);
                        blue += (rgbBlue * kernel[m][n]);
                    }
                }
                int redOutput = Math.min(Math.max((int) (red), 0), 255);
                int greenOutput = Math.min(Math.max((int) (green), 0), 255);
                int blueOutput = Math.min(Math.max((int) (blue), 0), 255);


                Color color = new Color(redOutput, greenOutput, blueOutput);
                outputIMG.setRGB(i, j, color.getRGB());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        String imagePath = "img9.jpg";
        parallel.inputIMG = ImageIO.read(new File(imagePath));

        parallel.width = inputIMG.getWidth();
        parallel.height = inputIMG.getHeight();
        float[][] kernel = {{-1,-1,-1},{1,5,1},{-1,-1,-1}};
        parallel.kernel = kernel;
        parallel.kernelLength = kernel.length;

        int partitions = Runtime.getRuntime().availableProcessors(); //this is number of threads we want to use.
        //int partitions = 4;
        int partitionLength = parallel.inputIMG.getWidth() / partitions;
        parallel.outputIMG = new BufferedImage(parallel.inputIMG.getWidth(),parallel.inputIMG.getHeight(),parallel.inputIMG.getType());
        System.out.println("Number of partitions is "+partitions);
        System.out.println("Processing...");

        Task[] tasks = new Task[partitions];

        //long start = System.currentTimeMillis();

        for (int i=0; i<partitions; i++){
            if (i != partitions-1)
            { tasks[i] = new Task(i*partitionLength, (i+1)*partitionLength); }
            else
            { tasks[i] = new Task(i*partitionLength, parallel.inputIMG.getWidth()); }
        }

        Thread[] threads = new Thread[partitions];
        for (int i=0; i<partitions; i++){
            threads[i] = new Thread(tasks[i]);
        }

        for (int i=0; i<partitions; i++){
            threads[i].start();
        }

        for (int i=0;i<partitions;i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        long finish = System.currentTimeMillis();
        long time = finish - start;
        System.out.println("Time needed for parallel processing: "+time);

        //File file = new File("output_image.jpg");
        //ImageIO.write(outputIMG, "jpg", file);

        parallel.outputIMG.flush();
        parallel.outputIMG = null;
        System.gc();
    }
}