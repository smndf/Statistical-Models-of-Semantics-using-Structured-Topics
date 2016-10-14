package reader;

import mcl.MarkovClustering;
import mcl.MarkovClusteringSparse;
import mcl.Matrix;
import mcl.SparseMatrix;

import org.junit.Test;
public class TestMCL {


	@Test
	public void testRun() {
		
		double[][] matrix = {{1, 0, 0, 1}, {0, 1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}};

		SparseMatrix m2 = new SparseMatrix(matrix);
		MarkovClusteringSparse mc = new MarkovClusteringSparse();
		SparseMatrix res = mc.run(m2, 0, 2.0, 1.0, 0.00000001);
		double[][] r = res.getDense();
		for (int i = 0; i<r.length;i++){
			for (int j=0; j<r[0].length;j++){
				System.out.print(r[i][j] + " ");
			}			
			System.out.println();
		}
		
		/*
		Matrix m = new Matrix(matrix);
		MarkovClustering mc = new MarkovClustering();
		Matrix res = mc.run(m, 0, 2.0, 1.0, 0.00000001);
		System.out.println(res);
		*/
	}	


}
