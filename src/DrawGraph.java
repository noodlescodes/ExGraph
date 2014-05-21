import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import javax.swing.JFrame;

public class DrawGraph extends JFrame {

	private static final long serialVersionUID = 1L;
	int WIDTH = 1920;
	int HEIGHT = 1000;
	int[][] nodes;
	int[][] edges;
	int[][] edgeCoords;
	int[][] arcCoords;
	int deltaL = 3;
	int deltaU = 4;
	int onesUp = 0;
	int onesDown = 0;

	public DrawGraph() {
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		nodes = new int[deltaL * deltaU + 1][2];
		edges = new int[nodes.length][nodes.length];
	}

	public void calculatePosition() {
		nodes[0][0] = 950;
		nodes[0][1] = 40;
		int d = WIDTH / (deltaL);
		for(int i = 1; i < deltaU; i++) {
			nodes[i][0] = (i - 1) * d + d / 2 - 10;
			nodes[i][1] = 300;
		}
		d = WIDTH / (deltaL * deltaL);
		for(int i = deltaU; i < nodes.length; i++) {
			nodes[i][0] = (i - deltaU) * d + d / 2 - 8;
			nodes[i][1] = 600;
		}
	}

	public void read(String file) {
		String line;
		String[] lineArray;

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			int count = 0;
			while((line = br.readLine()) != null) {
				lineArray = line.split(",");
				for(int i = 0; i < lineArray.length; i++) {
					edges[count][i] = Integer.parseInt(lineArray[i]);
					if(edges[count][i] == 1 && i < deltaU) {
						onesUp++;
					}
					else if(edges[count][i] == 1) {
						onesDown++;
					}
				}
				count++;
			}
			br.close();
		}
		catch(Exception e) {
			System.out.println("Couldn't read file");
		}
	}

	public void generateEdges() {
		edgeCoords = new int[onesUp][4];
		int count = 0;
		for(int i = 0; i < edges.length; i++) {
			for(int j = 0; j < edges[i].length; j++) {
				if(edges[i][j] == 1 && i < deltaU) {
					edgeCoords[count][0] = nodes[i][0] + 20;
					edgeCoords[count][1] = nodes[i][1] + 20;
					edgeCoords[count][2] = nodes[j][0] + 20;
					edgeCoords[count][3] = nodes[j][1] + 20;
					count++;
				}
			}
		}
	}

	public void generateArcs() {
		arcCoords = new int[onesDown][3];
		int count = 0;
		for(int i = 0; i < edges.length; i++) {
			for(int j = 0; j < edges[i].length; j++) {
				if(edges[i][j] == 1 && i >= deltaU && j >= deltaU) {
					arcCoords[count][0] = Math.min(nodes[i][0], nodes[j][0]) + 20;
					arcCoords[count][1] = nodes[i][1];
					arcCoords[count][2] = Math.max(nodes[i][0], nodes[j][0]) - Math.min(nodes[i][0], nodes[j][0]);
					count++;
				}
			}
		}
	}

	public void paint(Graphics g) {
		super.paintComponents(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		
		Random rand = new Random(); // used for random colours

		for(int i = 0; i < edgeCoords.length; i++) {
			g2.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
			g2.drawLine(edgeCoords[i][0], edgeCoords[i][1], edgeCoords[i][2], edgeCoords[i][3]);
		}

		for(int i = 0; i < arcCoords.length; i++) {
			g2.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
			g2.drawArc(arcCoords[i][0], arcCoords[i][1] - 240, arcCoords[i][2], 500, 180, 180);
		}

		// Draw the ovals after the lines
		g2.setColor(new Color(0, 0, 0));
		for(int i = 0; i < nodes.length; i++) {
			g2.fillOval(nodes[i][0], nodes[i][1], 40, 40);
		}
	}

	public static void main(String[] args) {
		DrawGraph dg = new DrawGraph();
		dg.calculatePosition();
		dg.read("ExSolution13CSV.csv");
		dg.generateEdges();
		dg.generateArcs();
	}
}
