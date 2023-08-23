/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
//		存放 不是 api 提供的 且已经处理完的 BeanFactoryPostProcessor 和 BeanDefinitionRegistryPostProcessor 的对象名称，防止重复执行
//		因为 api 提供的不会出现重复执行的情况，除非是你写的代码有问题
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
//			为了动态注册 bean，所以用 BeanDefinitionRegistry
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//			存放直接实现父类 bean
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
//			存放直接实现子类 bean
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
//			当前 beanFactoryPostProcessors 里面都是通过 api 添加进来的 直接实现了 父类或子类 的 bean
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
//					如果是直接实现子类的，强转后直接执行子类方法，并把 bean 加到 子类集合
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
//					直接加到父类集合
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
//			currentRegistryProcessors 存储了当前要执行的 直接实现了子类的 bean
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
//			根据类型到 BeanDefinitionMap 当中找到所有直接实现了子类的集合，并返回对应的名字集合
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
//					如果有实现 PriorityOrdered 排序接口，先实例化并优先添加到 当前要执行的直接实现子类的集合中
//					getBean：先从单例池里面找，能找到就返回，找不到就实例化，当前这里肯定找不到，直接实例化
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
//					把当前 bean 名称添加到已执行的集合中，防止重复执行
					processedBeans.add(ppName);
				}
//				没有实现 PriorityOrdered 接口的表示优先级低，先不用执行
			}
//			排序，当前 currentRegistryProcessors 里面都是实现了子类 且 有排序接口的，先排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
//			合并到子类集合，方便后续遍历执行父类方法
			registryProcessors.addAll(currentRegistryProcessors);
//			遍历 当前要执行的子类集合，并执行子类的子类方法，这里是又可能往里面加入新的 BeanDefinition，所以后面会继续重复在 BeanDefinitionMap 找
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
//			当前要执行的子类集合已经把子类方法都执行完了，清空当前集合
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
//			第二次 根据类型到 BeanDefinitionMap 当中找到所有直接实现了子类的集合，并返回对应的名字集合
//			这里主要是为了找到实现了 Ordered 接口的/以及在上一步的 BeanDefinitionRegistryPostProcessors 中又注册了新的 BeanDefinition
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
//					如果没有执行过，且 有实现 Ordered 排序接口的 子类，加到要执行的集合，标记已执行
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
//			这里的要执行的集合中都是实现了 Ordered 接口的子类，先排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
//			合并到子类集合，方便后续遍历执行父类方法
			registryProcessors.addAll(currentRegistryProcessors);
//			执行这一批实现了 Ordered 接口的 子类的子类方法
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
//			执行完子类方法，清空要执行集合
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				//如果这一回没有找到未执行过的 直接实现子类的才会跳出循环
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
//						只要是未执行过的 子类，都进入这里
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName); // 标记已执行
						reiterate = true;
//						这里标记找到了未执行过的的子类，下面将会执行子类的子类方法，
//						担心子类方法又往 BeanDefinitionMap 里面注册了新的 BeanDefinition，所以要继续循环
					}
				}
//				要执行的子类集合排序
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors); // 合并，方便后面遍历执行父类方法
//				执行子类方法
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear(); // 清空要执行列表
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
//			遍历所有子类，执行他们的父类方法
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
//			遍历所有通过 api 方式加进来的父类，执行他们的父类方法
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
//			除非实在 Spring boot 或者你自己拓展了 Spring，要不然很少能进到这里
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
//		找父类，上面 if 里面的 regularPostProcessors 只包含了所有通过 api 方式加进来的父类，
//		所以这里要从 BeanDefinitionMap 里面找直接实现父类的 bean name 集合
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
//		用三个集合分别存 实现 PriorityOrdered/Ordered/没实现排序接口 的父类，注意后两个只是放 name，不像第一个直接放 bean
//		注意，找父类不会像找 子类一样循环遍历，
//		所以尽量不要在 父类 里面增加新的 BeanDefinition，要不然缺了像子类这个循环遍历，会导致这个 bean 的功能不齐全
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
//				直接跳过以及执行的，比如上面通过 api 加入进来的 直接实现父类的 bean
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
//				PriorityOrdered 的优先级最高，这里是直接放 bean 的
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
//				Ordered 次之，这里只是放 name，还没有实例化 bean
				orderedPostProcessorNames.add(ppName);
			}
			else {
//				没实现排序的，这里只是放 name，还没有实例化 bean
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
//		实现了 PriorityOrdered 的 父类优先级最高，先排序，然后再执行父类方法
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
//		Ordered 的优先级次之，所以这里才执行
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
//		没有实现排序接口的 最后才执行
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
//		从 BeanDefinitionMap 里面找实现 BeanPostProcessor 的 bean name 集合
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
//		beanProcessorTargetCount： spring 期望一个 bean 应该执行几个 BeanPostProcessor 的数量
//		来源1：beanPostProcessors.size()：spring内部自己 add 到这个list里面的；来源2：扫描 bdMap getBean 实例化的实现 BeanPostProcessor 的 bean数量
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
//		这里是把 BeanPostProcessorChecker 也加进去，所以要 +1；BeanPostProcessorChecker 的作用是检查一个 bean 即将执行的 BeanPostProcessor 数量，如果 执行数量 < beanProcessorTargetCount 期望数量，就打印日志
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
//		在 add BeanPostProcessorChecker 之后，在 beanPostProcessors 没有添加到预期的所有对象之前，就有一个 bean 开始走生命周期了，那就会出现预期和实际不一致的情况

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>(); // 存放了实现 PriorityOrdered 接口的 BeanPostProcessor
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>(); // 存放了实现 PriorityOrdered 和 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>(); // 存放了实现 Ordered 接口的 BeanPostProcessor 的 bean name
		List<String> nonOrderedPostProcessorNames = new ArrayList<>(); // 存放了没有实现排序接口的 BeanPostProcessor 的 bean name
		for (String ppName : postProcessorNames) {
			// 如果是实现了 PriorityOrdered 接口的 BeanPostProcessor，就放到 priorityOrderedPostProcessors 里面
//			例如 AutowiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor 这种实现了 PriorityOrdered 接口的
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
//				getBean 直接实例化了，不会走到上面的 BeanPostProcessorChecker
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
//				如果还是实现了 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor，就放到 internalPostProcessors 里面
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
//				如果是实现了 Ordered 接口的 BeanPostProcessor，就把 bean name 放到 orderedPostProcessorNames 里面
				orderedPostProcessorNames.add(ppName);
			}
			else {
//				如果没有实现排序接口的 BeanPostProcessor，就把 bean name 放到 nonOrderedPostProcessorNames 里面
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory); // 对实现 PriorityOrdered 接口的 BeanPostProcessor 进行排序
//		注册实现 PriorityOrdered 接口的 BeanPostProcessor 到 List<BeanPostProcessor> beanPostProcessors 里面
//		因为上面遍历的时候，已经把实现 PriorityOrdered 接口的 BeanPostProcessor 实例化了，
//		注意：上面是实例化，不是注册，所以如果实现了 PriorityOrdered 接口的 BeanPostProcessor 里面有依赖其他 bean，是不会被注入的，因为还没有注册到 beanPostProcessors 里面
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

//		而下面的实现 Ordered 接口的 BeanPostProcessor 和没有实现排序接口的 BeanPostProcessor，因为上面已经注册好 AutowiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor，所以可以注入其他 bean

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
//			实例化实现 Ordered 接口的 BeanPostProcessor，例如 AnnotationAwareAspectJAutoProxyCreator 这种
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
//				如果还是实现了 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor，就放到 internalPostProcessors 里面
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory); // 对实现 Ordered 接口的 BeanPostProcessor 进行排序
//		注册实现 Ordered 接口的 BeanPostProcessor 到 List<BeanPostProcessor> beanPostProcessors 里面
//		因为 AnnotationAwareAspectJAutoProxyCreator 在上面只是实例化了，没有注册，所以在另一个实现了 Ordered 接口的 BeanPostProcessor 里面注入其他 bean，这个 bean 的 aop 是不会生效的，
//			因为 AnnotationAwareAspectJAutoProxyCreator 还没有注册到 beanPostProcessors 里面
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

//		而下面这种没有实现排序接口的 BeanPostProcessor，因为上面已经注册好 AnnotationAwareAspectJAutoProxyCreator，所以注入其他 bean 的 aop 是会生效的

//		所以为什么 Ordered 和 没有实现排序接口的 list 装的是 bean name，而不像 PriorityOrdered 一样装的是实例化的 BeanPostProcessor？
//		就是因为 优先级高的先完成注册，会有一些额外的功能(自动注入和aop)，然后再注册优先级低的，就能使用到优先级高的提供的功能了

		// Now, register all regular BeanPostProcessors. 注册所有常规的 BeanPostProcessors, 即没有实现排序接口的 BeanPostProcessor
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
//			实例化没有实现排序接口的 BeanPostProcessor
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
//				如果还是实现了 MergedBeanDefinitionPostProcessor 接口的 BeanPostProcessor，就放到 internalPostProcessors 里面
				internalPostProcessors.add(pp);
			}
		}
//		注册没有实现排序接口的 BeanPostProcessor 到 List<BeanPostProcessor> beanPostProcessors 里面
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// 重新注册用于检测内部 bean 作为 ApplicationListeners 的后处理器，将其移动到处理器链的末尾（以便捕获代理等）。覆盖了之前的 ApplicationListenerDetector
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
//				检查一个 bean 即将执行的 BeanPostProcessor 数量，如果 执行数量 < beanProcessorTargetCount 期望数量，就打印日志
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
