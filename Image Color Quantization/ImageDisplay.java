import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Arrays;

import javax.swing.*;
//Uncomment line 596 and recompile to run the script against this code, else images keep populating, you don't have to do this if you're using the terminal manually.

public class ImageDisplay {

    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne, imgTwo;
    double error = 0;

    int width = 512;
    int height = 512;

    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage quantizeUniform(BufferedImage img, int numBuckets) {
        BufferedImage imgQuantized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int nBuckets = (int) Math.cbrt(numBuckets);
        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        // Building the histogram for each channel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                histogramR[red]++;
                histogramG[green]++;
                histogramB[blue]++;
            }
        }
        // establishing the count of non-zero occurences of values
        int nonZeroCountR = 0;
        for (int value : histogramR) {
            if (value > 0) {
                nonZeroCountR++;
            }
        }
        int nonZeroCountG = 0;
        for (int value : histogramG) {
            if (value > 0) {
                nonZeroCountG++;
            }
        }
        int nonZeroCountB = 0;
        for (int value : histogramB) {
            if (value > 0) {
                nonZeroCountB++;
            }
        }
        // System.out.println("non-zero red values: " + nonZeroCountR+" green values:
        // "+nonZeroCountG+" blue values: "+nonZeroCountB);

        double bucketSize = 256.0 / nBuckets;
        double[] bucketRanges = new double[nBuckets + 1];
        for (int i = 0; i < nBuckets + 1; i++) {
            bucketRanges[i] = Math.min(i * bucketSize, 255);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = img.getRGB(x, y) >> 16 & 0xff;
                int green = img.getRGB(x, y) >> 8 & 0xff;
                int blue = img.getRGB(x, y) & 0xff;
                int red1, green1, blue1;
                // finding the bucket and the quantized color
                if (nonZeroCountR <= nBuckets) {// checking if number of non-zero values is less than alloted number of
                                                // buckets
                    red1 = red;
                } else {
                    int bucketR = 0;
                    while (red > bucketRanges[++bucketR])
                        ;
                    red1 = ((int) Math.round((bucketRanges[bucketR] + bucketRanges[bucketR - 1]) / 2.0));
                }
                if (nonZeroCountG <= nBuckets) {// checking if number of non-zero values is less than alloted number of
                                                // buckets
                    green1 = green;
                } else {
                    int bucketG = 0;
                    while (green > bucketRanges[++bucketG])
                        ;
                    green1 = ((int) Math.round((bucketRanges[bucketG] + bucketRanges[bucketG - 1]) / 2.0));
                }
                if (nonZeroCountB <= nBuckets) {// checking if number of non-zero values is less than alloted number of
                                                // buckets
                    blue1 = blue;
                } else {
                    int bucketB = 0;
                    while (blue > bucketRanges[++bucketB])
                        ;
                    blue1 = (int) Math.round((bucketRanges[bucketB] + bucketRanges[bucketB - 1]) / 2.0);
                }

                int quantizedColor = (0xff << 24) |
                        red1 << 16 |
                        green1 << 8 |
                        blue1;

                imgQuantized.setRGB(x, y, quantizedColor);
            }
        }
        return imgQuantized;
    }

    private BufferedImage quantizeNonUniform(BufferedImage img, int numBuckets) {
        BufferedImage imgQuantized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        // Building the histogram for each channel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                histogramR[red]++;
                histogramG[green]++;
                histogramB[blue]++;
            }
        }
        numBuckets = (int) Math.cbrt(numBuckets);
        // establishing the count of non-zero occurences of values
        int nonZeroCountR = 0;
        for (int value : histogramR) {
            if (value > 0) {
                nonZeroCountR++;
            }
        }
        int nonZeroCountG = 0;
        for (int value : histogramG) {
            if (value > 0) {
                nonZeroCountG++;
            }
        }
        int nonZeroCountB = 0;
        for (int value : histogramB) {
            if (value > 0) {
                nonZeroCountB++;
            }
        }

        int[] bucketRangesR = calculateBucketRanges(histogramR, numBuckets);
        int[] bucketRangesG = calculateBucketRanges(histogramG, numBuckets);
        int[] bucketRangesB = calculateBucketRanges(histogramB, numBuckets);

        // finding the bucket and the quantized color
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int red1, green1, blue1;
                if (nonZeroCountR <= numBuckets) {// checking if number of non-zero values is less than alloted number
                                                  // of buckets
                    red1 = red;
                } else {
                    int bucketR = findBucket(red, bucketRangesR);
                    red1 = findmean(histogramR, bucketRangesR[bucketR - 1] + 1, bucketRangesR[bucketR]);
                }
                if (nonZeroCountG <= numBuckets) {// checking if number of non-zero values is less than alloted number
                                                  // of buckets
                    green1 = green;
                } else {
                    int bucketG = findBucket(green, bucketRangesG);
                    green1 = findmean(histogramG, bucketRangesG[bucketG - 1] + 1, bucketRangesG[bucketG]);
                }
                if (nonZeroCountB <= numBuckets) {// checking if number of non-zero values is less than alloted number
                                                  // of buckets
                    blue1 = blue;
                } else {
                    int bucketB = findBucket(blue, bucketRangesB);
                    blue1 = findmean(histogramB, bucketRangesB[bucketB - 1] + 1, bucketRangesB[bucketB]);
                }

                int quantizedColor = (0xff << 24) |
                        (red1 << 16) |
                        (green1 << 8) |
                        blue1;

                imgQuantized.setRGB(x, y, quantizedColor);
            }
        }
        int pixelValue1, pixelValue2;
        int red1 = 0, green1 = 0, blue1 = 0, red2 = 0, green2 = 0, blue2 = 0;
        double error1 = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelValue1 = img.getRGB(x, y);
                red1 = ((pixelValue1 >> 16) & 0xff);
                green1 = ((pixelValue1 >> 8) & 0xff);
                blue1 = (pixelValue1 & 0xff);
                pixelValue2 = imgQuantized.getRGB(x, y);
                red2 = ((pixelValue2 >> 16) & 0xff);
                green2 = ((pixelValue2 >> 8) & 0xff);
                blue2 = (pixelValue2 & 0xff);
                error1 += Math.abs(red2 - red1) + Math.abs(green2 - green1) + Math.abs(blue2 - blue1);
            }
        }
        if (error1 != 0) {
            BufferedImage imgThree = quantizeUniform(img, numBuckets * numBuckets * numBuckets);
            double error2 = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelValue1 = img.getRGB(x, y);
                    red1 = ((pixelValue1 >> 16) & 0xff);
                    green1 = ((pixelValue1 >> 8) & 0xff);
                    blue1 = (pixelValue1 & 0xff);
                    pixelValue2 = imgThree.getRGB(x, y);
                    red2 = ((pixelValue2 >> 16) & 0xff);
                    green2 = ((pixelValue2 >> 8) & 0xff);
                    blue2 = (pixelValue2 & 0xff);
                    error2 += Math.abs(red2 - red1) + Math.abs(green2 - green1) + Math.abs(blue2 - blue1);
                }
            }
            if (error2 < error1) {
                imgQuantized = imgThree;
            }
        }
        return imgQuantized;
    }

    private int findmean(int[] histogram, int i, int j) {
        int sum = 0;
        int sum1 = 0;
        for (int index = i; index <= j; index++) {
            sum += index * histogram[index];
            sum1 += histogram[index];
        }

        // Returning the weighted average
        return (int) Math.round(sum / (double) sum1);
    }

    private int[] calculateCumulative(int[] histogram) {
        int[] cumu = new int[histogram.length];
        cumu[0] = histogram[0];
        // building the cumulative from the histogram
        for (int i = 1; i < histogram.length; i++) {
            cumu[i] = cumu[i - 1] + histogram[i];
        }

        return cumu;
    }

    private int[] calculateBucketRanges(int[] histogram, int totalBuckets) {
        int[] cdf = calculateCumulative(histogram);

        int[] bucketRanges = new int[totalBuckets + 1];
        int start = 0;
        while (cdf[start] == 0) {
            start++;
        }
        bucketRanges[0] = start;
        int end = 255;
        while (histogram[end] == 0) {
            end--;
        }
        bucketRanges[totalBuckets] = end;
        int pixelsPerBucket = width * height / (2 * totalBuckets);// establishing the starting threshold
        int totpixels = 0;
        for (int i = 1; i < totalBuckets; i++) {
            int targetValue = pixelsPerBucket;
            int j = 0;
            while (cdf[j] - totpixels < targetValue) {
                j++;
            }
            bucketRanges[i] = j;
            totpixels = cdf[j];
            pixelsPerBucket = ((width * height) - totpixels) / (totalBuckets - i);// updating the threshold based on
                                                                                  // remaining pixels and available
                                                                                  // buckets
        }
        for (int i = totalBuckets; i > 1; i--) {
            int j = i;
            while (j >= 1 && (bucketRanges[i] == bucketRanges[j - 1])) {
                bucketRanges[j - 1]--;// reiterating to distribute the spread, since buckets seem to accumulate at the
                                      // end
                j--;
            }
        }
        bucketRanges[totalBuckets] = end;
        // for (int i : bucketRanges) {
        // System.out.print(i+",");
        // }
        // System.out.print("\n");
        return bucketRanges;
    }

    private int findBucket(int value, int[] bucketRanges) {
        int bucketIndex = 0;

        while (value > bucketRanges[++bucketIndex])
            ;

        return bucketIndex;
    }

    private long calculateHistogramSum(BufferedImage img, int[] histogram, int channelIndex) {// returns weighted sum of
                                                                                              // channel
        long sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = (img.getRGB(x, y) >> ((2 - channelIndex) * 8)) & 0xFF;
                sum += color * histogram[color];
            }
        }
        return sum;
    }

    private int[] calculateNumBucketsPerChannel(BufferedImage img, int totalBuckets) {
        int[] numBucketsPerChannel = new int[3];
        int nBuckets = 3 * (int) Math.cbrt(totalBuckets);

        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                histogramR[red]++;
                histogramG[green]++;
                histogramB[blue]++;
            }
        }
        long sumR = calculateHistogramSum(img, histogramR, 0);
        // System.out.println(sumR);
        long sumG = calculateHistogramSum(img, histogramG, 1);
        // System.out.println(sumG);
        long sumB = calculateHistogramSum(img, histogramB, 2);
        // System.out.println(sumB);
        // Calculating the number of buckets per channel based on their contribution to
        // the overall image
        for (int i = 0; i < 3; i++) {
            double weight = 0.0;
            switch (i) {
                case 0:
                    weight = sumR / (double) (sumR + sumG + sumB);
                    break;
                case 1:
                    weight = sumG / (double) (sumR + sumG + sumB);
                    break;
                case 2:
                    weight = sumB / (double) (sumR + sumG + sumB);
                    break;
            }
            numBucketsPerChannel[i] = (int) Math.round(weight * nBuckets);
        }
        int diff = totalBuckets - (numBucketsPerChannel[0] * numBucketsPerChannel[1] * numBucketsPerChannel[2]);
        while (diff > 0) {// checking if more buckets can be allocated
            numBucketsPerChannel[0]++;
            int diff1 = totalBuckets - (numBucketsPerChannel[0] * numBucketsPerChannel[1] * numBucketsPerChannel[2]);
            if (diff1 < 0) {
                numBucketsPerChannel[0]--;
            }
            numBucketsPerChannel[1]++;
            int diff2 = totalBuckets - (numBucketsPerChannel[0] * numBucketsPerChannel[1] * numBucketsPerChannel[2]);
            if (diff2 < 0) {
                numBucketsPerChannel[1]--;
            }
            numBucketsPerChannel[2]++;
            int diff3 = totalBuckets - (numBucketsPerChannel[0] * numBucketsPerChannel[1] * numBucketsPerChannel[2]);
            if (diff3 < 0) {
                numBucketsPerChannel[2]--;
            }
            // System.out.println(numBucketsPerChannel[0]+" "+numBucketsPerChannel[1]+"
            // "+numBucketsPerChannel[2]);
            if (diff1 < 0 && diff2 < 0 && diff3 < 0) {
                break;
            }
        }

        return numBucketsPerChannel;
    }

    private BufferedImage quantize3(BufferedImage img, int totalBuckets) {
        BufferedImage imgQuantized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                histogramR[red]++;
                histogramG[green]++;
                histogramB[blue]++;
            }
        }
        int nonZeroCountR = 0;
        for (int value : histogramR) {
            if (value > 0) {
                nonZeroCountR++;
            }
        }
        int nonZeroCountG = 0;
        for (int value : histogramG) {
            if (value > 0) {
                nonZeroCountG++;
            }
        }
        int nonZeroCountB = 0;
        for (int value : histogramB) {
            if (value > 0) {
                nonZeroCountB++;
            }
        }
        int[] numBuckets = calculateNumBucketsPerChannel(img, totalBuckets);

        int[] bucketRangesR = calculateBucketRanges(histogramR, numBuckets[0]);
        int[] bucketRangesG = calculateBucketRanges(histogramG, numBuckets[1]);
        int[] bucketRangesB = calculateBucketRanges(histogramB, numBuckets[2]);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int red1, green1, blue1;

                if (nonZeroCountR <= numBuckets[0]) {
                    red1 = red;
                } else {
                    int bucketR = findBucket(red, bucketRangesR);
                    if (bucketRangesR[bucketR] == red) {
                        red1 = red;
                    } else
                        red1 = findmean(histogramR, bucketRangesR[bucketR - 1], bucketRangesR[bucketR]);
                }
                if (nonZeroCountG <= numBuckets[1]) {
                    green1 = green;
                } else {
                    int bucketG = findBucket(green, bucketRangesG);
                    if (bucketRangesG[bucketG] == green) {
                        green1 = green;
                    } else
                        green1 = findmean(histogramG, bucketRangesG[bucketG - 1], bucketRangesG[bucketG]);
                }
                if (nonZeroCountB <= numBuckets[2]) {
                    blue1 = blue;
                } else {
                    int bucketB = findBucket(blue, bucketRangesB);
                    if (bucketRangesB[bucketB] == blue) {
                        blue1 = blue;
                    } else
                        blue1 = findmean(histogramB, bucketRangesB[bucketB - 1], bucketRangesB[bucketB]);
                }

                int quantizedColor = (0xff << 24) |
                        (red1 << 16) |
                        (green1 << 8) |
                        blue1;

                imgQuantized.setRGB(x, y, quantizedColor);
            }
        }
        int pixelValue1, pixelValue2;
        int red1 = 0, green1 = 0, blue1 = 0, red2 = 0, green2 = 0, blue2 = 0;
        double error1 = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelValue1 = img.getRGB(x, y);
                red1 = ((pixelValue1 >> 16) & 0xff);
                green1 = ((pixelValue1 >> 8) & 0xff);
                blue1 = (pixelValue1 & 0xff);
                pixelValue2 = imgQuantized.getRGB(x, y);
                red2 = ((pixelValue2 >> 16) & 0xff);
                green2 = ((pixelValue2 >> 8) & 0xff);
                blue2 = (pixelValue2 & 0xff);
                error1 += Math.abs(red2 - red1) + Math.abs(green2 - green1) + Math.abs(blue2 - blue1);
            }
        }

        // System.out.println((int)error1);
        if (error1 != 0) {
            BufferedImage imgThree = quantizeNonUniform(img, totalBuckets);
            double error2 = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelValue1 = img.getRGB(x, y);
                    red1 = ((pixelValue1 >> 16) & 0xff);
                    green1 = ((pixelValue1 >> 8) & 0xff);
                    blue1 = (pixelValue1 & 0xff);
                    pixelValue2 = imgThree.getRGB(x, y);
                    red2 = ((pixelValue2 >> 16) & 0xff);
                    green2 = ((pixelValue2 >> 8) & 0xff);
                    blue2 = (pixelValue2 & 0xff);
                    error2 += Math.abs(red2 - red1) + Math.abs(green2 - green1) + Math.abs(blue2 - blue1);
                }
            }
            if (error2 < error1) {
                imgQuantized = imgThree;
            }
        }

        return imgQuantized;
    }

    public void showIms(String[] args) {
        // Read in the specified image
        imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[0], imgOne);

        int quantizationMode = Integer.parseInt(args[1]);
        int totalBuckets = Integer.parseInt(args[2]);

        switch (quantizationMode) {
            case 1:
                imgTwo = quantizeUniform(imgOne, totalBuckets);
                break;
            case 2:
                imgTwo = quantizeNonUniform(imgOne, totalBuckets);
                break;
            case 3:
                imgTwo = quantize3(imgOne, totalBuckets);
                break;
            default:
                System.out.println("Pick a valid quantization mode. Choose 1, 2, or 3.");
                return;
        }
        int pixelValue1, pixelValue2;
        int red1 = 0, green1 = 0, blue1 = 0, red2 = 0, green2 = 0, blue2 = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelValue1 = imgOne.getRGB(x, y);
                red1 = ((pixelValue1 >> 16) & 0xff);
                green1 = ((pixelValue1 >> 8) & 0xff);
                blue1 = (pixelValue1 & 0xff);
                pixelValue2 = imgTwo.getRGB(x, y);
                red2 = ((pixelValue2 >> 16) & 0xff);
                green2 = ((pixelValue2 >> 8) & 0xff);
                blue2 = (pixelValue2 & 0xff);
                error += Math.abs(red2 - red1) + Math.abs(green2 - green1) + Math.abs(blue2 - blue1);
            }
        }
        System.out.println("Error is:" + (int) error);
        // Uncomment line 596 to run the script against this code, else images keep
        // populating
        //System.exit(0);

        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        lbIm1 = new JLabel(new ImageIcon(imgTwo));

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
    }

    public static void main(String[] args) {
        ImageDisplay ren = new ImageDisplay();
        ren.showIms(args);
    }
}
