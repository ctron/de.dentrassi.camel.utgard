/*
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.dentrassi.camel.utgard;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionTest {
    public static void main(final String[] args) throws Exception {

        Logger.getLogger("org.jinterop").setLevel(Level.OFF);

        final ConnectionKey connectionKey = new ConnectionKey();

        connectionKey.setClsid("F8582CF2-88FB-11D0-B850-00C0F0104305");
        connectionKey.setDomain("");
        connectionKey.setHost("192.168.122.72");
        connectionKey.setUser("opc");
        connectionKey.setPassword("opc1234");

        final UtgardConnection connection = new UtgardConnection(connectionKey);

        connection.addItem("Triangle Waves.Int4", v -> {
            System.out.println(v.getValue());
        });

        Thread.sleep(10_000);

        connection.removeItem("Triangle Waves.Int4");

        Thread.sleep(10_000);
    }
}
