package programmierprojekt_lk_pw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * Programmierprojekt WS19 : Ein webbasierter Routenplaner
 * Luka Kapsachilis (3424492) st163158@stud.uni-stuttgart.de
 * Pascal Walloner (3494480) st170276@stud.uni-stuttgart.de
 * Hoai Nam Trinh (3390830) st161171@stud.uni-stuttgart.de
 */
import java.util.Scanner;

//Uses inputDataStream to read through the stream of data and uses read() method and nextInt() methods for taking inputs.
//Approach with Buffering 

public class Main {

	static class Reader {

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

	// Zerlegung einer xhr Query vom Client/Website an den Server, um die einzelnen
	// gelieferten
	// Parameter zu extrahieren
	public static Map<String, List<String>> getQueryParams(final String url) {
		try {
			final Map<String, List<String>> params = new HashMap<String, List<String>>();
			final String[] urlParts1 = url.split(" "); // Splitting by Regex in two phases
			final String temp = urlParts1[1]; // das wichtgiste Stück wo die ganze Information enthalten ist:
												// start=15&destination=20
			System.out.println("Incoming Start-Destination Query:" + url);
			final String[] urlParts2 = temp.split("\\?");
			if (urlParts2.length > 1) {
				final String query = urlParts2[1];
				for (final String param : query.split("&")) {
					final String[] pair = param.split("=");
					final String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}

					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values); // Bsp: (start,15) (destination,20)
					}
					values.add(value);
				}
			}
			return params;
		} catch (final UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}

	@SuppressWarnings({ "unused", "resource" })
	public static void main(final String[] args) throws IOException {

		final Scanner inputScanner = new Scanner(System.in);
		System.out.println("Enter file name of the graph (path):");
		final Reader reader = new Reader(inputScanner.nextLine());

		final Graph mapGraph;
		// Opening file

		// Skipping first 4 lines
		reader.skipLine();
		reader.skipLine();
		reader.skipLine();
		reader.skipLine();

		try {
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

			// WEBSERVER
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(8080);

			} catch (final IOException e) {
				System.err.println("Could not listen on port: 8080.");
				System.exit(1);
			}

			Socket clientSocket = null;
			final boolean running = true;
			boolean display = true;
			boolean connected = false;
			System.out.println("0");
			final String responseBody = "<!DOCTYPE html>\n" + "<html>\n" + "    <head>\n"
					+ "        <meta charset=\"UTF-8\"></meta>\n"
					+ "        <title>Webbasierter Routenplaner LK PW HNT</title>\n"
					+ "        <!--import of leaflet--> \n"
					+ "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></meta>\n"
					+ "        <link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"docs/images/favicon.ico\" />\n"
					+ "        <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.6.0/dist/leaflet.css\" integrity=\"sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ==\" crossorigin=\"\"/>\n"
					+ "        <script src=\"https://unpkg.com/leaflet@1.6.0/dist/leaflet.js\" integrity=\"sha512-gZwIG9x3wUXg2hdXF6+rVkLF/0Vi9U8D2Ntg4Ga5I5BZpVkVxlJWbSQtXPSiUTtC0TjtGOmxa1AJPuV0CPthew==\" crossorigin=\"\"></script>\n"
					+ "        <style>\n" + "            #mapid{\n" + "                position: relative; \n"
					+ "                left: 50%;\n" + "                transform: translateX(-50%);\n"
					+ "                top:50px;\n" + "                \n" + "            }\n"
					+ "        </style>    \n" + "    </head>\n" + "\n" + "    <body>\n"
					+ "        <!--Anfrageformulare-->\n" + "        <p><b>Start- und Zielanfrage</b> </p>\n"
					+ "        <p>Bitte Start- und Zielknoten eingeben: </p>\n" + "\n"
					+ "        <form method = \"GET\" id = \"myForm\" class = \"ajax\">\n"
					+ "            <input type=\"number\" id=\"start\" name=\"start\" min=\"0\" max=\"25115476\" step=\"1\" value=\"0\"></input>\n"
					+ "            <input type=\"number\" id=\"destination\" name=\"destination\" min=\"0\" max=\"25115476\" step=\"1\" value=\"0\"></input>\n"
					+ "            <input type = \"submit\" value=\"Send\"></input>\n" + "\n" + "        </form>\n"
					+ "        <p>Die Distanz zwischen den beiden Knoten ist: </p>\n"
					+ "        <div id=\"display\"></div> <!--- hier werden Antworten angezeigt --->\n"
					+ "        <div id=\"mapid\" style=\"width: 600px; height: 400px;\"></div> <!--- hier wird die karte angezeigt --->\n"
					+ "\n" + "        <p><b>Naechster-Knoten-Suche</b> </p>\n"
					+ "        <p>Bitte Breiten- und Laengengrad eingeben: </p>\n"
					+ "        <form method = \"GET\" id = \"myForm2\" class = \"ajax2\">\n"
					+ "            <input type=\"number\" id=\"latitude\" name=\"latitude\" min=\"46\" max=\"55\" step=\"any\" value=\"46\"></input>\n"
					+ "            <input type=\"number\" id=\"longitude\" name=\"longitude\" min=\"5\" max=\"17\" step=\"any\" value=\"5\"></input>\n"
					+ "            <input type = \"submit\" value=\"Send\"></input>\n"
					+ "        <p>Der naechste Knoten ist: </p>\n" + "        <div id=\"displayNeighbour\"></div>\n"
					+ "        <p>Er hat die Koordinaten: </p>\n" + "        <div id=\"displayLatitude\"></div>\n"
					+ "        <div id=\"displayLongitude\"></div>\n" + "\n"
					+ "        <div id=\"mapid\" style=\"width: 600px; height: 400px;\"></div> <!--- hier wird die karte angezeigt --->\n"
					+ "        \n" + "        <!--Client Anfrage JQuery Skript-->\n"
					+ "        <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js\" type=\"text/javascript\"></script>\n"
					+ "        <script>\n" + "\n" + "        \n"
					+ "            var mymap = L.map('mapid').setView([51.165691, 10.451526], 6);\n" + "			\n"
					+ "			//Path and markers\n" + "			var feature = {\n"
					+ "				\"type\": \"LineString\",\n" + "				\"coordinates\": []\n"
					+ "			};\n" + "			var gjLayer = L.geoJSON(feature);\n"
					+ "			gjLayer.addTo(mymap);\n" + "\n" + "            //display for longitude and latitude\n"
					+ "            var popup = L.popup();\n" + "            function onMapClick(e) {\n"
					+ "                alert(\"Lat, Lon : \" + e.latlng.lat + \", \" + e.latlng.lng)\n"
					+ "            popup\n" + "                .setLatLng(e.latlng)\n" + "                \n"
					+ "                .setContent(e.latlng.toString())\n" + "                .openOn(mymap);\n"
					+ "                \n" + "            }\n" + "            mymap.on('click', onMapClick);\n" + "\n"
					+ "            //tile Layer map externally imported\n"
					+ "            L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {\n"
					+ "                maxZoom: 18,\n"
					+ "                attribution: 'Map data &copy; <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a> contributors, ' +\n"
					+ "                    '<a href=\"https://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, ' +\n"
					+ "                    'Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>',\n"
					+ "                id: 'mapbox/streets-v11'\n" + "            }).addTo(mymap);\n" + "			\n"
					+ "            //    Distanz zwischen den beiden Knoten Anfrage\n"
					+ "			$('form.ajax').on('submit',function(event){\n"
					+ "                event.preventDefault(); // verhindert den reload der Seite\n"
					+ "                console.log($(this).serialize()); // was an den server geschickt wird\n"
					+ "                // Formular per AJAX senden\n" + "                $.ajax({\n"
					+ "                    type: 'GET',\n" + "                    url: $(this).prop('action'),\n"
					+ "                    data: $(this).serialize(),\n" + "                    dataType: 'json',\n"
					+ "                    success: function(data){\n"
					+ "                        //var distance = Object.values(data)[0];\n"
					+ "						var distance = data['distance'];\n"
					+ "						var path = data['path'];\n"
					+ "                        console.log(distance);\n"
					+ "                        document.getElementById(\"display\").innerHTML = distance;\n"
					+ "						\n" + "						gjLayer.remove();\n"
					+ "						gjLayer = L.geoJSON(path);\n"
					+ "						gjLayer.addTo(mymap);\n" + "                    }\n"
					+ "                }); \n" + "                return false;\n" + "\n" + "            });\n" + "\n"
					+ "            //Naechster-Knoten-Anfrage\n"
					+ "            $('form.ajax2').on('submit',function(event){\n"
					+ "                event.preventDefault();\n"
					+ "                console.log($(this).serialize());\n" + "                $.ajax({\n"
					+ "                    type: 'GET',\n" + "                    url: $(this).prop('action'),\n"
					+ "                    data: $(this).serialize(),\n" + "                    dataType: 'json',\n"
					+ "                    success: function(data){\n"
					+ "                        var distance = Object.values(data)[0];\n"
					+ "                        document.getElementById(\"displayNeighbour\").innerHTML = distance;\n"
					+ "                        var latitude = Object.values(data)[1];\n"
					+ "                        document.getElementById(\"displayLatitude\").innerHTML = latitude;\n"
					+ "                        var longitude = Object.values(data)[2];\n"
					+ "                        document.getElementById(\"displayLongitude\").innerHTML = longitude;\n"
					+ "                    }\n" + "                });\n" + "                return false;\n"
					+ "            });\n" + "\n" + "        </script>\n" + "    </body>\n" + "</html> \n" + "\n" + "";
			System.out.println("1");
			while (running) {
				System.out.println("2");
				try {
					System.out.println("3");
					clientSocket = serverSocket.accept();

					if (clientSocket != null) {
						System.out.println("Connected");
					}
				} catch (final IOException e) {
					System.err.println("Accept failed.");
					System.exit(1);
				}
				connected = true;

				final InputStream in = clientSocket.getInputStream();
				final BufferedReader socketReader = new BufferedReader(new InputStreamReader(in)); // Hier kommen die
																									// Xhr requests von
																									// der Website in
																									// Form einer URI
																									// an, welche die
																									// Parameter enthält
				final OutputStream out = clientSocket.getOutputStream(); // Channel to send response directly to html
																			// website

				final PrintWriter socketWriter = new PrintWriter(new OutputStreamWriter(out)); // Channel for the
				// display

				// Für Start-Destination Queries: /action_page.php?start=2&destination=3
				// HTTP/1.1
				String response = "0";
				final String temp = socketReader.readLine();
				if (temp != null) {
					connected = false; // I dont know why exactly here.
					final String temp2 = temp.replaceAll("\\s+", "");
					if (temp2.length() > 12) { // dann ist es keine leere noise request

						final Map<String, List<String>> parameter = getQueryParams(temp);
						// Fall 1: Distanz und Pfadanfrage
						if (parameter.get("start") != null && parameter.get("destination") != null) {

							List<Integer> path = new ArrayList<Integer>(mapGraph.getNodeCount());
							path = mapGraph.findPath(
									Integer.parseInt(((ArrayList<String>) parameter.get("start")).get(0)),
									Integer.parseInt(((ArrayList<String>) parameter.get("destination")).get(0)), true,
									false);
							if (path == null) {
								System.out.println("No path found!");
								continue;
							}
							final int distance0 = path.get(0);
							response = Integer.toString(distance0);

							String pathJson = "{ \"type\": \"LineString\", \"coordinates\": [";
							for (int i = 1; i < path.size(); i++) {
								final double[] nodeCoords = mapGraph.getNodeCoordinates(path.get(i));
								pathJson = pathJson + "[" + nodeCoords[1] + ", " + nodeCoords[0] + "]";
								if (i < path.size() - 1) {
									pathJson = pathJson + ", ";
								}
							}
							pathJson = pathJson + "]}";

							final int pathLength = path.size() - 1;
							final String json = "{ \"distance\": " + distance0 + ", \"path\": " + pathJson + "}";

							// Hier bauen wir die HTTP/1.1 response, die an den Server geht
							final StringBuilder builder = new StringBuilder();
							builder.append("HTTP/1.1 200 OK\r\n");
							builder.append("Content-Type: application/json; charset=utf-8\r\n");

							builder.append("Content-Length: " + json.length());
							builder.append("\r\n\r\n");
							builder.append(json);
							socketWriter.print(builder.toString());
							socketWriter.flush();

							socketWriter.close();
							System.out.println("Shortest distance: " + distance0);
							path.clear();
						} // Fall 2 Längen- und Breitengrad- Anfrage mit Ausgabe der Lat Long dieses
							// Neighbours
						else if (parameter.get("longitude") != null && parameter.get("latitude") != null) {

							final int neighbour = mapGraph.getNearestNeighborTree(
									Double.parseDouble(parameter.get("latitude").get(0)),
									Double.parseDouble(parameter.get("longitude").get(0)));
							final double[] coordinates = mapGraph.getNodeCoordinates(neighbour);
							final double latitude = coordinates[0];
							final double longitude = coordinates[1];
							final String neighbourString = Integer.toString(neighbour);
							final StringBuilder builder = new StringBuilder();
							builder.append("HTTP/1.1 200 OK\r\n");
							builder.append("Content-Type: application/json; charset=utf-8\r\n");
							final String responseNeighbour = "{\"neighbour\":" + neighbour + ",\"latitude\":" + latitude
									+ ",\"longitude\":" + longitude + "}";
							builder.append("Content-Length: " + responseNeighbour.length());
							builder.append("\r\n\r\n");
							builder.append(responseNeighbour);
							socketWriter.print(builder.toString());
							socketWriter.flush();
							socketWriter.close();
							System.out.println(neighbourString);
						}
					}
					display = true;
				}
				if (display) {
					socketWriter.println("HTTP/1.0 200 OK");
					socketWriter.println("Content-Type: text/html; charset=ISO-8859-1");
					final int responseLength = responseBody.length();
					socketWriter.println("Content-Length: " + responseLength);
					socketWriter.println("Server: NanoHTTPServer");
					socketWriter.println();
					socketWriter.println(responseBody);

					socketWriter.flush(); // Flush
					display = false;
					socketWriter.close();

				}

			}
		} catch (final FileNotFoundException e) {
			System.err.println("File not found!");
			System.exit(1);
		} catch (final IOException e) {
			System.exit(1);
		}
//		} catch (final NumberFormatException e) {
//			System.err.println("Wrong number format in file!");
//			System.exit(1);
//		} catch (final IllegalArgumentException e) {
//			System.err.println("Invalid input!");
//			System.exit(1);
//		}

	}

}
