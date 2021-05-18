#!/bin/sh


cd src
cd programmierprojekt_lk_pw
javac Main.java Graph.java DistNodePair.java Quadtree.java QuadtreeNode.java
cd ..
java -Xmx12g programmierprojekt_lk_pw.Main
