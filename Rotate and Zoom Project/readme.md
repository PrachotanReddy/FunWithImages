# Image Transformation

## Overview

This Java program, **ImageDisplay**, is designed to read a .rgb image, apply zoom and rotation transformations based on user-defined parameters, and display the transformed image in real time. The user can specify the image file path, zoom speed, rotation speed, and frames per second (fps) as command-line arguments.

## Usage

To use the program, compile the Java file and run the compiled class with the following command:

```bash
javac ImageDisplay.java
java ImageDisplay [image_path] [zoom_speed] [rotation_speed] [fps]
```

Replace `[image_path]`, `[zoom_speed]`, `[rotation_speed]`, and `[fps]` with the appropriate values. The program validates the input parameters to ensure they fall within valid ranges. If the parameters are invalid, an error message is displayed, and the program exits.

## Transformation Logic

The `updateFrame()` method applies zoom and rotation transformations to the image in real time. It uses a Timer to update the frame at the specified frames per second. The transformations are calculated based on elapsed time, zoom speed, and rotation speed.

### Zoom Transformation

The zoom transformation is implemented using the formula:

```java
double zoomFactor = 1 + ((zoomSpeed - 1) * elapsedTime / 1000.0);
```

This formula adjusts the image size based on the zoom speed and elapsed time. It calculates the zoom factor, which is a multiplier for the image size.

### Finding Corresponding Pixels in the Original Image

The rotation transformation is achieved by calculating the rotated coordinates for each pixel:

```java
double rotationAngle = Math.toRadians(rotationSpeed * elapsedTime / 1000.0);
double xOffset = x - centerX;
double yOffset = y - centerY;

double cosTheta = Math.cos(rotationAngle);
double sinTheta = Math.sin(rotationAngle);

double rotatedX = xOffset * cosTheta + yOffset * sinTheta;
double rotatedY = -xOffset * sinTheta + yOffset * cosTheta;
```

Here:

- `rotationAngle`: The angle of rotation converted from degrees to radians.
- `xOffset` and `yOffset`: The differences between the pixel coordinates and the center of the image.
- `cosTheta` and `sinTheta`: Precomputed trigonometric values for efficiency.
- `rotatedX` and `rotatedY`: The rotated coordinates after applying the rotation transformation.

The transformed coordinates (`rotatedX` and `rotatedY`) are used to find the corresponding pixel coordinates in the original image:

```java
int sourceX = (int)Math.round(rotatedX / zoomFactor + centerX);
int sourceY = (int)Math.round(rotatedY / zoomFactor + centerY);
```

- `zoomFactor`: The factor by which the image is zoomed.
- `centerX` and `centerY`: The center coordinates of the image.

The rounded values of `rotatedX` and `rotatedY` are adjusted based on the zoom factor and the center of the image. These adjusted and rounded coordinates (`sourceX` and `sourceY`) represent the pixel coordinates in the original image that correspond to the current pixel in the transformed image. They are used to fetch the color information from the original image and apply it to the transformed image.

In summary, this part of the code is crucial for mapping the transformed coordinates back to the original image, ensuring that the visual content is correctly displayed after applying the zoom and rotation transformations.

If the zoom speed is less than 1 (zooming out), a simple averaging filter (3x3 neighborhood) is applied to smooth the image.

```java
int sumR = 0, sumG = 0, sumB = 0;
for (int i = -1; i <= 1; i++) {
    for (int j = -1; j <= 1; j++) {
        int sampleX = sourceX + i;
        int sampleY = sourceY + j;

        if (sampleX >= 0 && sampleX < width && sampleY >= 0 && sampleY < height) {
            int sampleColor = originalPixels[sampleY * width + sampleX];
            sumR += (sampleColor >> 16) & 0xFF;
            sumG += (sampleColor >> 8) & 0xFF;
            sumB += sampleColor & 0xFF;
        }
    }
}
int avgR = sumR / 9;
int avgG = sumG / 9;
int avgB = sumB / 9;
```

Feel free to experiment with different images and parameter values to observe various transformations.

**Author: Prachotan Bathi Date: 02/12/2024**