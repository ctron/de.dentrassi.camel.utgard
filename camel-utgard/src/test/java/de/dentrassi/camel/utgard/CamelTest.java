/*
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.dentrassi.camel.utgard;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelTest {

    public static void main(final String[] args) throws Exception {

        final DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("utgard:192.168.122.72?user=opc&password=opc1234&classId=F8582CF2-88FB-11D0-B850-00C0F0104305&itemId=Triangle Waves.Int4")
                        .setBody(simple(
                                "Value: ${body.value.object}, Timestamp: ${body.timestamp.asCalendar().getTime()}"))
                        .to("stream:out");
            }
        });

        ctx.start();

        Thread.sleep(Long.MAX_VALUE);

    }
}
