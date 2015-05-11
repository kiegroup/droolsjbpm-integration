package org.kie.server.api.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "list-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbList implements Wrapped<List<?>> {

    @XmlElementWrapper(name = "items")
    private Object[] items;

    public JaxbList() {
    }

    public JaxbList(List<Object> items) {
        this.items = items.toArray();
    }

    public List<Object> getItems() {
        return Arrays.asList(items);
    }

    public void setItems(List<Object> items) {
        this.items = items.toArray();
    }

    @Override
    public List<?> unwrap() {
        List<Object> unwrapped = new ArrayList<Object>();

        if (items != null) {
            for (Object o : items) {
                Object item = o;
                if (o instanceof Wrapped) {
                    item = ((Wrapped) o).unwrap();
                }
                unwrapped.add(item);
            }
        }
        return unwrapped;
    }
}
