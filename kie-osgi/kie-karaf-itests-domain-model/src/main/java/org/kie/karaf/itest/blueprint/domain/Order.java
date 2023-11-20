package org.kie.karaf.itest.blueprint.domain;

import java.io.Serializable;

public class Order implements Serializable {

    private final Customer customer;

    private final Drink drink;

    private Boolean approved;

    public Order(final Customer customer, final Drink drink) {
        this.customer = customer;
        this.drink = drink;
    }

    public void approve() {
        this.approved = true;
    }

    public void disapprove() {
        this.approved = false;
    }

    // This method duplicates with approve() and disapprove(). But added to clarify bitmask issue DROOLS-7014.
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public Drink getDrink() {
        return this.drink;
    }

    public Boolean isApproved() {
        return this.approved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (customer != null ? !customer.equals(order.customer) : order.customer != null) return false;
        if (drink != null ? !drink.equals(order.drink) : order.drink != null) return false;
        return !(approved != null ? !approved.equals(order.approved) : order.approved != null);

    }

    @Override
    public int hashCode() {
        int result = customer != null ? customer.hashCode() : 0;
        result = 31 * result + (drink != null ? drink.hashCode() : 0);
        result = 31 * result + (approved != null ? approved.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "customer=" + customer +
                ", drink=" + drink +
                ", approved=" + approved +
                '}';
    }
}
