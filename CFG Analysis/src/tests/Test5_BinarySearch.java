package tests;

public class Test5_BinarySearch {
public void BinarySearch()
{  //BinarySearch x in sorted array A
	int[] A = {-3,0,1,4,6,7,9};
	int x = 7;
	int low = 0;
	int high = A.length-1;
	int mid = low;
	while(low<high)
	{
		mid = (low + high) / 2;
		if(A[mid]==x)
			break;
		else if(A[mid]<x)
			low = mid;
		else 
			high = mid;
	}
}
public static void main(String[] args){
	new Test5_BinarySearch().BinarySearch();
}
}
