package com.cpsc310.treespotter.shared;

/**
 * @author virgo47 from StackOverflow<br>
 * 
 *  Aleksy shamelessly C&Ped this from
 *  http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method
 * 
 *	gets the name of the current method in a sort-of robust way 
 */
public class MethodNameTest {
	private static final int CLIENT_CODE_STACK_INDEX;

	static {
		// Finds out the index of "this code" in the returned stack trace -
		// funny but it differs in JDK 1.5 and 1.6
		int i = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			i++;
			if (ste.getClassName().equals(MethodNameTest.class.getName())) {
				break;
			}
		}
		CLIENT_CODE_STACK_INDEX = i;
	}

	public static void main(String[] args) {
		System.out.println("methodName() = " + methodName());
		System.out.println("CLIENT_CODE_STACK_INDEX = "
				+ CLIENT_CODE_STACK_INDEX);
	}

	public static String methodName() {
		return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
	}
}