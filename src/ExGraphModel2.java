import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class ExGraphModel2 {

	// problem variables
	public int deltaL = 7; // minimum degree
	public int deltaU = 8; // maximum degree
	public int nVertices = deltaL * deltaU + 1; // number of vertices

	// model variables
	LpSolve lp;
	double[] row;
	int[] colno;

	// output options
	public static final boolean VERBOSE = true;
	public static final boolean OUTPUT = false;

	public static void dumpTime(long t) {
		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("timeDump.txt", true)));
			w.println("EX: " + t);
			w.close();
		}
		catch(IOException e) {
		}
	}

	public String createString(int i, int j) {
		return "B" + i + "," + j + "E";
	}

	public int getIndex(int i, int j) {
		return nVertices * (i - 1) + j;
	}

	// returns the submatrix starting at i0, j0 and finishes at i0 + deltaL, j0 + deltaL
	public int[][] getSubMatrix(int i0, int j0) {
		int[][] submatrix = new int[deltaL][deltaL];

		for(int i = 0; i < deltaL; i++) {
			for(int j = 0; j < deltaL; j++) {
				submatrix[i][j] = getIndex(i + i0, j + j0);
			}
		}

		return submatrix;
	}

	public int execute() throws LpSolveException {
		int ret = 0;
		int j = 0;
		colno = new int[nVertices * nVertices];
		row = new double[nVertices * nVertices];

		lp = LpSolve.makeLp(0, nVertices * nVertices);
		if(lp.getLp() == 0) {
			ret = 1;
		}

		if(ret == 0) {
			if(VERBOSE) {
				System.out.println("Setting up model name.");
			}
			// setup model name
			lp.setLpName("ExGraph v2");

			if(VERBOSE) {
				System.out.println("Setting up variable names.");
			}
			// setup variable names
			int loop = 1;
			for(int i = 1; i <= nVertices; i++) {
				for(int j1 = 1; j1 <= nVertices; j1++) {
					lp.setColName(loop, createString(i, j1));
					lp.setBinary(loop++, true);
				}
			}

			double[] weights = new double[nVertices];
			for(int i = 0; i < weights.length; i++) {
				weights[i] = 1;
			}

			if(VERBOSE) {
				System.out.println("Setting build mode.");
			}
			// setup build mode
			lp.setAddRowmode(true);

			// ---begin adding predefined constraints---
			if(VERBOSE) {
				System.out.println("Adding predefined constraints.");
			}
			// rows two through deltaU
			for(int i = 2; i <= deltaU; i++) {
				for(int j1 = 1; j1 <= deltaL; j1++) {
					lp.setBounds(getIndex(i, j1 + (i - 1) * deltaL + 1), 1, 1.5);
				}
			}
			// first row
			for(int i = 2; i <= deltaU; i++) {
				lp.setBounds(getIndex(1, i), 1, 1.5);
			}
			// identity matrix
			j = deltaL + deltaU + 1;
			for(int i = deltaU + 1; i <= deltaL + deltaU; i++) {
				for(int b = 1; b < deltaL; b++) {
					lp.setBounds(getIndex(i, j + (b - 1) * deltaL), 1, 1.5);
				}
				j++;
			}

			// adds 0 around the trace where approprite
			// for(int c = 0; c < deltaL; c++) {
			// for(int i = deltaU + 1 + c * deltaL; i < deltaU + deltaL * (c + 1); i++) {
			// lp.setBounds(getIndex(i, j), 0, 0);
			// }
			// }
			// ---end adding predefined constraints---

			// ---begin a_{i,i}=0 constraints---
			if(VERBOSE) {
				System.out.println("Beginning a_{i,i}=0 constraints.");
			}
			for(int i = 1; i <= nVertices; i++) {
				colno[j] = getIndex(i, i);
				row[j++] = 1;
				lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
				j = 0;
			}
			// ---end a_{i,i}=0 constraints---

			// ---begin a_{i,j}=a_{j,i} constraints---
			if(VERBOSE) {
				System.out.println("Beginning a_{i,j}=a_{j,i} constraints.");
			}
			j = 0;
			for(int i = 1; i <= nVertices; i++) {
				for(int j1 = 1; j1 <= nVertices; j1++) {
					if(i != j1) {
						colno[j] = getIndex(i, j1);
						row[j++] = 1;
						colno[j] = getIndex(j1, i);
						row[j++] = -1;
						lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
					}
					j = 0;
				}
			}
			// ---end a_{i,j}=a_{j,i} constraints---

			// ---begin one 1 in each row/column of submatrix---
			if(VERBOSE) {
				System.out.println("Beginning one 1 in each row/column of submatrix constraints.");
			}
			j = 0;
			for(int i = deltaU + deltaL + 1; i <= deltaU + deltaL * deltaL; i += deltaL) {
				for(int j0 = deltaU + deltaL + 1; j0 <= deltaU + deltaL * deltaL; j0 += deltaL) {
					if(i == j0 || j0 < i) {
						continue;
					}
					int[][] submatrix = getSubMatrix(i, j0);
					for(int n = 0; n < submatrix.length; n++) {
						j = 0;
						for(int m = 0; m < submatrix[n].length; m++) {
							colno[j] = submatrix[n][m];
							row[j++] = 1;
						}
						lp.addConstraintex(j, row, colno, LpSolve.EQ, 1);
					}
					for(int m = 0; m < submatrix.length; m++) {
						j = 0;
						for(int n = 0; n < submatrix[m].length; n++) {
							colno[j] = submatrix[n][m];
							row[j++] = 1;
						}
						lp.addConstraintex(j, row, colno, LpSolve.EQ, 1);
					}
				}
			}
			// ---end one 1 in each row/column submatrix---

			// ---begin no triangles constraint---
			if(VERBOSE) {
				System.out.println("Beginning triangle constraints.");
			}
			j = 0;
			for(int i = 1; i <= nVertices; i++) {
				for(int j1 = i + 1; j1 <= nVertices; j1++) {
					for(int k = j1 + 1; k <= nVertices; k++) {
						if(i != j1 && j1 != k && i != k) {
							colno[j] = getIndex(i, j1);
							row[j++] = 1;
							colno[j] = getIndex(j1, k);
							row[j++] = 1;
							colno[j] = getIndex(i, k);
							row[j++] = 1;
							lp.addConstraintex(j, row, colno, LpSolve.LE, 2);
							j = 0;
						}
					}
				}
			}
			// ---end no triangles constraint---

			// ---begin no squares constraint---
			if(VERBOSE) {
				System.out.println("Beginning square constraints.");
			}
			j = 0;
			for(int i = deltaL + deltaU; i <= nVertices; i++) {
				for(int j1 = i + 1; j1 <= nVertices; j1++) {
					for(int k = j1 + 1; k <= nVertices; k++) {
						for(int l = k + 1; l <= nVertices; l++) {
							if(i != j1 && i != k && i != l && j1 != k && j1 != l && k != l) {
								colno[j] = getIndex(i, j1);
								row[j++] = 1;
								colno[j] = getIndex(j1, k);
								row[j++] = 1;
								colno[j] = getIndex(k, l);
								row[j++] = 1;
								colno[j] = getIndex(l, i);
								row[j++] = 1;
								lp.addConstraintex(j, row, colno, LpSolve.LE, 3);
								j = 0;
							}
						}
					}
				}
			}
			// ---end no squares constraint---

			// ---begin orthogonal submatrix---
			if(VERBOSE) {
				System.out.println("Beginning orthogonal submatrix constraints.");
			}
			j = 0;
			for(int j0 = deltaL + deltaU + 1; j0 <= deltaU + deltaL * deltaL; j0 += deltaL) {
				for(int i = deltaU + 1; i <= deltaU + deltaL * (deltaL - 1); i += deltaL) {
					for(int k = i + deltaL; k <= deltaU + deltaL * deltaL; k += deltaL) {
						int[][] top = getSubMatrix(i, j0);
						int[][] bottom = getSubMatrix(k, j0);
						for(int m = 0; m < top.length; m++) {
							for(int n = 0; n < top[m].length; n++) {
								colno[j] = top[m][n];
								row[j++] = 1;
								colno[j] = bottom[m][n];
								row[j++] = 1;
								lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
								j = 0;
							}
						}
					}
				}
			}
			j = 0;
			for(int j0 = deltaL + deltaU + 1; j0 <= deltaU + deltaL * deltaL; j0 += deltaL) {
				for(int i = deltaU + 1; i <= deltaU + deltaL * (deltaL - 1); i += deltaL) {
					for(int k = i + deltaL; k <= deltaU + deltaL * deltaL; k += deltaL) {
						int[][] top = getSubMatrix(j0, i);
						int[][] bottom = getSubMatrix(j0, k);
						for(int m = 0; m < top.length; m++) {
							for(int n = 0; n < top[m].length; n++) {
								colno[j] = top[m][n];
								row[j++] = 1;
								colno[j] = bottom[m][n];
								row[j++] = 1;
								lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
								j = 0;
							}
						}
					}
				}
			}
			// ---end orthogonal submatrix---

			// ---begin correct row sum constraint---
			// if(VERBOSE) {
			// System.out.println("Beginning row sum constraints.");
			// }
			// j = 0;
			// for(int i = deltaU + 1; i <= nVertices; i++) {
			// for(int j1 = 1; j1 <= nVertices; j1++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// j = 0;
			// }
			// // first row
			// j = 0;
			// for(int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(1, i);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// // column between column 2 and column deltaU
			// j = 0;
			// for(int j1 = 2; j1 <= deltaU; j1++) {
			// for(int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// j = 0;
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaU);
			// }
			// ---end correct row sum constraint---

			if(VERBOSE) {
				System.out.println("Turning off build mode.");
			}
			// turn off build mode
			lp.setAddRowmode(false);

			if(VERBOSE) {
				System.out.println("Beginning objective function.");
			}
			// ---begin objective function---
			j = 0;
			colno[j] = 1; // first column
			row[j++] = 0; // value of first column

			// set the objective function in lpsolve
			lp.setObjFnex(j, row, colno);

			// set the object direction to maximise
			lp.setMaxim();
			// ---end objective function---

			if(VERBOSE) {
				System.out.println("Writing model to file.");
			}
			// write model to file
			lp.writeLp("modelEx.lp");

			// set message type
			lp.setVerbose(LpSolve.NORMAL);

			// ---begin solving---
			if(VERBOSE) {
				System.out.println("Presovling.");
			}
			lp.setPresolve(LpSolve.PRESOLVE_BOUNDS | LpSolve.PRESOLVE_COLS | LpSolve.PRESOLVE_ROWS, lp.getPresolveloops());

			if(VERBOSE) {
				System.out.println("Starting to sovle the model. This may take a while.");
			}
			ret = lp.solve();
			if(ret == LpSolve.OPTIMAL) {
				ret = 0;
			}
			else {
				ret = 5;
			}
			// ---end solving---
		}

		if(VERBOSE) {
			System.out.println("Beginning output.");
		}

		if(ret == 0) {
			lp.printObjective();
			lp.setOutputfile("ExSolution.dat");
			lp.printSolution(1);
			lp.printConstraints(1);
		}
		else {
			System.out.println("No solution found.");
		}
		// ---end output---

		return ret;
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(startTime));
		System.out.println("Start time: " + date);
		try {
			new ExGraphModel2().execute();
		}
		catch(LpSolveException e) {
		}
		long totalTime = (System.currentTimeMillis() - startTime);
		System.out.println("Total time: " + totalTime);
		dumpTime(totalTime);
	}
}
