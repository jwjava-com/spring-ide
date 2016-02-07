/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug;

import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * This class is responsible for managing the debug strategies. It is also responsible for managing the life
 * cycle of process listeners abd other {@link Disposable}s that may be associated with a debug strategy and
 * should be disposed when the manager itself is disposed.
 * <p>
 *
 * @author Kris De Volder
 */
public class DebugStrategyManager implements Disposable {

	// At the moment there's only one supported debug strategy, in the future there may be more than one.
	// In that case this class may need be generalized to manage a collection of strategies somehow.

	private DebugSupport strategy;
	private Disposable processTracker;

	public DebugStrategyManager(DebugSupport strategy, BootDashViewModel viewModel) {
		this.strategy = strategy;
		this.processTracker = strategy.createProcessTracker(viewModel);
	}

	@Override
	public void dispose() {
		if (this.processTracker!=null) {
			this.processTracker.dispose();
			this.processTracker = null;
		}
	}

	public DebugSupport getStrategy() {
		return strategy;
	}

}
