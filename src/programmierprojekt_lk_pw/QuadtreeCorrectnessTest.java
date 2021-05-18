package programmierprojekt_lk_pw;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

class QuadtreeCorrectnessTest {

	@Test
	void test() throws IOException {
		class Reader {

			final private int BUFFER_SIZE = 8192; // Buffer Size of 1024
			private final DataInputStream dataInputStream;
			private final byte[] buffer;
			private int bufferPointer, bytesRead;

			public Reader(final String file_name) throws IOException {
				dataInputStream = new DataInputStream(new FileInputStream(file_name));
				buffer = new byte[BUFFER_SIZE];
				bufferPointer = 0;
				bytesRead = 0;
			}

			// To skip the first lines
			public void skipLine() throws IOException {
				int c = 0;
				while ((c = readNextByte()) != -1) {
					// Detect the beginning of a new line
					if (c == '\n')
						break;
				}
			}

			// For node numbers and edge numbers. int suffices for the Deutschlandgraph
			public int nextInt() throws IOException {
				int number = 0;
				byte currentByte = readNextByte();
				while (currentByte <= ' ')
					currentByte = readNextByte();
				do {
					// Trick: Subtract the char '0' from a byte to get an int,
					// char's are actually of the same type / length as shorts
					// when you have a char that represents a ASCII digit (like '1' to '9' here)
					// and you subtract the smallest possible ASCII digit from it ('0'),
					// then you'll be left with the digit's corresponding value
					// the casting from char to int is done automatically because arithmetics are
					// involved
					number = number * 10 + (currentByte - '0');
				} while ((currentByte = readNextByte()) >= '0' && currentByte <= '9');

				return number;
			}

			// For Longitude and Latitudes of the graph
			public double nextDouble() throws IOException {
				double number = 0, divisionFactor = 1;
				byte currentByte = readNextByte();
				while (currentByte <= ' ')
					currentByte = readNextByte();
				do {
					number = number * 10 + (currentByte - '0');
				} while ((currentByte = readNextByte()) >= '0' && currentByte <= '9');

				if (currentByte == '.') {
					while ((currentByte = readNextByte()) >= '0' && currentByte <= '9') {
						number += (currentByte - '0') / (divisionFactor *= 10);
					}
				}
				return number;
			}

			// Helper functions to convert text into bytes or detecting if there is no text
			// to read

			private byte readNextByte() throws IOException {
				if (bufferPointer == bytesRead) {
					fillBuffer();
				}
				return buffer[bufferPointer++];
			}

			// Convert the text into bytes.
			private void fillBuffer() throws IOException {
				bytesRead = dataInputStream.read(buffer, bufferPointer = 0, BUFFER_SIZE);
				if (bytesRead == -1) // No more characters to read
					buffer[0] = -1;
			}
		}

		final Scanner inputScanner = new Scanner(System.in);
		System.out.println("Enter file name of the graph (path):");
		final Reader reader = new Reader(inputScanner.nextLine());
		inputScanner.close();
		final Graph mapGraph;
		// Opening file
		// Skipping first 4 lines
		reader.skipLine();
		reader.skipLine();
		reader.skipLine();
		reader.skipLine();
		// Taking the time
		final long startTime = System.currentTimeMillis();
		// Initializing graph
		final int nodeCount = reader.nextInt();
		final int edgeCount = reader.nextInt();
		mapGraph = new Graph(nodeCount, edgeCount);
		for (int i = 0; i < nodeCount; i++) {
			reader.nextInt();
			reader.nextInt();
			mapGraph.addNode(reader.nextDouble(), reader.nextDouble());
			reader.nextInt();
		}
		for (int i = 0; i < edgeCount; i++) {
			mapGraph.addEdge(reader.nextInt(), reader.nextInt(), reader.nextInt());
			reader.nextInt();
			reader.nextInt();
		}

		// Output time
		final long finalTime = (System.currentTimeMillis() - startTime);

		System.out.println("Reading the file took " + finalTime + "ms.");

		boolean fail = false;
		System.out.println("test the quadtree implementation for 100 random coordinate pairs");
		double rand_latitude = 0;
		double rand_longitude = 0;
		int sumTime = 0;
		double meanTime = 0;
		for (int i = 0; i < 100; i++) {
			rand_latitude = mapGraph.getMinLat() + (mapGraph.getMaxLat()-mapGraph.getMinLat())*Math.random();
			rand_longitude = mapGraph.getMinLong() + (mapGraph.getMaxLong()-mapGraph.getMinLong())*Math.random();
			int time = mapGraph.nearestNeighborCalculationTest(rand_latitude, rand_longitude);
			if (time == -1) {
				fail = true;
			}else {
				sumTime += time;
			}
		}
		if (!fail) {
			meanTime = sumTime/100;
			System.out.println("The test was successful. The tree search took on average "+meanTime+" ms.");
		}
		assertEquals(false, fail);
	}

}
