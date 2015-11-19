package tests;

public class Test2_BubbleSort {
public void BubbleSort()
{   // BubbleSort array A
	int[] A = {-1,4,7,9,0,2,3,-5,6,8};
	boolean swap = true;
	int start=0;
	int i=0;
	int temp;
	while(swap==true){
		swap=false;
		for(i=A.length-1;i>start;i--)
			if(A[i]<A[i-1])
			{
			   temp=A[i];
			   A[i]=A[i-1];
			   A[i-1]=temp;
			   swap=true;
			}
		start++;
	}
}
public static void main(String[] args){
	new Test2_BubbleSort().BubbleSort();
}
}
