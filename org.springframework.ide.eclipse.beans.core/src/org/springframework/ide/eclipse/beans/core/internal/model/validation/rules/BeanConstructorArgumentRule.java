/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotationConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedRootBeanDefinition;
import org.springframework.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.core.type.asm.SimpleClassReaderFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;

/**
 * Validates a given {@link IBean}'s constructor argument. Skips abstract
 * beans.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanConstructorArgumentRule extends AbstractBeanValidationRule {

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof Bean && !((Bean) element).isAbstract());
	}

	@Override
	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		BeanDefinition bd = ((Bean) bean).getBeanDefinition();
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		// Validate merged constructor arguments in bean's class (child beans
		// not supported)
		String className = bd.getBeanClassName();
		if (className != null && !ValidationRuleUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean)
					.getProject(), className);
			if (type != null) {
				validateConstructorArguments(bean, type, mergedBd
						.getConstructorArgumentValues(), context);
			}
		}

		// If any constructor argument defined in bean the validate the merged
		// constructor arguments in merged bean's class (child beans fully
		// supported)
		if (!bd.getConstructorArgumentValues().isEmpty()) {
			String mergedClassName = mergedBd.getBeanClassName();
			if (mergedClassName != null
					&& !ValidationRuleUtils.hasPlaceHolder(mergedClassName)) {
				IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(
						bean).getProject(), mergedClassName);
				if (type != null) {
					validateConstructorArguments(bean, type, mergedBd
							.getConstructorArgumentValues(), context);
				}
			}
		}
	}

	protected void validateConstructorArguments(final IBean bean,
			final IType type, ConstructorArgumentValues argumentValues,
			final BeansValidationContext context) {

		// TODO CD need to add a check here if constructor has @Autowired
		// annotation; for now we just skip validation of
		// ScannedRootBeanDefinition

		// Skip validation if auto-wiring or a factory are involved
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		if (!(bd instanceof ScannedRootBeanDefinition)
				&& bd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO
				&& bd.getFactoryBeanName() == null
				&& bd.getFactoryMethodName() == null) {

			// Check for default constructor if no constructor arguments are
			// available
			final int numArguments = (argumentValues == null ? 0
					: argumentValues.getArgumentCount());
			try {
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element = BeansModelUtils
							.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}
					
					AnnotationMetadata metadata = getAnnotationMetadata(bean, type);
					// add check if prototype and configurable and if constructor
					// is autowired do this at the latest possible stage due to 
					// performance considerations
					if (!((bd.isPrototype() && metadata.hasConfigurableAnnotation()))
							|| (metadata.isConstructorAutowired() 
									&& checkIfAutowiredAnnotationPostProcessorIsRegistered(context))) {
						context.error(bean, "NO_CONSTRUCTOR",
								"No constructor with "
										+ numArguments
										+ (numArguments == 1 ? " argument"
												: " arguments")
										+ " defined in class '"
										+ type.getFullyQualifiedName() + "'");
					}
				}
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}
	
	/**
	 * Checks if a {@link AutowiredAnnotationBeanPostProcessor} has been registered
	 * in the {@link BeanDefinitionRegistry}.
	 */
	private boolean checkIfAutowiredAnnotationPostProcessorIsRegistered
		(BeansValidationContext context) {
		try {
			return context.getIncompleteRegistry().getBeanDefinition(
				AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME) != null;
		}
		catch (NoSuchBeanDefinitionException e) {
			return false;
		}
	}

	/**
	 * Retrieves a instance of {@link AnnotationMetadata} that contains information
	 * about used annotations in the class under question
	 */
	private AnnotationMetadata getAnnotationMetadata(
			final IBean bean, final IType type) {
		IJavaProject jp = JdtUtils.getJavaProject(bean.getElementResource()
				.getProject());
		final String className = type.getFullyQualifiedName();
		final AnnotationMetadata visitor = new AnnotationMetadata();
		if (jp != null) {
			try {
				JdtUtils.getProjectClassLoaderSupport(jp).executeCallback(
					new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

						public void doWithActiveProjectClassLoader()
								throws Throwable {
							ClassReaderFactory classReaderFactory = new SimpleClassReaderFactory();
							ClassReader classReader = classReaderFactory
									.getClassReader(className);
							classReader.accept(visitor, false);
						}
					});
			}
			catch (Throwable e) {
				BeansCorePlugin.log(e);
			}
		}
		return visitor;
	}

	/**
	 * ASM based visitor that checks the precedence of an {@link Autowired}
	 * annotation on <b>any</b> constructor.
	 */
	private static class AnnotationMetadata extends
			AnnotationMetadataReadingVisitor {

		private static final String CONSTRUCTOR_NAME = "<init>";

		private static final String AUTOWIRED_NAME = Type
				.getDescriptor(Autowired.class);

		private boolean isConstructorAutowired = false;

		@Override
		public MethodVisitor visitMethod(int modifier, String name,
				String params, String arg3, String[] arg4) {
			if (CONSTRUCTOR_NAME.equals(name)) {
				return new EmptyVisitor() {
					@Override
					public AnnotationVisitor visitAnnotation(final String desc,
							boolean visible) {
						if (AUTOWIRED_NAME.equals(desc)) {
							isConstructorAutowired = true;
						}
						return new EmptyVisitor();
					}
				};
			}
			return new EmptyVisitor();
		}

		public boolean isConstructorAutowired() {
			return isConstructorAutowired;
		}
		
		public boolean hasConfigurableAnnotation() {
			return super.hasAnnotation(Configurable.class.getName());
		}
	}
}
