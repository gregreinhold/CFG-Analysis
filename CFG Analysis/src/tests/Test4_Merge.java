package tests;

public class Test4_Merge {
public void Merge()
{	//merge two sorted array A and B to C
	int[] A = {-2,1,6,7};
	int[] B = {-3,2,3,5,8,9};
	int m = A.length;
	int n = B.length;
	int[] C = new int[m+n];
	int i = 0;
	int j = 0;
	int k = 0;
	for(k=0;k<C.length;k++)
	{
		if(i<m && j<n && A[i]<B[j])
		{ 
			C[k]=A[i];
			i++;
		}
		else if(i<m && j<n)
		{
			C[k]=B[j];
			j++;
		}
		else if(i<m)
		{
			while(i<m)
				C[k++]=A[i++];
		}
		else if(j<n)
		{
			while(j<n)
				C[k++]=B[j++];
		}
	}
}
public static void main(String[] args){
	new Test4_Merge().Merge();
}
}
