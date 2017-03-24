/*
 * Copyright © 2016 Noviflow and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.flowManager.impl.utils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Collection;

/**
 * Abstract class implemented by classes interested in receiving
 * notifications about data tree changes.
 * @param <D> - Class inherited from {@link DataObject} listened in the Datastore.
 */
public abstract class AbstractDataTreeChangeListener<D extends DataObject>
        implements DataTreeChangeListener<D>, AutoCloseable {

    protected DataBroker dataBroker;

    public AbstractDataTreeChangeListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<D>> changes) {
        for (final DataTreeModification<D> change : changes) {
            final InstanceIdentifier<D> identifier = change.getRootPath().getRootIdentifier();
            final DataObjectModification<D> root = change.getRootNode();
            switch (root.getModificationType()) {
                case DELETE:
                    remove(identifier, root.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    update(identifier, root.getDataBefore(), root.getDataAfter());
                    break;
                case WRITE:
                    if (root.getDataBefore() == null) {
                        add(identifier, root.getDataAfter());
                    } else {
                        update(identifier, root.getDataBefore(), root.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type "
                            + root.getModificationType());
            }
        }
    }

    /**
     * Method removes DataObject which is identified by InstanceIdentifier
     * from device.
     *
     * @param identifier - the whole path to DataObject
     * @param del - DataObject for removing
     */
    protected abstract void remove(InstanceIdentifier<D> identifier, D del);

    /**
     * Method updates the original DataObject to the update DataObject
     * in device. Both are identified by same InstanceIdentifier
     *
     * @param identifier - the whole path to DataObject
     * @param original - original DataObject (for update)
     * @param update - changed DataObject (contain updates)
     */
    protected abstract void update(InstanceIdentifier<D> identifier, D original, D update);

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier
     * to device.
     *
     * @param identifier - the whole path to new DataObject
     * @param add - new DataObject
     * @throws RuntimeException
     */
    protected abstract void add(InstanceIdentifier<D> identifier, D add) throws RuntimeException;
}
