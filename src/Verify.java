import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Verify {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		int nVertices = 43;

		int[][] sol = new int[nVertices][nVertices];
		int[][] triangleInGraph = new int[nVertices][nVertices];
		int[][] squaresInGraph = new int[nVertices][nVertices];

		try {
			BufferedReader br = new BufferedReader(new FileReader("ExSolution13CSV.csv"));
			String line;
			int count = 0;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					String[] linesplit = line.split(",");
					for(int i = 0; i < linesplit.length; i++) {
						int num = Integer.parseInt(linesplit[i]);
						sol[count][i] = num;
					}
					count++;
				}
			}
			br.close();
		}
		catch(FileNotFoundException e) {
		}
		catch(IOException e) {
		}

		int maxs = 0;
		int squares = 0;
		for(int i = 0; i < nVertices; i++) {
			for(int j1 = 0; j1 < nVertices; j1++) {
				for(int k = 0; k < nVertices; k++) {
					for(int l = 0; l < nVertices; l++) {
						if(i != j1 && i != k && i != l && j1 != k && j1 != l && k != l) {
							int sum = sol[i][j1] + sol[j1][k] + sol[k][l] + sol[l][i];
							if(sum > maxs) {
								maxs = sum;
							}
							if(sum > 3) {
								squares++;
								System.out.println("Square #" + squares + ": i,j: " + i + "," + j1 + "; j,k: " + j1 + "," + k + "; k,l: " + k + "," + l + "; l,i: " + l + "," + i);
								squaresInGraph[i][j1] = 1;
							}
						}
					}
				}
			}
		}

		int maxt= 0;
		int triangles = 0;
		for(int i = 0; i < nVertices; i++) {
			for(int j1 = 0; j1 < nVertices; j1++) {
				for(int k = 0; k < nVertices; k++) {
					if(i != j1 && j1 != k && i != k) {
						int sum = sol[i][j1] + sol[j1][k] + sol[i][k];
						if(sum > maxt) {
							maxt = sum;
						}
						if(sum > 2) {
							triangles++;
							System.out.println("Triangle #" + triangles + ": i,j: " + i + "," + j1 + "; j,k: " + j1 + "," + k + "; k,i: " + k + "," + i);
							triangleInGraph[i][j1] = 1;
						}
					}
				}
			}
		}

		if(false) {
			for(int i = 0; i < nVertices; i++) {
				int sum = 0;
				for(int j = 0; j < nVertices; j++) {
					sum += sol[i][j];
				}
				System.out.println("Sum of row " + i + " is: " + sum);
				sum = 0;
			}
		}
		
		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("triangles.csv")));
			for (int i = 0; i < triangleInGraph.length; i++) {
				for (int j = 0; j < triangleInGraph.length; j++) {
					if (j + 1 < triangleInGraph.length) {
						w.print(triangleInGraph[i][j] + ",");
					} else {
						w.print(triangleInGraph[i][j]);
					}
				}
				w.println("");
			}
			w.close();
		} catch (IOException e) {
		}
		
		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("squares.csv")));
			for (int i = 0; i < squaresInGraph.length; i++) {
				for (int j = 0; j < squaresInGraph.length; j++) {
					if (j + 1 < squaresInGraph.length) {
						w.print(squaresInGraph[i][j] + ",");
					} else {
						w.print(squaresInGraph[i][j]);
					}
				}
				w.println("");
			}
			w.close();
		} catch (IOException e) {
		}

		System.out.println("Maxt: " + maxt);
		System.out.println("Maxs: " + maxs);
		System.out.println("Triangles: " + triangles / 6);
		System.out.println("Squares: " + squares / 8);
	}
}
