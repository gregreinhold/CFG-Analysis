package tests;

public class Test1_MaxMin {
	public void Maxmin()
	{// Find the max and min value of array A
		int[] A = {0,5,2,3,4,9,-1,6,7,-2};
		int max = A[0];
		int min = A[0];
        int i = 0;
		for (i = 0; i < A.length; i++)
		{
			if (A[i] > max) 
				max = A[i];
			if (A[i] < min) 
				min = A[i];
		}
	}
	public static void main(String[] args){
		new Test1_MaxMin().Maxmin();
	}
}
