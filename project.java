import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class project {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        String imagePath ="C:\\Users\\hasib\\Downloads\\images.jpg";
        int[][] matrix = {{1,2,1},{0,0,0},{-1,-2,-1}}; //edge detection
        BufferedImage image = ImageIO.read(new File(imagePath));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setSize(300,300);
        frame.setVisible(true);
        BufferedImage image2 = Convolution(image,matrix);

        JLabel label = new JLabel(new ImageIcon(image2));
        frame.add(label);

        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime + " milliseconds");

    }

    public static BufferedImage Convolution (BufferedImage img, int[][] matrix){
        BufferedImage convoluted = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for (int i=0; i<img.getWidth();i++) {
            for (int j=0; j< img.getHeight(); j++) {
                int red=0;
                int blue=0;
                int green=0;
                int x= matrix.length/2;
                int width= img.getWidth();
                int height = img.getHeight();
                for (int k = 0; k < matrix.length; k++) {
                    for (int l = 0; l < matrix[0].length; l++) {
                        int newX = (i - x)+k;
                        int newY = (j - x) + l;
                        if (newX>=0 && newX<width && newY>=0 && newY<height){
                            int a= img.getRGB(newX,newY);
                            int ared = ((a>>16)& 0xff) * matrix[k][l];
                            int agreen = ((a>>8)& 0xff) * matrix[k][l];
                            int ablue = (a & 0xff) * matrix[k][l];
                            red += (ared);
                            green += (agreen);
                            blue += (ablue);
                        }
                    }
                }
                red = Math.min(Math.max(red, 0), 255);
                green = Math.min(Math.max(green, 0), 255);
                blue = Math.min(Math.max(blue, 0), 255);
                Color color = new Color(red, green, blue);
                convoluted.setRGB(i, j, color.getRGB());
            }
        }
        return convoluted;
    }

}
