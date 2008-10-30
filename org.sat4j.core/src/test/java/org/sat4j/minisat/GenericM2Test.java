/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/

package org.sat4j.minisat;

import junit.framework.TestSuite;

import org.sat4j.specs.ISolver;

/**
 * @author leberre
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class GenericM2Test extends AbstractM2Test<ISolver> {

	private String solvername;

	private static final SolverFactory factory = SolverFactory.instance();

	/**
	 * @param arg0
	 */
	public GenericM2Test(String arg0) {
		setName("AbstractM2Test" + arg0);
		solvername = arg0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected ISolver createSolver() {
		return factory.createSolverByName(solvername);
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		String[] names = factory.solverNames();
		String name;
		for (int i = 0; i < names.length; i++) {
			name = names[i];
			if (!"Mini3SAT".equals(name) && !"DimacsOutput".equals(name))
				suite.addTest(new GenericM2Test(name));
		}
		return suite;
	}

	@Override
	protected void runTest() throws Throwable {
		assertFalse(solveInstance(PREFIX + "pigeons/hole6.cnf"));
	}
}
