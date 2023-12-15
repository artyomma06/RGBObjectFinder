# Enhanced RGB Object Detection Program

This advanced program excels in detecting objects within RGB images. By leveraging sophisticated techniques, it accurately identifies and highlights the specified objects against a lime green background.

## Implementation Details

The process begins by taking an RGB image file, followed by the input of object images featuring a distinctive lime green (0, 255, 0) background to delineate the objects. The program harnesses the power of the HSV hue channel and YUV U and V channels, employing histogram comparison techniques to assess the likelihood of object presence. Subsequently, the number of islands cluster algorithm is applied to identify all potential occurrences of the target object. For each cluster, a meticulous histogram comparison is executed, culminating in the drawing of a conspicuous red box on the image for enhanced visibility.

## Prerequisites

Before diving into the project, ensure that you have the following:

- Java

## Usage Guidelines

Follow these steps to set up and run the project:

1. **Clone the Repository:**
   - Clone this repository to your local machine.

2. **Compile the Code:**
   - Execute the following command to compile the Java code:
        - javac ImageDisplay.java


3. **Run the Program:**
   - Launch the program with the following command, specifying the RGB files for the main image and object images:
        - java ImageDisplay image.rgb object1.rgb object2.rgb
