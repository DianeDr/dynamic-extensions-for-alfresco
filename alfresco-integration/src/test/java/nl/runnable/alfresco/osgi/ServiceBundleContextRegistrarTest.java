/*
Copyright (c) 2012, Runnable
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of Runnable nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nl.runnable.alfresco.osgi;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.descriptor.DescriptorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.context.ApplicationContext;

/**
 * {@link ServiceBundleContextRegistrar} unit test.
 * 
 * @author Laurens Fridael
 * 
 */
public class ServiceBundleContextRegistrarTest {

	private static class ServiceRegistrationAnswer implements Answer<ServiceRegistration<?>> {

		private final ServiceRegistration<?> serviceRegistration;

		private ServiceRegistrationAnswer(final ServiceRegistration<?> serviceRegistration) {
			this.serviceRegistration = serviceRegistration;
		}

		@Override
		public ServiceRegistration<?> answer(final InvocationOnMock invocation) throws Throwable {
			return serviceRegistration;
		}
	}

	private static class ExampleServicePropertiesProvider implements ServicePropertiesProvider {

		private final Map<String, Object> properties = new HashMap<String, Object>();

		private ExampleServicePropertiesProvider() {
			properties.put("qualifier", "example");
		}

		@Override
		public Map<String, Object> getServiceProperties(final Object service, final List<String> serviceNames) {
			return properties;
		}
	}

	private static class IsServicePropertiesThatContainsProperty extends ArgumentMatcher<Dictionary<String, Object>> {

		private final String name;

		private final String value;

		private IsServicePropertiesThatContainsProperty(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(final Object argument) {
			final Dictionary<String, Object> properties = (Dictionary<String, Object>) argument;
			return value.equals(properties.get(name));
		}
	}

	private static final ExampleService EXAMPLE_SERVICE = new ExampleServiceImpl();

	private static final String EXAMPLE_SERVICE_BEAN_NAME = "testService1";

	private static final String EXAMPLE_SERVICE_NAME = "nl.runnable.alfresco.osgi.ExampleService";

	private ServiceBundleContextRegistrar serviceBundleContextRegistrar;

	private ServiceDefinition serviceDefinition;

	@Before
	public void setup() {
		serviceBundleContextRegistrar = new ServiceBundleContextRegistrar();
		serviceBundleContextRegistrar.setServicePropertiesProviders(Arrays
				.<ServicePropertiesProvider> asList(new ExampleServicePropertiesProvider()));
		serviceDefinition = new ServiceDefinition(EXAMPLE_SERVICE_BEAN_NAME, EXAMPLE_SERVICE_NAME);
		serviceBundleContextRegistrar.setServiceDefinitions(Arrays.<ServiceDefinition> asList(serviceDefinition));
		serviceBundleContextRegistrar.setApplicationContext(createMockApplicationContext());
		serviceBundleContextRegistrar.setDescriptorService(mock(DescriptorService.class));
	}

	protected ApplicationContext createMockApplicationContext() {
		final ApplicationContext applicationContext = mock(ApplicationContext.class);
		when(applicationContext.containsBean(EXAMPLE_SERVICE_BEAN_NAME)).thenReturn(true);
		when(applicationContext.isSingleton(EXAMPLE_SERVICE_BEAN_NAME)).thenReturn(true);
		when(applicationContext.getBean(EXAMPLE_SERVICE_BEAN_NAME)).thenReturn(EXAMPLE_SERVICE);
		return applicationContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRegisterInBundleContextAndUnregisterFromBundleContext() {
		final BundleContext bundleContext = mock(BundleContext.class);
		final ServiceRegistration<?> serviceRegistration = mock(ServiceRegistration.class);
		final ServiceReference serviceReference = mock(ServiceReference.class);
		when(serviceReference.getProperty(anyString())).thenReturn(EXAMPLE_SERVICE_NAME);
		when(serviceRegistration.getReference()).thenReturn(serviceReference);
		when(bundleContext.registerService(any(String[].class), anyObject(), any(Dictionary.class))).thenAnswer(
				new ServiceRegistrationAnswer(serviceRegistration));
		final List<ServiceRegistration<?>> serviceRegistrations = serviceBundleContextRegistrar
				.registerInBundleContext(bundleContext);
		assertSame(serviceRegistration, serviceRegistrations.get(0));
		verify(bundleContext).registerService(eq(new String[] { EXAMPLE_SERVICE_NAME }), eq(EXAMPLE_SERVICE),
				argThat(new IsServicePropertiesThatContainsProperty("qualifier", "example")));

		serviceBundleContextRegistrar.unregisterFromBundleContext();
		verify(serviceRegistration).unregister();
	}
}
