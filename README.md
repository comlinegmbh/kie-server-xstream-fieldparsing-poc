Kie-Server ignores the "ignore-unknown-elements" Parameter
==========================================================

When executing a BatchCommand with a custom data-model, the kie-server should interpret the header 
**x-kie-contenttype** with a value of **XSTREAM;ignore-unknown-elements=true** to ignore any additional
elements in the data-model, like mentioned in issue JBPM-9558.

We need this behaviour, because we want a loose coupling between the kie-client and the kie-server.
If the client gets updated with a new version of the data-model, the server should run with the older
version ignoring any new fields.

The **XStreamMarshaller** has an overloaded **unmarshall()** function which can handle the parameter mentioned above.
Unfortunately this method is not used in case of *BatchCommand* execution. In this case, the default implementation
is used, which sets the value to false:

    public <T> T unmarshall(String input, Class<T> type) {
        return unmarshall(input, type, Collections.singletonMap(XSTREAM_IGNORE_UNKNOWN, false));
    }

	
Setting the default to "true" solve the problem but perhaps this is not the desired solution.

Using the **MarshallingFormat.buildParameters()** to extract the parameter and pass it to the **XStreamMarshaller.unmarshall()**
function seems to be the right way. 
Therefore the **KieServerResource.executeCommands()** function must be extended to pass the parameters from the header to the underlying layers:

    ServiceResponsesList result = delegate.executeScript(command, MarshallerHelper.getFormat(contentType), null);

Maybe it would be considerable to change the **MarshallingFormat** form *enum* to a "normal" data object so the parameters could be transferred via instance variables.