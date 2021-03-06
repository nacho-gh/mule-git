package org.mule.config.spring;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.config.MuleConfiguration;

import org.springframework.beans.factory.SmartFactoryBean;

public class MuleConfigurationConfigurator extends MuleConfiguration implements MuleContextAware, SmartFactoryBean
{
    
    private MuleContext muleContext;

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    public boolean isEagerInit()
    {
        return true;
    }

    public boolean isPrototype()
    {
        return false;
    }

    public Object getObject() throws Exception
    {
        MuleConfiguration configuration = muleContext.getConfiguration();
        configuration.setDefaultSynchronousEndpoints(isDefaultSynchronousEndpoints());
        configuration.setWorkingDirectory(getWorkingDirectory());
        configuration.setDefaultSynchronousEventTimeout(getDefaultSynchronousEventTimeout());
        configuration.setDefaultEncoding(getDefaultEncoding());
        configuration.setDefaultTransactionTimeout(getDefaultTransactionTimeout());
        configuration.setDefaultRemoteSync(isDefaultRemoteSync());
        configuration.setClusterId(getClusterId());
        configuration.setDomainId(getDomainId());
        configuration.setId(getId());
        
        //configuration.setD
        configuration.setDefaultConnectionStrategy(getDefaultConnectionStrategy());
        
        return configuration;
//        
//        
//        <xsd:element name="default-threading-profile" type="threadingProfileType" minOccurs="0"/>
//        <xsd:element name="default-dispatcher-threading-profile" type="threadingProfileType" minOccurs="0"/>
//        <xsd:element name="default-receiver-threading-profile" type="threadingProfileType" minOccurs="0"/>
//        <xsd:element name="default-service-threading-profile" type="threadingProfileType" minOccurs="0"/>
//        <xsd:element ref="abstract-storage" minOccurs="0"/>

    }

    public Class getObjectType()
    {
        return MuleConfiguration.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
