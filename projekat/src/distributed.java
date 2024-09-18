import mpi.MPI;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import static java.lang.Math.sqrt;

class distributed {
    public static void main(String[] args) throws IOException {

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        float[] kernel = new float[9];

        byte[] imageBytes = null;
        BufferedImage inputIMG = null;

        int[] WidthAndHeight = new int[2];
        BufferedImage[] PartsConvoluted = new BufferedImage[size]; //tu idu svi finalni djelovi
        long startTime=0;
        long endTime=0;
        String imagePath = "img4.jpg";


        int WIDTH = 0;
        int HEIGHT=0;
        int IMAGETYPE =0;

        //System.out.println("Size is: "+size+". Starting now.");
        if (rank == 0) {
            startTime=System.currentTimeMillis();
            BufferedImage image = ImageIO.read(new File(imagePath));
            //kernel = new float[]{1, 0, -1, 2, 0, -2, 1, 0, -1};
            kernel= new float[]{0,-1,0,-1,5,-1,0,-1,0};
            //kernel = new float[]{1, 2, 1, 0, 0, 0, -1, -2, -1};
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Send(kernel, 0, kernel.length, MPI.FLOAT, i, 99);
            }

            WIDTH = image.getWidth();
            HEIGHT = image.getHeight();
            IMAGETYPE=image.getType();

            BufferedImage[] subimages = new BufferedImage[size];
            int chunk = WIDTH / size;

            for (int i = 0; i < size - 1; i++) {
                subimages[i] = image.getSubimage(i * chunk, 0, chunk, HEIGHT);  //nisam uradio zadnji chunk jer ima ostatak
            }
            subimages[size - 1] = image.getSubimage((size - 1) * chunk, 0, chunk, HEIGHT);
            image.flush();
            image=null;

            System.out.println("Preparing to send everyone their parts from node 0.");
            //startTime=System.currentTimeMillis();
            for (int i = 1; i < size; i++) {   //krecemo od 1 jer ce r0 raditi vec "kod kuce"
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                ImageIO.write(subimages[i], "JPG", bs);
                imageBytes = bs.toByteArray();
                bs.flush();
                bs.close();



                WidthAndHeight[0] = subimages[i].getWidth();
                WidthAndHeight[1] = subimages[i].getHeight();
                subimages[i].flush();
                subimages[i]=null;
                int imageSize = imageBytes.length;
                MPI.COMM_WORLD.Send(new int[]{imageSize}, 0, 1, MPI.INT, i, 88); //velicina arraya sa bajtovima

                MPI.COMM_WORLD.Send(imageBytes, 0, imageBytes.length, MPI.BYTE, i, 77);
                imageBytes=null;
                MPI.COMM_WORLD.Send(WidthAndHeight, 0, 2, MPI.INT, i, 10);


            }
            //System.out.println("Sent all parts from node 0.");
            //startTime=System.currentTimeMillis();
            inputIMG = subimages[0];
        }
        else {
            MPI.COMM_WORLD.Recv(kernel, 0, kernel.length, MPI.FLOAT, 0, 99);
            //System.out.println("Received kernel to node"+rank);
            int[] imageSizeArray = new int[1];
            MPI.COMM_WORLD.Recv(imageSizeArray, 0, 1, MPI.INT, 0, 88);
            int imageSize = imageSizeArray[0];

            imageBytes = new byte[imageSize];    //svaki put kad recv globalnu varijablu, koja je null, moras azurirati koliko ce mjesta uzeti!
            MPI.COMM_WORLD.Recv(imageBytes, 0, imageSize, MPI.BYTE, 0, 77);
            InputStream is = new ByteArrayInputStream(imageBytes);
            BufferedImage inputRaw = ImageIO.read(is);
            is.close();
            imageBytes = null;


            MPI.COMM_WORLD.Recv(WidthAndHeight, 0, 2, MPI.INT, 0, 10);

            inputIMG = new BufferedImage(inputRaw.getWidth(), inputRaw.getHeight(), BufferedImage.TYPE_INT_RGB);
            inputIMG.getGraphics().drawImage(inputRaw, 0, 0, null);
            //System.out.println("Received imageBytes, width and height of my part by "+rank);
        }

        int width = inputIMG.getWidth();
        int height = inputIMG.getHeight();
        int kernelLength = (int) sqrt(kernel.length);
        BufferedImage outputIMG = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        float[][] kernel1 = new float[kernelLength][kernelLength];
        for (int i = 0; i < kernelLength; i++) {
            for (int j = 0; j < kernelLength; j++) {
                kernel1[i][j] = kernel[i * kernelLength + j];
            }
        }

        for (int i = 0; i < width; i++) {            //everybody has to do this!
            for (int j = 0; j < height; j++) {
                float red = 0f;
                float green = 0f;
                float blue = 0f;

                for (int m = 0; m < kernelLength; m++) {
                    for (int n = 0; n < kernelLength; n++) {

                        int iC = (i - kernelLength / 2 + m + width) % width;
                        int jC = (j - kernelLength / 2 + n + height) % height;
                        int rgbTotal = inputIMG.getRGB(iC, jC);

                        int rgbRed = (rgbTotal >> 16) & 0xff;
                        int rgbGreen = (rgbTotal >> 8) & 0xff;
                        int rgbBlue = (rgbTotal) & 0xff;

                        red += (rgbRed * kernel1[m][n]);
                        green += (rgbGreen * kernel1[m][n]);
                        blue += (rgbBlue * kernel1[m][n]);
                    }
                }
                int redOutput = Math.min(Math.max((int) (red), 0), 255);
                int greenOutput = Math.min(Math.max((int) (green), 0), 255);
                int blueOutput = Math.min(Math.max((int) (blue), 0), 255);


                Color color = new Color(redOutput, greenOutput, blueOutput);
                outputIMG.setRGB(i, j, color.getRGB());
            }
        }
        //System.out.println("image is convoluted by node "+rank); //radi do ovdje sve kako treba

        if (rank != 0)   //transform image into byte array and send it to node 0
        {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageIO.write(outputIMG, "JPG", bs);
            byte[] imageBytes1 = bs.toByteArray();
            bs.flush();
            bs.close();
            int[] length = new int[]{imageBytes1.length};
            //System.out.println("Turned image into byted image by node "+rank);  //funkcionise

            //System.out.println("Node " + rank + " has byted image array length of " + imageBytes1.length);
            //sending byted images array
            MPI.COMM_WORLD.Send(length, 0, 1, MPI.INT, 0, 1); //velicina byte arraya
            //System.out.println("SUCCESSFULLY SENT BYTE ARRAY LENGTH BY " + rank);
            MPI.COMM_WORLD.Send(imageBytes1, 0, imageBytes1.length, MPI.BYTE, 0, 2);
            System.out.println("SUCCESSFULLY SENT convoluted byted image from " + rank);
            //imageBytes1=null;



            outputIMG.flush();
            outputIMG = null;

        }

        if (rank == 0) { //receive all byted parts, then put them in order in array, then transform all parts into JPG image parts, and join them.
            PartsConvoluted[0] = outputIMG;
            int[] length = new int[1];
            //System.out.println("Node 0 is ready to receive convoluted byted parts.");

            //MPI.COMM_WORLD.Recv(length, 0,1,MPI.INT,1,1); //potreban nam je length samo od jedno. Jer su prvi size-1 iste duzine

            BufferedImage outputFINAL = new BufferedImage(WIDTH, HEIGHT, IMAGETYPE);
            Graphics g = outputFINAL.getGraphics();



            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(length, 0, 1, MPI.INT, i, 1);
                int sizeOfarray = length[0];
                byte[] imageBytes1 = new byte[sizeOfarray];
                MPI.COMM_WORLD.Recv(imageBytes1, 0, sizeOfarray, MPI.BYTE, i, 2);
                //System.out.println("Primito je");

                InputStream is = new ByteArrayInputStream(imageBytes1);
                BufferedImage inputRaw = ImageIO.read(is);
                is.close();


                PartsConvoluted[i] = new BufferedImage(inputRaw.getWidth(), inputRaw.getHeight(), BufferedImage.TYPE_INT_RGB);
                PartsConvoluted[i].getGraphics().drawImage(inputRaw, 0, 0, null);
                inputRaw.flush();
                inputRaw = null;



            }

            int currentX = 0;

            for (int i = 0; i < PartsConvoluted.length; i++) {
                BufferedImage img = PartsConvoluted[i];
                g.drawImage(img, currentX, 0, null);
                currentX += img.getWidth(); // Move x position by the width of the current image
                //System.out.println("Done by " + i);
            }
            endTime=System.currentTimeMillis();
            System.out.println("Duration time is "+(endTime-startTime));

            g.dispose();

            File file = new File("output_image.jpg");
            ImageIO.write(outputFINAL, "jpg", file);
            outputIMG = null;



        }

        MPI.Finalize();



    }
}
