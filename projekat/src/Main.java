import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        // Set colors for each pixel
        img.setRGB(0, 0, Color.RED.getRGB());    // Top-left pixel
        img.setRGB(1, 0, Color.GREEN.getRGB());  // Top-right pixel
        img.setRGB(0, 1, Color.BLUE.getRGB());   // Bottom-left pixel
        img.setRGB(1, 1, Color.YELLOW.getRGB()); // Bottom-right pixel

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageIO.write(img, "JPG", bs);
        bs.flush();
        byte[] imageBytes = bs.toByteArray();
        bs.close();
        for (int i=0; i<imageBytes.length;i++){
            System.out.println(i+". byte is "+imageBytes[i]);
        }
    }
}