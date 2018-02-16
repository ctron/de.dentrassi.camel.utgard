# OPC DA Camel component

This component for allows connections to OPC DA. For OPC UA you can use `camel-milo` which
is available in Apache Camel starting with version 2.19.0.

This work is based on the OpenSCADA Utgard project: [ibh-systems/org.openscada](https://github.com/ibh-systems/org.openscada). 

## Endpoint

The endpoint URI is `utgard:host?user=username&password=password&classId=01234567-89AB-CDEF-0123-4567890ABCDE&itemId=OPC.Item.ID`

**Note:** The URI must be used with the "class id" (aka "clsId"). It is *not* possible to use
the "prog id".

The following additional parameters may be used:

<dl>
<dt>refreshRate</dt><dd>The refresh rate of the item in milliseconds</dd>
<dt>domain</dt><dd>The domain name of the user</dd>
</dl>

**Note:** Currently the component only supports consuming data from OPC DA (aka reading).

## Message / Payload

The message consists of the body and headers, following the classic Apache Camel approach. The body will
contain the main OPC item state.

### Body

An instance of the `OPCITEMSTATE` as defined here: [OPCITEMSTATE.java](https://github.com/ibh-systems/org.openscada/blob/master/utgard/org.openscada.opc.dcom/src/org/openscada/opc/dcom/da/OPCITEMSTATE.java).

The main value is a `JIVariant`, which is a representation of the COM `Variant` in Java. You can see its definition here: [JIVariant.java](https://github.com/ibh-systems/org.openscada/blob/master/jinterop/org.openscada.jinterop.core/src/org/jinterop/dcom/core/JIVariant.java).

### Headers

Currently the following headers are provided:

<table>
<tr><th>Name</th><th>Description</th></tr>
<tr><td><code>utgard.itemId</code></td><td>The OPC item ID</td></tr>
</table>

## Install into Eclipse Kura

Install the DP using the Kura Package Manager:

* Choose a [released version](https://github.com/ctron/de.dentrassi.camel.utgard/releases)
* Copy the URL of the "DP" package and add it to Kura using the Web UI

This will extend the embedded Camel runtime to provide the `utgard` endpoint. You can now either
drop in your own Camel application bundle or use the pre-installed "Camel XML Router" to drop in
a set of Camel XML route definitions (also see example below).

### Example XML configuration

The following XML route definition subscribes to an OPC DA server

~~~xml
<routes xmlns="http://camel.apache.org/schema/spring">
  <route id="route1">
    <from uri="utgard:192.168.122.72?user=opc&amp;password=opc1234&amp;classId=F8582CF2-88FB-11D0-B850-00C0F0104305&amp;itemId=Triangle Waves.Int4"/>
    <setBody><simple>Value: ${body.value.object}, Timestamp: ${body.timestamp.asCalendar().getTime()}</simple></setBody>
    <to uri="stream:out"/>
  </route>
</routes>
~~~
