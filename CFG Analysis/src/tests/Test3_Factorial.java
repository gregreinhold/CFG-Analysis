package tests;

public class Test3_Factorial {
public void Factorial()
{
	int N=10;
	int i=1;
	int Fac=1;
	for(i=N;i>1;i--)
		Fac = Fac * i;
	System.out.println(Fac);
}
public static void main(String[] args){
	new Test3_Factorial().Factorial();
}
}
