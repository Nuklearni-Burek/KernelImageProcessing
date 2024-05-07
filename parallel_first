import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Paralelno implements Runnable {
    int beginning;
    int end;
    BufferedImage image;
    int[][] matrix;
    BufferedImage convolution;

    Paralelno(BufferedImage image, int beginning, int end, int[][] matrix) {
        this.beginning = beginning;
        this.end = end;
        this.image = image;
        this.matrix = matrix;
    }

    @Override
    public void run() {
        BufferedImage imgPart = image.getSubimage(beginning, 0, end - beginning, image.getHeight());
        convolution = convolution(imgPart, matrix);
    }

    private static BufferedImage convolution(BufferedImage img, int[][] matrix) {
        BufferedImage convoluted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                Color color = applyKernel(matrix, img, i, j);
                convoluted.setRGB(i, j, color.getRGB());
            }
        }
        return convoluted;
    }

    private static Color applyKernel(int[][] matrix, BufferedImage img, int i, int j) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int x = matrix.length / 2;
        int width = img.getWidth();
        int height = img.getHeight();
        for (int k = 0; k < matrix.length; k++) {
            for (int l = 0; l < matrix[0].length; l++) {
                int newX = (i - x) + k;
                int newY = (j - x) + l;
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    int argb = img.getRGB(newX, newY);
                    int alpha = (argb >> 24) & 0xFF;
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
        return new Color(red, green, blue);
    }

    public BufferedImage getConvolution() {
        return convolution;
    }
}

public class glavna {
    public static void main(String[] args) throws IOException, InterruptedException {
        String imagePath = "C:\\Users\\hasib\\OneDrive\\Radna površina\\HasibProject\\IMG_1932.jpg";
        int[][] matrix = {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
        BufferedImage image = ImageIO.read(new File(imagePath));

        int partitions = 3; // Number of partitions
        int partitionWidth = image.getWidth() / partitions;

        Paralelno[] tasks = new Paralelno[partitions];
        Thread[] threads = new Thread[partitions];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < partitions; i++) {
            int start = i * partitionWidth;
            int end = (i == partitions - 1) ? image.getWidth() : (i + 1) * partitionWidth;
            tasks[i] = new Paralelno(image, start, end, matrix);
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        BufferedImage[] convolutions = new BufferedImage[partitions];
        for (int i = 0; i < partitions; i++) {
            convolutions[i] = tasks[i].getConvolution();
        }

        BufferedImage mergedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = mergedImage.createGraphics();

        int x = 0;
        for (BufferedImage convolution : convolutions) {
            g.drawImage(convolution, x, 0, null);
            x += partitionWidth;
        }

        g.dispose();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution time: "+executionTime+" milliseconds");

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setVisible(true);
        JLabel label = new JLabel(new ImageIcon(mergedImage));
        frame.add(label);

    }
}
