/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.QuickfixContext;

public interface FixableProblem {
	List<ICompletionProposal> getQuickfixes(QuickfixContext context);
}
