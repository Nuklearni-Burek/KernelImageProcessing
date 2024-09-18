import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class sequential {
    public static void main(String[] args) throws IOException {
        String fileLocation= "img5.jpg";
        float[][] kernel = {{0,-1,0},{-1,5,-1},{0,-1,0}}; // sharpen
        int kernelLength = kernel.length;

        BufferedImage inputIMG = ImageIO.read(new File(fileLocation));
        int width = inputIMG.getWidth();
        int height =  inputIMG.getHeight();
        BufferedImage outputIMG = new BufferedImage(width, height, inputIMG.getType());
        System.out.println("Processing...");
        long start = System.currentTimeMillis();

        for (int i=0; i<width; i++){
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

        long finish = System.currentTimeMillis();
        long time = finish - start;
        System.out.println("Time needed for sequential processing: "+time);

        //File file = new File("output_image.jpg");
        //ImageIO.write(outputIMG, "jpg", file);

        outputIMG.flush();
        outputIMG = null;
        System.gc();

    }
}
