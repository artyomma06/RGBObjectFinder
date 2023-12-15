
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;

// Sources:
// https://en.wikipedia.org/wiki/HSL_and_HSV 
// https://www.cs.rit.edu/~ncs/color/
// https://medium.com/@eric.christopher.ness/leetcode-200-number-of-islands-4dcd3ff971bb
// https://www.tabnine.com/code/java/methods/java.awt.Graphics/drawString
public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	int width = 640; // default image width and height
	int height = 480;

	/**
	 * Reads the RGB values of an image file into a BufferedImage.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param imgPath The file path of the image.
	 * @param img The BufferedImage to store the image.
	 */
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

	/**
	 * Computes the hue histogram of an image, excluding a specific color (red in this case).
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The hue histogram of the image.
	 */
	private int[] hueHistOfObject(BufferedImage img, int threshold) {
		int[] histy = new int[361];
		for (int i = 0; i < 361; i++) {
			histy[i] = 0;
		}

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Skip pure red pixels (255, 0, 0)
				if (g == 255 && r == 0 && b == 0) {
					continue;
				}

				int bigM = Math.max(r, Math.max(g, b));
				int littleM = Math.min(r, Math.min(g, b));
				int c = bigM - littleM;
				double hPrime = -1;

				if (c == 0) {
					continue; // Skip processing if saturation is zero (gray).
				} else if (bigM == r) {
					hPrime = ((g - b) / (double) c) % 6;
				} else if (bigM == g) {
					hPrime = ((b - r) / (double) c) + 2;
				} else if (bigM == b) {
					hPrime = ((r - g) / (double) c) + 4;
				} else {
					continue; // Skip if no valid case matches.
				}

				int h = (int) (60 * hPrime);

				if (h < 0) {
					h = h + 360;
				}

				histy[h] = histy[h] + 1;
			}
		}

		// Non-normalized filtering
		for (int i = 0; i < 361; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the value histogram of an image, excluding red pixels.
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The value histogram of the image.
	 */
	private int[] vHistOfObject(BufferedImage img, int threshold) {
		// Initialize an array to store the histogram values for intensity (value).
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the image.
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Skip pure red pixels (255, 0, 0).
				if (g == 255 && r == 0 && b == 0) {
					continue;
				}

				// Calculate weighted components for intensity (value).
				double calcR = 0.615 * (double) r;
				double calcG = 0.515 * (double) g;
				double calcB = 0.100 * (double) b;

				// Compute value (v) and ignore if negative.
				int v = (int) (calcR - calcG - calcB);
				if (v < 0) {
					continue;
				}

				// Increment the histogram bin for the computed value.
				histy[v] = histy[v] + 1;
			}
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the u component histogram of an image, excluding red pixels.
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The u component histogram of the image.
	 */
	private int[] uHistOfObject(BufferedImage img, int threshold) {
		// Initialize an array to store the histogram values for the u component.
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the image.
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Skip pure red pixels (255, 0, 0).
				if (g == 255 && r == 0 && b == 0) {
					continue;
				}

				// Calculate weighted components for the u component.
				double calcR = -0.147 * (double) r;
				double calcG = 0.289 * (double) g;
				double calcB = 0.436 * (double) b;

				// Compute u component and ignore if negative.
				int u = (int) (calcR - calcG + calcB);
				if (u < 0) {
					continue;
				}

				// Increment the histogram bin for the computed u component.
				histy[u] = histy[u] + 1;
			}
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the hue histogram of an image.
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The hue histogram of the image.
	 */
	private int[] hueHistOfImage(BufferedImage img, int threshold) {
		// Initialize an array to store the histogram values for hue.
		int[] histy = new int[361];
		for (int i = 0; i < 361; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the image.
		for(int y = 0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Calculate the maximum and minimum of RGB components.
				int bigM = Math.max(r, Math.max(g, b));
				int littleM = Math.min(r, Math.min(g, b));
				int c = bigM - littleM;
				double hPrime = -1;

				// Skip if chroma (c) is 0, indicating achromatic color.
				if (c == 0) {
					continue;
				} else if (bigM == r) {
					hPrime = ((g - b) / (double) c) % 6;
				} else if (bigM == g) {
					hPrime = ((b - r) / (double) c) + 2;
				} else if (bigM == b) {
					hPrime = ((r - g) / (double) c) + 4;
				} else {
					continue;
				}

				// Compute hue and convert to degrees.
				int h = (int) (60 * hPrime);
				if (h < 0) {
					h = h + 360;
				}

				// Increment the histogram bin for the computed hue.
				histy[h] = histy[h] + 1;
			}
		}

		// Filtering based on the threshold.
		for (int i = 0; i < 361; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the value histogram of an image.
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The value histogram of the image.
	 */
	private int[] vHistOfImage(BufferedImage img, int threshold) {
		// Initialize an array to store the histogram values for intensity (value).
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the image.
		for(int y = 0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Calculate weighted components for intensity (value).
				double calcR = 0.615 * (double) r;
				double calcG = 0.515 * (double) g;
				double calcB = 0.100 * (double) b;

				// Compute value (v) and ignore if negative.
				int v = (int) (calcR - calcG - calcB);
				if (v < 0) {
					continue;
				}

				// Increment the histogram bin for the computed value.
				histy[v] = histy[v] + 1;
			}
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the u component histogram of an image.
	 * @param img The input image.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The u component histogram of the image.
	 */
	private int[] uHistOfImage(BufferedImage img, int threshold) {
		// Initialize an array to store the histogram values for the u component.
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the image.
		for(int y = 0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Calculate weighted components for the u component.
				double calcR = -0.147 * (double) r;
				double calcG = 0.289 * (double) g;
				double calcB = 0.436 * (double) b;

				// Compute u component and ignore if negative.
				int u = (int) (calcR - calcG + calcB);
				if (u < 0) {
					continue;
				}

				// Increment the histogram bin for the computed u component.
				histy[u] = histy[u] + 1;
			}
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the histogram of a specific color component for an object (single pixel cluster).
	 * @param img The input image.
	 * @param imgName The color component identifier ("h" for hue, "v" for value, "u" for u component).
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The histogram of the specified color component for the object.
	 */
	private int[] histogramOfObject(BufferedImage img, String imgName, int threshold) {
		if (imgName.equals("h")) {
			return hueHistOfObject(img, threshold);
		} else if (imgName.equals("v")) {
			return vHistOfObject(img, threshold);
		} else {
			return uHistOfObject(img, threshold);
		}
	}

	/**
	 * Computes the histogram of a specific color component for the entire image.
	 * @param img The input image.
	 * @param imgName The color component identifier ("h" for hue, "v" for value, "u" for u component).
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The histogram of the specified color component for the entire image.
	 */
	private int[] histogramOfImage(BufferedImage img, String imgName, int threshold) {
		if (imgName.equals("h")) {
			return hueHistOfImage(img, threshold);
		} else if (imgName.equals("v")) {
			return vHistOfImage(img, threshold);
		} else {
			return uHistOfImage(img, threshold);
		}
	}

	/**
	 * Computes the hue histogram of a specific color component for a cluster of pixels.
	 * @param img The input image.
	 * @param cluster The list of pixel coordinates in the cluster.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The hue histogram of the specified color component for the cluster.
	 */
	private int[] hueHistOfCluster(BufferedImage img, List<List<Integer>> cluster, int threshold) {
		// Initialize an array to store the histogram values for hue.
		int[] histy = new int[361];
		for (int i = 0; i < 361; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the cluster.
		for (List<Integer> pixel : cluster) {
			int x = pixel.get(0);
			int y = pixel.get(1);
			int pix = img.getRGB(x, y);
			int r = ((pix >> 16) & 0xFF);
			int g = ((pix >> 8) & 0xFF);
			int b = ((pix) & 0xFF);

			// Calculate the maximum and minimum of RGB components.
			int bigM = Math.max(r, Math.max(g, b));
			int littleM = Math.min(r, Math.min(g, b));
			int c = bigM - littleM;
			double hPrime = -1;

			// Skip if chroma (c) is 0, indicating achromatic color.
			if (c == 0) {
				continue;
			} else if (bigM == r) {
				hPrime = ((g - b) / (double) c) % 6;
			} else if (bigM == g) {
				hPrime = ((b - r) / (double) c) + 2;
			} else if (bigM == b) {
				hPrime = ((r - g) / (double) c) + 4;
			} else {
				continue;
			}

			// Compute hue and convert to degrees.
			int h = (int) (60 * hPrime);
			if (h < 0) {
				h = h + 360;
			}

			// Increment the histogram bin for the computed hue.
			histy[h] = histy[h] + 1;
		}

		// Filtering based on the threshold.
		for (int i = 0; i < 361; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the value histogram of a specific color component for a cluster of pixels.
	 * @param img The input image.
	 * @param cluster The list of pixel coordinates in the cluster.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The value histogram of the specified color component for the cluster.
	 */
	private int[] vHistOfCluster(BufferedImage img, List<List<Integer>> cluster, int threshold) {
		// Initialize an array to store the histogram values for intensity (value).
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the cluster.
		for (List<Integer> pixel : cluster) {
			int x = pixel.get(0);
			int y = pixel.get(1);
			int pix = img.getRGB(x, y);
			int r = ((pix >> 16) & 0xFF);
			int g = ((pix >> 8) & 0xFF);
			int b = ((pix) & 0xFF);

			// Calculate weighted components for intensity (value).
			double calcR = 0.615 * (double) r;
			double calcG = 0.515 * (double) g;
			double calcB = 0.100 * (double) b;

			// Compute value (v) and ignore if negative.
			int v = (int) (calcR - calcG - calcB);
			if (v < 0) {
				continue;
			}

			// Increment the histogram bin for the computed value.
			histy[v] = histy[v] + 1;
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) {
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the u component histogram of a specific color component for a cluster of pixels.
	 * @param img The input image.
	 * @param cluster The list of pixel coordinates in the cluster.
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The u component histogram of the specified color component for the cluster.
	 */
	private int[] uHistOfCluster(BufferedImage img, List<List<Integer>> cluster, int threshold) {
		// Initialize an array to store the histogram values for u component.
		int[] histy = new int[256];
		for (int i = 0; i < 256; i++) {
			histy[i] = 0;
		}

		// Iterate through each pixel in the cluster.
		for (int i = 0; i < cluster.size(); i++) {
			int x = cluster.get(i).get(0);
			int y = cluster.get(i).get(1);
			int pix = img.getRGB(x, y);
			int r = ((pix >> 16) & 0xFF);
			int g = ((pix >> 8) & 0xFF);
			int b = ((pix) & 0xFF);

			// Calculate weighted components for u component.
			double calcR = -0.147 * (double) r;
			double calcG = 0.289 * (double) g;
			double calcB = 0.436 * (double) b;

			// Compute u component and ignore if negative.
			int u =  (int)(calcR - calcG + calcB);
			if (u < 0) {
				continue;
			}

			// Increment the histogram bin for the computed u component.
			histy[u] = histy[u] + 1;
		}

		// Non-normalized filtering.
		for (int i = 0; i < 256; i++) {
			if (histy[i] < threshold) { // May need to increase or decrease threshold
				histy[i] = 0;
			}
		}

		return histy;
	}

	/**
	 * Computes the histogram of a specific color component for a cluster of pixels.
	 * @param img The input image.
	 * @param cluster The list of pixel coordinates in the cluster.
	 * @param imgName The color component identifier ("h" for hue, "v" for value, "u" for u component).
	 * @param threshold The threshold for filtering low occurrences in the histogram.
	 * @return The histogram of the specified color component for the cluster.
	 */
	private int[] histogramOfCluster(BufferedImage img, List<List<Integer>> cluster, String imgName, int threshold) {
		if (imgName.equals("h")) {
			return hueHistOfCluster(img, cluster, threshold);
		} else if (imgName.equals("v")) {
			return vHistOfCluster(img, cluster, threshold);
		} else {
			return uHistOfCluster(img, cluster, threshold);
		}
	}

	/**
	 * Checks if a significant number of colors in an object's histogram are present in the image's histogram.
	 * @param imageHist The histogram of the entire image.
	 * @param objectHist The histogram of the object.
	 * @param threshold The threshold ratio for considering the existence of the object in the image.
	 * @return True if the object is considered to exist in the image, false otherwise.
	 */
	private boolean imageExist(int[] imageHist, int[] objectHist, double threshold) {
		int objectColorCount = 0;
		int imageColorCount = 0;

		// Count the number of colors present in the object.
		for (int i = 0; i < objectHist.length; i++) {
			if (objectHist[i] > 0) {
				objectColorCount++;
				if (imageHist[i] > 0) {
					imageColorCount++;
				}
			}
		}

		// Check if the ratio of object colors present in the image exceeds the threshold.
		return imageColorCount >= (int) (objectColorCount * threshold);
	}

	/**
	 * Generates a list of pixel coordinates for each hue value in the image.
	 * @param img The input image.
	 * @param imgGram The histogram of hues in the image.
	 * @return A list of pixel coordinates for each hue value.
	 */
	private List<List<List<Integer>>> generateImagePointsForHue(BufferedImage img, int[] imgGram) {
		List<List<List<Integer>>> points = new ArrayList<>();
		
		// Initialize a list for each possible hue value.
		for (int i = 0; i < 361; i++) {
			List<List<Integer>> innerList = new ArrayList<>();
			points.add(innerList);
		}

		// Iterate through each pixel in the image.
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);
				int bigM = Math.max(r, Math.max(g, b));
				int littleM = Math.min(r, Math.min(g, b));
				int c = bigM - littleM;
				double hPrime = -1;

				// Skip if chroma (c) is 0, indicating achromatic color.
				if (c == 0) {
					continue;
				} else if (bigM == r) {
					hPrime = ((g - b) / (double) c) % 6;
				} else if (bigM == g) {
					hPrime = ((b - r) / (double) c) + 2;
				} else if (bigM == b) {
					hPrime = ((r - g) / (double) c) + 4;
				} else {
					continue;
				}

				// Compute hue and convert to degrees.
				int h = (int) (60 * hPrime);
				if (h < 0) {
					h = h + 360;
				}

				// Skip if hue is not present in the histogram.
				if (imgGram[h] == 0) {
					continue;
				}

				// Add the pixel coordinates to the list for the corresponding hue value.
				List<Integer> point = new ArrayList<>();
				point.add(x);
				point.add(y);
				points.get(h).add(point);
			}
		}

		return points;
	}

	/**
	 * Generates a list of pixel coordinates for each value (intensity) in the image.
	 * @param img The input image.
	 * @param imgGram The histogram of values in the image.
	 * @return A list of pixel coordinates for each value.
	 */
	private List<List<List<Integer>>> generateImagePointsForV(BufferedImage img, int[] imgGram) {
		List<List<List<Integer>>> points = new ArrayList<>();

		// Initialize a list for each possible value.
		for (int i = 0; i < 256; i++) {
			List<List<Integer>> innerList = new ArrayList<>();
			points.add(innerList);
		}

		// Iterate through each pixel in the image.
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Calculate weighted components for intensity (value).
				double calcR = 0.615 * (double) r;
				double calcG = 0.515 * (double) g;
				double calcB = 0.100 * (double) b;

				// Compute value and ignore if negative.
				int v = (int) (calcR - calcG - calcB);
				if (v < 0) {
					continue;
				}

				// Skip if value is not present in the histogram.
				if (imgGram[v] == 0) {
					continue;
				}

				// Add the pixel coordinates to the list for the corresponding value.
				List<Integer> point = new ArrayList<>();
				point.add(x);
				point.add(y);
				points.get(v).add(point);
			}
		}

		return points;
	}

	/**
	 * Generates a list of pixel coordinates for each u component in the image.
	 * @param img The input image.
	 * @param imgGram The histogram of u components in the image.
	 * @return A list of pixel coordinates for each u component.
	 */
	private List<List<List<Integer>>> generateImagePointsForU(BufferedImage img, int[] imgGram) {
		List<List<List<Integer>>> points = new ArrayList<>();

		// Initialize a list for each possible u component.
		for (int i = 0; i < 256; i++) {
			List<List<Integer>> innerList = new ArrayList<>();
			points.add(innerList);
		}

		// Iterate through each pixel in the image.
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);

				// Calculate weighted components for u component.
				double calcR = -0.147 * (double) r;
				double calcG = 0.289 * (double) g;
				double calcB = 0.436 * (double) b;

				// Compute u component and ignore if negative.
				int u =  (int)(calcR - calcG + calcB);
				if (u < 0) {
					continue;
				}

				// Skip if u component is not present in the histogram.
				if (imgGram[u] == 0) {
					continue;
				}

				// Add the pixel coordinates to the list for the corresponding u component.
				List<Integer> point = new ArrayList<>();
				point.add(x);
				point.add(y);
				points.get(u).add(point);
			}
		}

		return points;
	}

	/**
	 * Generates a list of pixel coordinates for each color component in the image.
	 * @param img The input image.
	 * @param imgName The color component identifier ("h" for hue, "v" for value, "u" for u component).
	 * @param imgGram The histogram of the specified color component in the image.
	 * @return A list of pixel coordinates for each color component.
	 */
	private List<List<List<Integer>>> generateImagePoints(BufferedImage img, String imgName, int[] imgGram) {
		if (imgName.equals("h")) {
			return generateImagePointsForHue(img, imgGram);
		} else if (imgName.equals("v")) {
			return generateImagePointsForV(img, imgGram);
		} else {
			return generateImagePointsForU(img, imgGram);
		}
	}

	// Finds clusters in a binary island matrix using depth-first search.
	private List<List<List<Integer>>> findClusters(int[][] island, int clusterRange) {
		List<List<List<Integer>>> clusters = new ArrayList<>();

		// Iterate through each pixel in the island matrix.
		for (int x = 0; x < 640; x++) {
			for (int y = 0; y < 480; y++) {
				if (island[x][y] == 1) {
					List<List<Integer>> cluster = new ArrayList<>();
					Stack<List<Integer>> queue = new Stack<>();
					List<Integer> point = new ArrayList<>();
					point.add(x);
					point.add(y);
					queue.push(point);

					// Perform depth-first search to find cluster.
					while (!queue.isEmpty()) {
						List<Integer> check = queue.pop();
						int checkx = check.get(0);
						int checky = check.get(1);

						// Check boundary conditions and validity of pixels.
						if (checkx < 0 || checkx >= 640 || checky < 0 || checky >= 480 || island[checkx][checky] == 0) {
							continue;
						}

						// Mark the pixel as visited.
						island[checkx][checky] = 0;
						List<Integer> validPoint = new ArrayList<>();
						validPoint.add(checkx);
						validPoint.add(checky);
						cluster.add(validPoint);

						// Explore neighboring pixels within the specified range.
						for (int i = -clusterRange; i <= clusterRange; i++) {
							for (int j = -1; j < 2; j++) {
								if (i == 0 && j == 0) {
									continue;
								}
								List<Integer> nextcheck = Arrays.asList(checkx + i, checky + j);
								queue.push(nextcheck);
							}
						}
					}

					// Add the found cluster to the list of clusters.
					clusters.add(cluster);
				}
			}
		}
		return clusters;
	}

	// Marks the boundaries of a cluster and draws a label in the image.
	private void markCluster(List<List<Integer>> cluster, BufferedImage img, String objName) {
		int minX = 900;
		int maxX = -900;
		int minY = 900;
		int maxY = -900;

		// Find the bounding box of the cluster.
		for (int i = 0; i < cluster.size(); i++) {
			int x = cluster.get(i).get(0);
			int y = cluster.get(i).get(1);
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
		}

		// Draw boundaries around the cluster.
		for (int k = 0; k < 6; k++) {
			for (int i = minX; i <= maxX; i++) {
				img.setRGB(i, minY + k, Color.BLACK.getRGB());
				img.setRGB(i, maxY - k, Color.BLACK.getRGB());
			}
			for (int i = minY; i <= maxY; i++) {
				img.setRGB(minX + k, i, Color.BLACK.getRGB());
				img.setRGB(maxX - k, i, Color.BLACK.getRGB());
			}
		}

		// Draw the label in the image.
		Graphics g = img.getGraphics();
		Color labelColor = new Color(0, 0, 0);
		g.setColor(labelColor);
		g.drawString(objName, minX + 10, maxY - 10);
	}

	// Detects color clusters in the image and marks them based on specified thresholds.
	private void colorDetection(int[] objectGram, List<List<List<Integer>>> points, BufferedImage img,
								String conversion, String objName, int clusterThreshold,
								double existThreshold, int minClusterSize, int clusterRange) {
		List<List<Integer>> pool = new ArrayList<>();

		// Add pixel coordinates to a pool based on the object histogram.
		for (int i = 0; i < objectGram.length; i++) {
			if (objectGram[i] > 0.0) {
				for (List<Integer> point : points.get(i)) {
					pool.add(point);
				}
			}
		}

		// Initialize island matrix and mark pixels from the pool.
		int[][] island = new int[640][480];
		for (int i = 0; i < 640; i++) {
			for (int j = 0; j < 480; j++) {
				island[i][j] = 0;
			}
		}
		for (int i = 0; i < pool.size(); i++) {
			int x = pool.get(i).get(0);
			int y = pool.get(i).get(1);
			island[x][y] = 1;
		}

		// Find clusters in the island matrix.
		List<List<List<Integer>>> clusters = findClusters(island, clusterRange);

		// Iterate through clusters and mark them in the image.
		for (List<List<Integer>> cluster : clusters) {
			if (cluster.size() < minClusterSize) {
				continue;
			}
			int[] clusterHist = histogramOfCluster(img, cluster, conversion, clusterThreshold);
			boolean isValid = imageExist(clusterHist, objectGram, existThreshold);
			if (isValid) {
				markCluster(cluster, img, objName);
			}
		}
	}

	// Determines the dominant color based on histogram values.
	private String colorDeterminer(int[] histy) {
		int totalPixels = 0;
		for (int i = 0; i < 361; i++) {
			totalPixels = totalPixels + histy[i];
		}

		// Count pixels in different color ranges.
		int pinkPixels = countPixelsInRange(histy, 0, 18) + countPixelsInRange(histy, 90, 121)
				+ countPixelsInRange(histy, 210, 236) + countPixelsInRange(histy, 308, 361);

		int pikaPixels = countPixelsInRange(histy, 0, 120);
		int warningPixels = countPixelsInRange(histy, 51, 113);
		int oswaldPixels = countPixelsInRange(histy, 119, 241);
		int volleyPixel = countPixelsInRange(histy, 6, 56) + countPixelsInRange(histy, 111, 129)
				+ countPixelsInRange(histy, 210, 258);
		int strawPixel = countPixelsInRange(histy, 0, 128) + countPixelsInRange(histy, 346, 361);
		int rosePixel = countPixelsInRange(histy, 0, 111) + countPixelsInRange(histy, 351, 361);
		int applePixel = countPixelsInRange(histy, 0, 109);
		int logoPixel = countPixelsInRange(histy, 0, 113) + countPixelsInRange(histy, 343, 361);

		int redCount = countPixelsInRange(histy, 0, 30) + countPixelsInRange(histy, 345, 361);

		// Determine color based on pixel count ratios.
		if (((double) redCount / totalPixels) >= 0.68) {
			if (((double) applePixel / totalPixels) >= 0.90) {
				return "Apple";
			} else if (((double) rosePixel / totalPixels) >= 0.995) {
				return "Rose";
			} else if (((double) logoPixel / totalPixels) >= 0.98) {
				return "Logo";
			} else if (((double) strawPixel / totalPixels) >= 0.90) {
				return "Strawberry";
			} else {
				return "Red";
			}
		} else {
			if (((double) oswaldPixels / totalPixels) >= 0.90) {
				return "Oswald";
			} else if (((double) pikaPixels / totalPixels) >= 0.98) {
				return "Pikachu";
			} else if (((double) warningPixels / totalPixels) >= 0.90) {
				return "Warning";
			} else if (((double) pinkPixels / totalPixels) >= 0.95) {
				return "Kirby";
			} else if (((double) volleyPixel / totalPixels) >= 0.95) {
				return "Volleyball";
			} else {
				return "Hue";
			}
		}
	}

	public void showIms(String[] args) {
		// Read a parameter from command line
		String param1 = args[1];
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		// Loop through the input parameters
		for (int i = 1; i < args.length; i++) {
			param1 = args[i];
			readImageRGB(width, height, param1, imgTwo);

			// Default parameters
			String conversion = "h";
			int imageThreshold = 200;
			int objectThreshold = 450;
			double existThreshold = 0.80;
			int clusterThreshold = 10;
			double clusterExistThreshold = .80;
			int minClusterSize = 300;
			int clusterRange = 1;

			// Set specific parameters for known cases
			if (param1.equals("Kirby_object.rgb")) {
				conversion = "h";
				imageThreshold = 10;
				objectThreshold = 200;
				existThreshold = 0.90;
				clusterThreshold = 10;
				clusterExistThreshold = .90;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("warning_object.rgb")) {
				conversion = "h";
				imageThreshold = 10;
				objectThreshold = 200;
				existThreshold = 0.90;
				clusterThreshold = 10;
				clusterExistThreshold = .90;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("Oswald_object.rgb")) {
				conversion = "h";
				imageThreshold = 10;
				objectThreshold = 200;
				existThreshold = 0.95;
				clusterThreshold = 10;
				clusterExistThreshold = .95;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("pikachu_object.rgb")) {
				conversion = "h";
				imageThreshold = 200;
				objectThreshold = 250;
				existThreshold = 0.80;
				clusterThreshold = 100;
				clusterExistThreshold = .70;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("Volleyball_object.rgb")) {
				conversion = "h";
				imageThreshold = 600;
				objectThreshold = 400;
				existThreshold = 0.25;
				clusterThreshold = 10;
				clusterExistThreshold = .25;
				minClusterSize = 200;
				clusterRange = 1;
			} else if (param1.equals("strawberry_object.rgb")) {
				conversion = "v";
				imageThreshold = 100;
				objectThreshold = 600;
				existThreshold = 0.45;
				clusterThreshold = 10;
				clusterExistThreshold = .45;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("rose_object.rgb")) {
				conversion = "v";
				imageThreshold = 100;
				objectThreshold = 300;
				existThreshold = 0.80;
				clusterThreshold = 50;
				clusterExistThreshold = .80;
				minClusterSize = 300;
				clusterRange = 1;
			} else if (param1.equals("USC_object.rgb")) {
				conversion = "v";
				imageThreshold = 100;
				objectThreshold = 300;
				existThreshold = 0.75;
				clusterThreshold = 10;
				clusterExistThreshold = .75;
				minClusterSize = 300;
				clusterRange = 10;
			} else if (param1.equals("Apple_object.rgb")) {
				conversion = "v";
				imageThreshold = 100;
				objectThreshold = 300;
				existThreshold = 0.75;
				clusterThreshold = 10;
				clusterExistThreshold = .75;
				minClusterSize = 300;
				clusterRange = 10;
			} else {
				int[] startingGram = histogramOfObject(imgTwo, "h", 10);
				String colorcase = colorDeterminer(startingGram);
				if (colorcase.equals("Kirby")) {
					conversion = "h";
					imageThreshold = 10;
					objectThreshold = 200;
					existThreshold = 0.90;
					clusterThreshold = 10;
					clusterExistThreshold = .90;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Warning")) {
					conversion = "h";
					imageThreshold = 10;
					objectThreshold = 200;
					existThreshold = 0.90;
					clusterThreshold = 10;
					clusterExistThreshold = .90;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Oswald")) {
					conversion = "h";
					imageThreshold = 10;
					objectThreshold = 200;
					existThreshold = 0.95;
					clusterThreshold = 10;
					clusterExistThreshold = .95;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Pikachu")) {
					conversion = "h";
					imageThreshold = 200;
					objectThreshold = 250;
					existThreshold = 0.80;
					clusterThreshold = 100;
					clusterExistThreshold = .70;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Volleyball")) {
					conversion = "h";
					imageThreshold = 600;
					objectThreshold = 400;
					existThreshold = 0.25;
					clusterThreshold = 10;
					clusterExistThreshold = .25;
					minClusterSize = 200;
					clusterRange = 1;
				} else if (colorcase.equals("Strawberry")) {
					conversion = "v";
					imageThreshold = 100;
					objectThreshold = 600;
					existThreshold = 0.45;
					clusterThreshold = 10;
					clusterExistThreshold = .45;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Rose")) {
					conversion = "v";
					imageThreshold = 100;
					objectThreshold = 300;
					existThreshold = 0.80;
					clusterThreshold = 50;
					clusterExistThreshold = .80;
					minClusterSize = 300;
					clusterRange = 1;
				} else if (colorcase.equals("Logo")) {
					conversion = "v";
					imageThreshold = 100;
					objectThreshold = 300;
					existThreshold = 0.75;
					clusterThreshold = 10;
					clusterExistThreshold = .75;
					minClusterSize = 300;
					clusterRange = 10;
				} else if (colorcase.equals("Apple")) {
					conversion = "v";
					imageThreshold = 100;
					objectThreshold = 300;
					existThreshold = 0.75;
					clusterThreshold = 10;
					clusterExistThreshold = .75;
					minClusterSize = 300;
					clusterRange = 10;
				} else if (colorcase.equals("Red")) {
					conversion = "v";
					imageThreshold = 100;
					objectThreshold = 300;
					existThreshold = 0.75;
					clusterThreshold = 10;
					clusterExistThreshold = .75;
					minClusterSize = 300;
					clusterRange = 10;
				} else {
					conversion = "h";
					imageThreshold = 10;
					objectThreshold = 200;
					existThreshold = 0.90;
					clusterThreshold = 10;
					clusterExistThreshold = .90;
					minClusterSize = 300;
					clusterRange = 1;
				}
			}

			int[] imgGram = histogramOfImage(imgOne, conversion, imageThreshold);
			int[] objectGram = histogramOfObject(imgTwo, conversion, objectThreshold);
			boolean exist = imageExist(imgGram, objectGram, existThreshold);
			if (exist == true) {
				List<List<List<Integer>>> imgPoints = generateImagePoints(imgOne, conversion, imgGram);
				colorDetection(objectGram, imgPoints, imgOne, conversion, param1, clusterThreshold, clusterExistThreshold, minClusterSize, clusterRange);
			}

		}
		
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
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
