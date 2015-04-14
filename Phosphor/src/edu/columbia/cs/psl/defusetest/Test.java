package edu.columbia.cs.psl.defusetest;


public class Test {
	public static void main(String[] args) {
		int a = 10;
		a = 20;
		int a2 = a;
		int a3 = a;
		//		while( a >  1){
		//			a2 = a;
		//			a = a-5;
		//		}
		a2 = a3;
		double b = 1.1;
		float f = 1.1f;
		long l = 1;
		//testmethod(a, b, f, l);
		testStatic(a);

		Test t= new Test();
		t.testField();


	}

	public static void testmethod(int a, double b, float f, long l){
		int i = a;
		double j = b;
		long lg = l;
		float ff = f;
	}

	static int istatic= 2;
	static double dstatic = 2.2;
	static float fstatic = 2.2f;
	static long lstatic = 2;
	static int istatic2;

	public static void testStatic(int a){
		int i = istatic;
		istatic2 = i;
		double d = dstatic;
		float f = fstatic;
		long l = lstatic;
		istatic = a;
	}

	int ifield= 3;
	double dfield = 3.3;
	float ffield = 3.3f;
	long lfield = 3;
	int ifield2;

	void testField(){
		int i = ifield;
		ifield2 = i;
		double d = dfield;
		float f = ffield;
		long l = lfield;
		l = lstatic;
		//System.out.println(l);
	}
}

