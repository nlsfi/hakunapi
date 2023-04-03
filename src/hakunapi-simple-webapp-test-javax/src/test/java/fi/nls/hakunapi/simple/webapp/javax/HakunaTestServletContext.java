package fi.nls.hakunapi.simple.webapp.javax;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class HakunaTestServletContext implements ServletContext {
	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(Collections.EMPTY_LIST);
	}

	@Override
	public String getServletContextName() {
		return "hakuna";
	}

	@Override
	public String getContextPath() {
		return "/hakuna";
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return false;
	}

	protected Map<String, Object> attributes = new HashMap<>();

	@Override
	public void setAttribute(String name, Object attribute) {
		attributes.put(name, attribute);
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	class AttributesEnum implements Enumeration<String> {
		private Iterator<String> iterator;

		public AttributesEnum(Iterator<String> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		@Override
		public String nextElement() {
			return iterator.next();
		}

	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new AttributesEnum(attributes.keySet().iterator());
	}

	@Override
	public ServletContext getContext(String uripath) {

		return null;
	}

	@Override
	public int getMajorVersion() {

		return 0;
	}

	@Override
	public int getMinorVersion() {

		return 0;
	}

	@Override
	public int getEffectiveMajorVersion() {

		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() {

		return 0;
	}

	@Override
	public String getMimeType(String file) {

		return null;
	}

	@Override
	public Set<String> getResourcePaths(String path) {

		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {

		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {

		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {

		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {

		return null;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {

		return null;
	}

	@Override
	public Enumeration<Servlet> getServlets() {

		return null;
	}

	@Override
	public Enumeration<String> getServletNames() {

		return null;
	}

	@Override
	public void log(String msg) {

	}

	@Override
	public void log(Exception exception, String msg) {

	}

	@Override
	public void log(String message, Throwable throwable) {

	}

	@Override
	public String getRealPath(String path) {

		return null;
	}

	@Override
	public String getServerInfo() {

		return null;
	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public Dynamic addServlet(String servletName, String className) {

		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {

		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {

		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {

		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {

		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {

		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {

		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {

		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {

		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {

		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {

		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {

		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {

		return null;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {

		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {

		return null;
	}

	@Override
	public void addListener(String className) {

	}

	@Override
	public <T extends EventListener> void addListener(T t) {

	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {

	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {

		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {

		return null;
	}

	@Override
	public ClassLoader getClassLoader() {

		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {

	}

}
