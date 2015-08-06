package com.github.dynamicextensionsalfresco.actions

import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import com.github.dynamicextensionsalfresco.actions.annotations.ActionMethod

import org.alfresco.repo.action.executer.ActionExecuter
import org.alfresco.service.cmr.action.Action
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.rule.RuleServiceException
import org.springframework.util.ReflectionUtils

/**
 * Represents a mapping from [ActionExecuter.execute] to an [ActionMethod] -annotated
 * method.

 * @author Laurens Fridael
 */
class ActionMethodMapping(private val bean: Any, private val method: Method) {

    public var nodeRefParameterIndex: Int = -1

    public var actionParameterIndex: Int = -1

    private val parameterCount: Int

    private val parameterMappingsByName = HashMap<String, ParameterMapping>()

    init {
        this.parameterCount = method.getParameterTypes().size()
    }

    public fun invokeActionMethod(action: Action, nodeRef: NodeRef) {
        val parameters = arrayOfNulls<Any>(parameterCount)
        if (nodeRefParameterIndex > -1) {
            parameters[nodeRefParameterIndex] = nodeRef
        }
        if (actionParameterIndex > -1) {
            parameters[actionParameterIndex] = action
        }
        for (entry in parameterMappingsByName.entrySet()) {
            val parameterMapping = entry.getValue()
            var value = action.getParameterValue(parameterMapping.getName())
            if (parameterMapping.isMandatory() && value == null) {
                /*
                 * We throw RuleServiceException just as ParameterizedItemAbstractBase does when it encounters a missing
                 * value for a mandatory property.
                 */
                throw RuleServiceException("Parameter '${parameterMapping.getName()}' is mandatory, but no value was given.")
            }
            /* Single values for a multi-valued property are wrapped in an ArrayList automatically. */
            if (parameterMapping.isMultivalued() && (value is Collection<*>) == false) {
                value = ArrayList(Arrays.asList<Serializable>(value))
            }
            parameters[parameterMapping.getIndex()] = value
        }

        ReflectionUtils.invokeMethod(method, bean, *parameters)
    }

    public fun hasParameter(name: String): Boolean {
        return parameterMappingsByName.containsKey(name)
    }

    public fun addParameterMapping(parameterMapping: ParameterMapping) {
        val name = parameterMapping.getName()
        if (parameterMappingsByName.containsKey(name) == false) {
            parameterMappingsByName.put(name, parameterMapping)
        } else {
            throw IllegalStateException("Duplicate parameter name '$name'.")
        }
    }
}