package com.integratingfactor.idp.lib.overrides;

import javax.servlet.Filter;

/**
 * provides an interface to declare a custom authentication filter by an
 * application that will be configured by the security config durig
 * initialization
 * 
 * @author gnulib
 *
 */
public interface IdpAuthenticationFilter extends Filter {

    String getFilterUrl();

    Class<? extends Filter> getNextFilter();

}
