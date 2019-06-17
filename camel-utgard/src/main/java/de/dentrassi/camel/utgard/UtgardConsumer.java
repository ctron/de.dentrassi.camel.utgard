/*
 * Copyright (C) 2018, 2019 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.dentrassi.camel.utgard;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultMessage;
import org.openscada.opc.dcom.da.OPCITEMSTATE;

public class UtgardConsumer extends DefaultConsumer {

    private final String itemId;

    public UtgardConsumer(final UtgardEndpoint endpoint, final Processor processor) {
        super(endpoint, processor);
        this.itemId = endpoint.getItemId();
    }

    @Override
    public UtgardEndpoint getEndpoint() {
        return (UtgardEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        getEndpoint().getConnection().addItem(this.itemId, this::handleUpdate);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        getEndpoint().getConnection().removeItem(this.itemId);
    }

    private void handleUpdate(final OPCITEMSTATE state) throws Exception {
        final Exchange exchange = getEndpoint().createExchange();

        exchange.setIn(from(exchange.getContext(), state));

        getAsyncProcessor().process(exchange);
    }

    private Message from(final CamelContext camelContext, final OPCITEMSTATE state) {
        final DefaultMessage message = new DefaultMessage(camelContext);

        message.setBody(state);
        message.setHeader("utgard.itemId", this.itemId);

        return message;
    }

}
