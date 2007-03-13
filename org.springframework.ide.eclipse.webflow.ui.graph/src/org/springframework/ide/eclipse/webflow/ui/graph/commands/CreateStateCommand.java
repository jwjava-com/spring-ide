/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class CreateStateCommand extends Command {

	/**
	 * 
	 */
	private IState child;

	/**
	 * 
	 */
	private int index = -1;

	/**
	 * 
	 */
	private IWebflowState parent;

	/**
	 * 
	 */
	private List sourceConnections = new ArrayList();

	/**
	 * 
	 */
	private List targetConnections = new ArrayList();

	/**
	 * 
	 */
	private int result;

	private void deleteConnections(IState a) {
		if (a instanceof ITransitionableTo)
			targetConnections.addAll(((ITransitionableTo) a)
					.getInputTransitions());
		for (int i = 0; i < targetConnections.size(); i++) {
			if (targetConnections.get(i) instanceof IStateTransition) {
				IStateTransition t = (IStateTransition) targetConnections
						.get(i);
				t.setToState(null);
				t.getFromState().fireStructureChange(
						WebflowModelElement.OUTPUTS, t);
			}
			if (targetConnections.get(i) instanceof IIfTransition) {
				IIfTransition t = (IIfTransition) targetConnections.get(i);
				t.setToState(null);
				t.getElementParent().fireStructureChange(
						WebflowModelElement.OUTPUTS, t);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// create xml element
		child.createNew(parent);
		result = DialogUtils.openPropertiesDialog(parent, child, true);
		if (result != Dialog.OK) {
			return;
		}
		if (index > 0) {
			parent.addState(child, index);
		}
		else {
			parent.addState(child);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		if (result != Dialog.OK) {
			return;
		}
		if (index > 0)
			parent.addState(child, index);
		else
			parent.addState(child);
	}

	/**
	 * @param activity
	 */
	public void setChild(IState activity) {
		child = activity;
	}

	/**
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * @param sa
	 */
	public void setParent(IWebflowState sa) {
		parent = sa;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		parent.removeState(child);
		deleteConnections(child);
	}
}