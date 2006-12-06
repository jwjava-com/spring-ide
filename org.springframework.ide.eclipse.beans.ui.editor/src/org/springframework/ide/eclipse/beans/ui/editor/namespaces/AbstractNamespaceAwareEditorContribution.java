/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.LabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.INamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.IReferenceableNodesLocator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;

public abstract class AbstractNamespaceAwareEditorContribution implements
        INamespaceAwareEditorContribution {

    private IReferenceableNodesLocator referenceableElemenLocator;

    private LabelProvider labelProvider;

    private INamespaceContentAssistProcessor contentAssistProcessor;

    private IHyperlinkDetector hyperlinkDetector;

    public final INamespaceContentAssistProcessor getContentAssistProcessor() {
        if (this.contentAssistProcessor == null) {
            this.contentAssistProcessor = createNamespaceContentAssistProcessor();
        }
        return this.contentAssistProcessor;
    }

    protected abstract INamespaceContentAssistProcessor createNamespaceContentAssistProcessor();

    public final IHyperlinkDetector getHyperLinkDetector() {
        if (this.hyperlinkDetector == null) {
            this.hyperlinkDetector = createHyperlinkDetector();
        }
        return this.hyperlinkDetector;
    }

    protected abstract IHyperlinkDetector createHyperlinkDetector();

    public final LabelProvider getLabelProvider(
            BeansContentOutlineConfiguration configuration) {
        if (this.labelProvider == null) {
            this.labelProvider = createLabelProvider(configuration);
        }
        return this.labelProvider;
    }

    protected abstract LabelProvider createLabelProvider(
            BeansContentOutlineConfiguration configuration);

    public final IReferenceableNodesLocator getReferenceableElementsLocator() {
        if (this.referenceableElemenLocator == null) {
            this.referenceableElemenLocator = createReferenceableNodesLocator();
        }
        return this.referenceableElemenLocator;
    }

    protected IReferenceableNodesLocator createReferenceableNodesLocator() {
        return new DefaultReferenceableNodesLocator(this);
    }
}
