package treeminer;

public class test {

		abstract class A <T> {
			
		}
		
		class B extends A<C> {
			
		}
		
		class D extends A<E> {
			
		}
		
		class C {
			
		}
		
		class E extends C {
			
		}
		

		void main() {
			A<C> var = new B();
			A<E> var2 = new D();
		}
}
