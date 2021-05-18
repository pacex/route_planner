Programmierprojekt WS19 : Ein Webbasierter Routenplaner
 * Luka Kapsachilis 
 * Pascal Walloner 
 * Hoai Nam Trinh 

Note: 
- Checkout branch "webserver"

- Run the build script either as "bash build.sh"
			or as "./build.sh"

- Load the map file *.fmi (bw.fmi for Baden-Württemberg)

After running the build file, type "localhost:8080" in your browser to open the locally hosted site.


//////////Konsole///////////////////////
"Enter file name of the graph (path):" 
	// Bitte die Location des Graphen angeben. (Bsp. für die Graph-Datei auf dem Desktop: C:\Users\Alice\Desktop\germany.fmi)
	
	1: "read query file"
		// Bitte die Location der Query-Datei angeben. (Bsp. für die Query-Datei auf dem Desktop: C:\Users\Alice\Desktop\germany.que)
		// Bei Anfragen aus Query-Dateien wird der one-to-all-Dijkstra verwendet

	2: "enter single query" 
		// 1 für eine einzige Start-Ziel Anfrage (one-to-one-Dijkstra), 2, um Dijkstra von einem Knoten zu allen anderen Knoten zu berechnen, sowie danach Distanzen vom Startknoten aus abzufragen (one-to-all-Dijkstra)
		
		1: "Please enter the starting point:"/"Please enter the destination:" 
			// Startknotenindex und Zielknotenindex nacheinander als Zahl einzugeben
		
		2: "Please enter the fixed starting point" 
			// Den Startknotenindex eingeben, der für alle weiteren Anfragen der Startknotenindex ist
			"Please enter the destination" 
				// In einer Schleife können nun beliebig viele Anfragen gestellt werden
	3: "search for nearest neighbor"
		// Bitte zuerst gewünschten latitude und dann longitude eingeben, um per Quadtree den dem Punkt nächsten Knoten zu erhalten.
		
	4: "test the Quadtree implementation for nearest neighbor search"
		//Testet für 100 zufällige Koordinaten innerhalb der axis-aligned bounding box des geladenen Graphen die Quadtree implementierung um den nächsten Knoten zur
		//zufällig generierten Koordinate zu finden. Zusätzlich zur Korrektheit wird ausgegeben, wie lange die iterative Methode und die Quadtree Methode für die Suchanfrage gebraucht hat.
