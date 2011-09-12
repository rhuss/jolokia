/*
 * Copyright 2011 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jolokia.request;

import java.util.*;
import javax.management.MalformedObjectNameException;

import org.jolokia.util.RequestType;
import org.json.simple.JSONObject;

/**
 * A JMX request for <code>read</code> operations, i.e. for reading JMX attributes
 * on MBeans.
 *
 * @author roland
 * @since 15.03.11
 */
public class JmxReadRequest extends JmxObjectNameRequest {

    // One or more attribute names
    private List<String> attributeNames;

    // Whether multiple attributes are to be fetched
    private boolean multiAttributeMode = false;

    /**
     * Constructor for GET requests
     *
     * @param pObjectName object name of MBean to read attributes from
     * @param pAttribute a single attribute to lookup. Can be null in which case all attributes
     *                   are to be fetched
     * @param pPathParts optional path parts from for filtering the return value
     * @param pInitParams optional processing parameters
     * @throws MalformedObjectNameException if the name is not a proper object name.
     */
    JmxReadRequest(String pObjectName,String pAttribute,List<String> pPathParts,
                   Map<String, String> pInitParams) throws MalformedObjectNameException {
        super(RequestType.READ, pObjectName, pPathParts, pInitParams);
        initAttribute(pAttribute);
    }

    /**
     * Constructor for creating a JmxRequest resulting from an HTTP POST request
     *
     * @param pRequestMap request in object format
     * @param pParams optional processing parameters
     * @throws MalformedObjectNameException if the object name extracted is not a proper object name.
     */
    JmxReadRequest(Map<String, ?> pRequestMap, Map<String, String> pParams) throws MalformedObjectNameException {
        super(pRequestMap, pParams);
        initAttribute(pRequestMap.get("attribute"));
    }

    /**
     * Get a single attribute name. If this request was initialized with more than one attribute this
     * methods throws an exception.
     * @return the attribute's name or null
     * @throws IllegalStateException if this request was initialized with more than one attribute.
     */
    public String getAttributeName() {
       if (attributeNames == null) {
           return null;
       }
        if (isMultiAttributeMode()) {
            throw new IllegalStateException("Request contains more than one attribute (attrs = " +
                    "" + attributeNames + "). Use getAttributeNames() instead.");
        }
        return attributeNames.get(0);
    }

    /**
     * Get the list of all attribute names.
     *
     * @return list of attributes names or null
     */
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    /**
     * Whether this is a multi-attribute request, i.e. whether it contains one ore more attributes to fetch
     * @return true if this is a multi attribute request, false otherwise.
     */
    public boolean isMultiAttributeMode() {
        return multiAttributeMode;
    }

    /**
     * Whether this request has no attribute names associated  (which normally means, that all attributes should be fetched).
     * @return true if no attribute name is stored.
     */
    public boolean hasAttribute() {
        return isMultiAttributeMode() || getAttributeName() != null;
    }

    /** {@inheritDoc} */
    public JSONObject toJSON() {
        JSONObject ret = super.toJSON();
        if (attributeNames != null && attributeNames.size() > 0) {
            if (attributeNames.size() > 1) {
                ret.put("attribute", attributeNames);
            } else {
                ret.put("attribute", attributeNames.get(0));
            }
        }
        if (getPathParts() != null && getPathParts().size() > 0) {
            ret.put("path", getPath());
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer("JmxReadRequest[");
        appendReadParameters(ret);
        String baseInfo = getInfo();
        if (baseInfo != null) {
            ret.append(", ").append(baseInfo);
        }
        ret.append("]");
        return ret.toString();
    }

    // =================================================================


    private void appendReadParameters(StringBuffer pRet) {
        if (attributeNames != null && attributeNames.size() > 1) {
            pRet.append("attribute=[");
            for (int i = 0;i<attributeNames.size();i++) {
                pRet.append(attributeNames.get(i));
                if (i < attributeNames.size() - 1) {
                    pRet.append(",");
                }
            }
            pRet.append("]");
        } else {
            pRet.append("attribute=").append(getAttributeName());
        }
    }

    // initialize and detect multi attribute mode
    private void initAttribute(Object pAttrval) {
        if (pAttrval != null) {
            if (pAttrval instanceof String) {
                attributeNames = Arrays.asList((String) pAttrval);
                multiAttributeMode = false;
            } else if (pAttrval instanceof Collection) {
                Collection<String> attributes = (Collection<String>) pAttrval;
                if (attributes.size() == 1 && attributes.iterator().next() == null) {
                    attributeNames = Arrays.asList((String) null);
                } else {
                    attributeNames = new ArrayList<String>(attributes);
                    multiAttributeMode = true;
                }
            }
        }
    }

}
