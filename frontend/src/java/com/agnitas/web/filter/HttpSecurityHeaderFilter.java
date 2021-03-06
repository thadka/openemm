/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;

/**
 * Filter implementing HTTP Security headers.
 *
 * Filter properties (for web.xml) are:
 * 
 * 
 * <table summary="Overview of filter properties">
 *   <tr>
 *     <th colspan="4">Common properties</th>
 *   </tr>
 *   <tr>
 *     <th>Property</th>
 *     <th>Description</th>
 *     <th>Values</th>
 *     <th>Default</th>
 *   </tr>
 *   <tr>
 *     <td>${header}.enable</td>
 *     <td>Enables security header</td>
 *     <td>true, false</td>
 *     <td>{@value #COMMON_ENABLE_DEFAULT}</td>
 *   </tr>
 *   <tr>
 *     <td>${header}.overwrite</td>
 *     <td>Overwrite existing header</td>
 *     <td>true, false</td>
 *     <td>{@value #COMMON_OVERWRITE_DEFAULT}</td>
 *   </tr>
 *   <tr>
 *     <th colspan="4">HTTP Strict Transport Security (HSTS)</th>
 *   </tr>
 *   <tr>
 *     <td>hsts.magAge</td>
 *     <td>Period of time (in seconds) of advising browser to use HTTPS connections</td>
 *     <td>positive integer</td>
 *     <td>{@value #HSTS_MAXAGE_DEFAULT} seconds</td>
 *   </tr>
 *   <tr>
 *     <td>hsts.includeSubdomains</td>
 *     <td>Flag controlling if the browser should use HTTPS for sub-domains, too.</td>
 *     <td>true, false</td>
 *     <td>{@value #HSTS_INCLUDE_SUBDOMAINS_DEFAULT}</td>
 *   </tr>
 * </table>
 * 
 */
public final class HttpSecurityHeaderFilter implements Filter {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(HttpSecurityHeaderFilter.class);

	public static final String HSTS_HEADER_NAME = "Strict-Transport-Security";

	public static final String HSTS_ENABLE_PARAMETER_NAME = "hsts.enable";
	public static final String HSTS_OVERWRITE_PARAMETER_NAME = "hsts.overwrite";
	public static final String HSTS_MAXAGE_PARAMETER_NAME = "hsts.maxAge";
	public static final String HSTS_INCLUDE_SUBDOMAINS_PARAMETER_NAME = "hsts.includeSubdomains";
	
	public static final boolean COMMON_ENABLE_DEFAULT = false;
	public static final boolean COMMON_OVERWRITE_DEFAULT = false;
	
	
	public static final int HSTS_MAXAGE_DEFAULT = 86400;			// Default: 1 day
	public static final boolean HSTS_INCLUDE_SUBDOMAINS_DEFAULT = true;
	
	
	private boolean hstsEnabled = COMMON_ENABLE_DEFAULT;
	private boolean hstsOverwrite = COMMON_OVERWRITE_DEFAULT;
	private int hstsMaxAge = HSTS_MAXAGE_DEFAULT;
	private boolean hstsIncludeSubdomains = HSTS_INCLUDE_SUBDOMAINS_DEFAULT;

	@Override
	public final void destroy() {
		// Nothing to do here
	}

	@Override
	public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		final HttpServletResponseWrapper httpResponse = new HttpServletResponseWrapper((HttpServletResponse) response);

		insertHttpSecurityHeader(httpResponse);

		filterChain.doFilter(request, httpResponse);
	}
	
	private final void insertHttpSecurityHeader(final HttpServletResponse response) {
		if(hstsEnabled) {
			insertHstsHeader(response);
		}
	}

	private final void insertHstsHeader(final HttpServletResponse response) {
		if(hstsOverwrite || response.getHeader(HSTS_HEADER_NAME) == null) {
			response.addHeader(HSTS_HEADER_NAME, 
					String.format("max-age=%d%s", 
							hstsMaxAge,
							hstsIncludeSubdomains ? "; includeSubDomains" : ""
							));
		}
	}
	
	@Override
	public final void init(final FilterConfig config) throws ServletException {
		hstsEnabled = getBooleanInitParam(config, HSTS_ENABLE_PARAMETER_NAME, COMMON_ENABLE_DEFAULT);
		hstsOverwrite = getBooleanInitParam(config, HSTS_OVERWRITE_PARAMETER_NAME, COMMON_OVERWRITE_DEFAULT);
		hstsMaxAge = getIntInitParam(config, HSTS_MAXAGE_PARAMETER_NAME, 86400);
		hstsIncludeSubdomains = getBooleanInitParam(config, HSTS_INCLUDE_SUBDOMAINS_PARAMETER_NAME, HSTS_INCLUDE_SUBDOMAINS_DEFAULT);
	}
	
	private static final boolean getBooleanInitParam(final FilterConfig config, final String name, final boolean defaultValue) {
		final String value = config.getInitParameter(name);
		
		if(value == null) {
			return defaultValue;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch(final Exception e) {
				return defaultValue;
			}
		}
	}
	
	private static final int getIntInitParam(final FilterConfig config, final String name, final int defaultValue) {
		final String value = config.getInitParameter(name);
		
		if(value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch(final Exception e) {
				return defaultValue;
			}
		}
	}

}
