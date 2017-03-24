/*
 * Copyright © 2016 Inocybe, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.flowManager.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowManagerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManagerProvider.class);
    private final NodeDataTreeChangeListener dataTree;

    private final DataBroker dataBroker;

    public FlowManagerProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.dataTree = new NodeDataTreeChangeListener(dataBroker);
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("FlowManagerProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("FlowManagerProvider Closed");
    }
}