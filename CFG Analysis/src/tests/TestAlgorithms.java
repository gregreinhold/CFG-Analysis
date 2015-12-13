package tests;

public class TestAlgorithms {
	public void TestFunction()
	{
		int i = 1;
		if (i == 1)
			System.out.println(i);
		for (i = 0; i < 10; i++)
		{
			if (i % 3 == 0)
				System.out.println(i);
		}
		i = 0;
		while (i < 20)
		{
			if (i % 2 == 0)
				System.out.println(i);
			if (i % 3 == 0)
				System.out.println(i);
			i++;
		}
	}
	public void BinarySearch(){
		
	}
    public void QuickSort(){
		
	}
    
    public void TestFunction9() {
        int i = 2;
        if (i == 1)
           System.out.println("First IF. i=  " + i); 
        i = 1;
        if (i == 2)        
           System.out.println("Second IF. i=  " + i);
      }

    
    public void TestFunction23() {
        int i = 1;
        while( i < 10)  {
                System.out.println("print i= " + i);
                i = i +1; 
         }
    }
    
    public void TestFunction32() {
        int i = 1;
        if ((i % 2) == 0 )
                  System.out.println("first if in while. i= " + i);
        else 
                  System.out.println("first else in while. i= " + i);     
        for (i = 2; i< 5; i++)
                  System.out.println("first else in while. i= " + i);     
        while( i < 10) {
                 System.out.println("while loop. i= " + i);
                  i = i +1; 
        }
    }

        
    public void TestFunction33() {
        int i =1;   
        for (i = 2; i< 5; i++)
                  System.out.println("print i= " + i);     
        while( i < 10)  {
                 System.out.println("print i= " + i);
                  i = i +1; 
        }
        if ((i % 2) == 0 )
                  System.out.println("print i= " + i);
        else 
                  System.out.println("print i= " + i);  
    }
    
    public void TestFunction35() {
        int i =1;   
        for (i = 2; i< 5; i++){
            System.out.println("print i= " + i);     
            System.out.println("print i= " + i);     
            System.out.println("print i= " + i);     
        }
        while( i < 10)  {
                 System.out.println("print i= " + i);
                  i = i +1; 
        }
        if ((i % 2) == 0 )
                  System.out.println("print i= " + i);
        else 
                  System.out.println("print i= " + i);  
    }

    public void TestFunction28() {
        int i = 1;
        while( i < 10) {
              if ((i % 2) == 0 )
                      System.out.println("first if in while. i= " + i);
              else 
                      System.out.println("1st else in while. i= "+i);
              if ((i % 3) == 0 )
                      System.out.println("2nd if in while. i= " + i);
              i = i +1; 
        }
    }

}
