/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.http.transformers;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mule.config.MuleProperties;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.http.HttpConstants;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.Utility;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <code>ObjectToHttpClientMethodRequest</code> transforms a UMOMessage into
 * a HttpClient HttpMethod that represents an HttpRequest.
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */

public class ObjectToHttpClientMethodRequest extends AbstractEventAwareTransformer
{
    private List headers = null;

    public ObjectToHttpClientMethodRequest()
    {
        registerSourceType(String.class);
        setReturnClass(HttpMethod.class);
        headers = new ArrayList(Arrays.asList(HttpConstants.REQUEST_HEADER_NAMES));
    }

    public Object transform(Object src, UMOEventContext context) throws TransformerException
    {
        String endpoint = (String) context.getProperty(MuleProperties.MULE_ENDPOINT_PROPERTY, null);
        if(endpoint==null) {
            throw new TransformerException("Endpoint property is not set, this transformer should not be used before a dispatcher has been invoked");
        }
        String method = (String) context.getProperty(HttpConnector.HTTP_METHOD_PROPERTY, "POST");

        try
        {
            URI uri = new URI(endpoint);
            HttpMethod httpMethod = null;

            if (HttpConstants.METHOD_GET.equals(method))
            {
                httpMethod = new GetMethod(uri.toString());
                String paramName = (String) context.getProperty(
                        HttpConnector.HTTP_GET_BODY_PARAM_PROPERTY,
                        HttpConnector.DEFAULT_HTTP_GET_BODY_PARAM_PROPERTY);

                String query = uri.getQuery();
                if(query==null) {
                    query = paramName + "=" + paramName;
                } else {
                    query += "&" + paramName + "=" + paramName;
                }
                httpMethod.setQueryString(query);

            } else
            {
                PostMethod postMethod = new PostMethod(uri.toString());
                if(src instanceof String) {
                    postMethod.setRequestBody(src.toString());
                    postMethod.setRequestContentLength(src.toString().length());
                } else {
                    byte[] buffer = Utility.objectToByteArray(src);
                    postMethod.setRequestBody(new ByteArrayInputStream(buffer));
                    postMethod.setRequestContentLength(buffer.length);
                }
                httpMethod = postMethod;
            }
            //Standard headers
            String headerName;
            String value;
            for (Iterator iterator = headers.iterator(); iterator.hasNext();)
            {
                headerName = (String) iterator.next();
                value = (String)context.getProperty(headerName);
                if(value!=null) {
                    httpMethod.addRequestHeader(headerName, value);
                }
            }

            //Custom headers
            Map customHeaders = (Map)context.getProperty(HttpConnector.HTTP_CUSTOM_HEADERS_MAP_PROPERTY);
            if(customHeaders!=null) {
                Map.Entry entry;
                for (Iterator iterator = customHeaders.entrySet().iterator(); iterator.hasNext();)
                {
                    entry =  (Map.Entry)iterator.next();
                    httpMethod.addRequestHeader(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            //Mule properties
            UMOMessage m = context.getMessage();
            String user = (String)m.getProperty(MuleProperties.MULE_USER_PROPERTY);
            if(user!=null) {
                httpMethod.addRequestHeader("X-" + MuleProperties.MULE_USER_PROPERTY, user);
            }
            if(m.getCorrelationId()!=null) {
                httpMethod.addRequestHeader("X-" + MuleProperties.MULE_CORRELATION_ID_PROPERTY, m.getCorrelationId());
                httpMethod.addRequestHeader("X-" + MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, String.valueOf(m.getCorrelationGroupSize()));
                httpMethod.addRequestHeader("X-" + MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, String.valueOf(m.getCorrelationSequence()));
            }
            if(m.getReplyTo()!=null) {
                httpMethod.addRequestHeader("X-" + MuleProperties.MULE_REPLY_TO_PROPERTY, m.getReplyTo().toString());
            }

            return httpMethod;
        } catch (Exception e)
        {
            throw new TransformerException("Failed to create Http request: " + e.getMessage(), e);
        }
    }
}
