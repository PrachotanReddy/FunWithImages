
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	double zoomSpeed;
    double rotationSpeed;
    long startTime;
    int framesPerSecond;
	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void showIms(String[] args){

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		zoomSpeed = Double.parseDouble(args[1]);
        rotationSpeed = Double.parseDouble(args[2]);
        framesPerSecond = Integer.parseInt(args[3]);
    if (zoomSpeed>2.0||zoomSpeed<0.50||rotationSpeed>180.0||rotationSpeed<-180.0||framesPerSecond>30||framesPerSecond<1) {
      System.out.println("Invalid Input parameters, Try again");
      System.exit(-1);
    }
		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
    	startTime = System.currentTimeMillis();
		Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateFrame();
            }
        }, 0, 1000 / framesPerSecond);
	}

    private void updateFrame() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        double zoomFactor = 1+((zoomSpeed-1)* elapsedTime / 1000.0);
		if (zoomFactor<=0)
			System.exit(0);
  
        double rotationAngle = Math.toRadians(rotationSpeed * elapsedTime / 1000.0);
        int[] pixels = new int[width * height];
        int[] originalPixels = imgOne.getRGB(0, 0, width, height, null, 0, width);

        double centerX = (width) / 2.0;
        double centerY = (height) / 2.0;

        double cosTheta = Math.cos(rotationAngle);
        double sinTheta = Math.sin(rotationAngle);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double xOffset = x - centerX;
                double yOffset = y - centerY;

                double rotatedX = xOffset * cosTheta + yOffset * sinTheta;
                double rotatedY = -xOffset * sinTheta + yOffset * cosTheta;

                int sourceX = (int)Math.round(rotatedX / zoomFactor + centerX);
                int sourceY = (int)Math.round(rotatedY / zoomFactor + centerY);

                if (sourceX >= 0 && sourceX < width && sourceY >= 0 && sourceY < height) {
                  if(zoomSpeed>1)
                    pixels[y * width + x] = originalPixels[sourceY * width + sourceX];
                  else{
                    int Rsum = 0, Gsum = 0, Bsum = 0;
					// Applying a 3x3 average filter when zooming out
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int sampleX = sourceX + i;
                            int sampleY = sourceY + j;

                            if (sampleX >= 0 && sampleX < width && sampleY >= 0 && sampleY < height) {
                                int sampleColor = originalPixels[sampleY * width + sampleX];
                                Rsum += (sampleColor >> 16) & 0xFF;
                                Gsum += (sampleColor >> 8) & 0xFF;
                                Bsum += sampleColor & 0xFF;
                            }
                        }
                    }

                int avgR = Rsum / 9, avgG = Gsum / 9, avgB = Bsum / 9;

                pixels[y * width + x] = 0xff000000 | (avgR << 16) | (avgG << 8) | avgB;
                  }
                } else {
                    // Set areas outside original image to black
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        BufferedImage transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        transformedImage.setRGB(0, 0, width, height, pixels, 0, width);
        lbIm1.setIcon(new ImageIcon(transformedImage));
    }

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
