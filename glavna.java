import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class glavna {
    static String imagePath = "C:\\Users\\hasib\\OneDrive\\Radna površina\\HasibProject\\IMG_1933.jpg";
    static int[][] matrix = {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
    static BufferedImage image;

    static {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static BufferedImage image_final = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
    public glavna() throws IOException {
    }

    public static void main(String[] args) {
        Date s = new Date();
        BufferedImage final_image = KernelThreadPool(2);
        Date e = new Date();
        System.out.println("Execution time: "+(e.getTime() - s.getTime()));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setSize(final_image.getWidth(), final_image.getHeight());
        frame.setVisible(true);

        JLabel label = new JLabel(new ImageIcon(final_image));
        frame.add(label);
    }

    public static BufferedImage KernelThreadPool(int nt){
        ExecutorService ExService = Executors.newFixedThreadPool(nt);
        for (int i=0;i<image.getWidth() ;i++){
            for (int j=0;j<image.getHeight();j++) {
                KernelThreadPoolRunnable task = new KernelThreadPoolRunnable(i, j);
                ExService.execute(task);
            }
        }
        ExService.shutdown();
        return image_final;
    }



}
 class KernelThreadPoolRunnable implements Runnable{
    BufferedImage image = glavna.image;
    BufferedImage image_final = glavna.image_final;
    int i;
    int j;
    int[][] matrix = glavna.matrix;

    KernelThreadPoolRunnable (int i, int j) {
        this.i = i;
        this.j = j;
    }

    public void run(){
        int red = 0;
        int green = 0;
        int blue = 0;

        int x= matrix.length / 2;
        int width = image.getWidth();
        int height = image.getHeight();

        for (int k = 0; k < matrix.length; k++) {
            for (int l = 0; l < matrix[0].length; l++) {
                int newX = (i - x) + k;
                int newY = (j - x) + l;
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    int argb = image.getRGB(newX, newY);
                    int redChannel = (argb >> 16) & 0xFF;
                    int greenChannel = (argb >> 8) & 0xFF;
                    int blueChannel = argb & 0xFF;
                    red += redChannel * matrix[k][l];
                    green += greenChannel * matrix[k][l];
                    blue += blueChannel * matrix[k][l];
                }
            }
        }
        red = Math.min(Math.max(red, 0), 255);
        green = Math.min(Math.max(green, 0), 255);
        blue = Math.min(Math.max(blue, 0), 255);

        image_final.setRGB(i,j, (new Color(red,green,blue)).getRGB());

    }



 }
